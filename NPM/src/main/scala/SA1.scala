////////////////////////////////////////////////// Libraries ////////////////////////////////////////////////////
import akka.actor.ActorSystem
import akka.stream.scaladsl.GraphDSL.Implicits.{port2flow}
import akka.stream.{ActorMaterializer, FlowShape, IOResult, OverflowStrategy}
import akka.stream.scaladsl.{Balance, Broadcast, Compression, FileIO, Flow, GraphDSL, Keep, Merge, RunnableGraph, Sink, Source, Zip}

import java.nio.file.Paths
import akka.{Done, NotUsed}
import akka.util.ByteString

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.language.postfixOps
////////////////////////////////////////////////// Libraries ////////////////////////////////////////////////////
////////////////////////////////////////// The object of the project ////////////////////////////////////////////
object SA1 extends App {
//  Implicit values
  implicit val actorSystem: ActorSystem = ActorSystem("SA1")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = materializer.executionContext
//  Defining source
  val path = Paths.get("src/main/resources/packages.txt.gz")
  val source: Source[ByteString, Future[IOResult]] = FileIO.fromPath(path)
//  Unzipping the file
  val flowUnzip: Flow[ByteString, ByteString, NotUsed] = Compression.gunzip()
//  Convert ByteString to String
  val flowString: Flow[ByteString, String, NotUsed] = Flow[ByteString].map(_.utf8String.toString)
//  Split the string to package names
  val flowSplitLines: Flow[String, String, NotUsed] = Flow[String].mapConcat(_.split("\n").toList)
//  Convert the string to Package type
  val flowConverter: Flow[String, Package, NotUsed] = Flow[String].map(x => Package(Name = x))
//  Define the buffer
  val buffer = Flow[Package].buffer(10, OverflowStrategy.backpressure)
//  Limit request to NPM
  val requestLimiter = Flow[Package].throttle(1, 3.second)
//  API Request and get the list of versions
  val requestApiAndGetVersions: Flow[Package, Package, NotUsed] = Flow[Package].map(x => x.get_json_and_versions)
//  Get Dependencies of each package
  val getDependencies: Flow[Package, Package, NotUsed] = Flow[Package].map(x => x.get_dependencies)
//  Get DevDependencies of each package
  val getDevDependencies: Flow[Package, Package, NotUsed] = Flow[Package].map(x => x.get_dev_dependencies)
//  We need to prepare data here for the next steps
  val prepareDataForTheNextSteps: Flow[ByteString, Package, NotUsed] = flowUnzip
    .via(flowString)
    .via(flowSplitLines)
    .via(flowConverter)
    .via(requestLimiter)
    .via(requestApiAndGetVersions)
//  The graph for making one pipeline
  val onePipelineGraph: Flow[Package, (Package, Package), NotUsed] = Flow.fromGraph(
  GraphDSL.create() { implicit builder =>
    val dependencies = builder.add(getDependencies)
    val devDependencies = builder.add(getDevDependencies)
    val broadcast = builder.add(Broadcast[Package](2))
    val zip = builder.add(Zip[Package, Package])
    broadcast.out(0) ~> dependencies ~> zip.in0
    broadcast.out(1) ~> devDependencies ~> zip.in1
    FlowShape(broadcast.in, zip.out)
  })
//  Having parallel pipelines
  val parallelPipeline: Flow[Package, ((Package, Package)), NotUsed] = Flow.fromGraph(
  GraphDSL.create() { implicit builder =>
    val balance = builder.add(Balance[Package](2))
    val onePipeline = onePipelineGraph
    val merge = builder.add(Merge[(Package, Package)](2))
    balance.out(0) ~> onePipelineGraph ~> merge.in(0)
    balance.out(1) ~> onePipelineGraph ~> merge.in(1)
    FlowShape(balance.in, merge.out)
  })
  //  Sink
  val sink: Sink[(Package, Package), Future[Done]] = Sink
  .foreach[(Package, Package)]{
    x =>
      println("Analysing " + x._1.Name)
        for (i <- 0 to x._1.Version.length-1) {
          println("Version: "+x._1.Version(i)+", Dependencies: "+x._1.Dependencies(i)+", DevDependencies: "+x._2.DevDependencies(i))
        }
  }
//  Make the graph
  val runnableGraph: RunnableGraph[Future[Done]] = source
    .via(prepareDataForTheNextSteps)
    .via(buffer)
    .via(parallelPipeline)
    .toMat(sink)(Keep.right)
//  Run and then terminate
  runnableGraph.run().foreach(_ => actorSystem.terminate())
}
////////////////////////////////////////// The object of the project ////////////////////////////////////////////
