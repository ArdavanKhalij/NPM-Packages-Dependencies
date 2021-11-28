////////////////////////////////////////////////// Libraries ////////////////////////////////////////////////////
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, IOResult, OverflowStrategy}
import akka.stream.scaladsl.{Compression, FileIO, Flow, Keep, RunnableGraph, Sink, Source}

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
//  Sink
  val sink: Sink[Package, Future[Done]] = Sink.foreach(x => println("Package Name: "+x.Name))
//  Make the graph
  val runnableGraph: RunnableGraph[Future[Done]] = source.via(flowUnzip)
    .via(flowString)
    .via(flowSplitLines)
    .via(flowConverter)
    .toMat(sink)(Keep.right)
//  Run and then terminate
  runnableGraph.run().foreach(_ => actorSystem.terminate())
}
////////////////////////////////////////// The object of the project ////////////////////////////////////////////
