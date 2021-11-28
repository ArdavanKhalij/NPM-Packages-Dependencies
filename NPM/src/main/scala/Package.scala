import com.sun.tools.classfile.Dependencies

import scala.collection.mutable.ListBuffer

case class Package(Name: String = "Empty", Dependencies: ListBuffer[Int] = ListBuffer[Int]()
                   , Version: ListBuffer[String] = ListBuffer[String]()
                   , DevDependencies: ListBuffer[Int] = ListBuffer[Int]()){
//  Get versions
  def get_json_and_versions: Package = {
    val url = s"https://registry.npmjs.org/${Name}"
    val response = requests.get(url)
    if (response.statusCode == 200) {
      val json = ujson.read(response.text)
      val versions = json.obj("versions").obj.toList
      for ((version, remain) <- versions){
        Version += version
      }
    }
    else {
      println(response.statusCode)
    }
    return this
  }
//  Get Dependencies
  def get_dependencies: Package = {
    var numberOfDependencies = 0
    var numberOfDevDependencies = 0
    val url = s"https://registry.npmjs.org/${Name}"
    var dependencies = ListBuffer()
    val response = requests.get(url)
    if (response.statusCode == 200) {
      val json = ujson.read(response.text)
      val versions = json.obj("versions").obj.toList
      for ((version, remain) <- versions) {
        try {
          var dependencies = remain.obj("dependencies").obj.toList
          numberOfDependencies += dependencies.length
        }
        catch {
          case e => numberOfDependencies += 0
        }
        Dependencies += numberOfDependencies
        numberOfDependencies = 0
        try {
          var devDependencies = remain.obj("devDependencies").obj.toList
          numberOfDevDependencies += devDependencies.length
        }
        catch {
          case e => numberOfDevDependencies += 0
        }
        DevDependencies += numberOfDevDependencies
        numberOfDevDependencies = 0
      }
    }
    else {
      println(response.statusCode)
    }
    return this
  }
}
