import scala.collection.mutable.ListBuffer

case class Package(Name: String = "Empty", Dependencies: ListBuffer[Int] = ListBuffer[Int]()
                   , Version: ListBuffer[String] = ListBuffer[String]()){
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
}
