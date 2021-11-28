case class Package(Name: String = "Empty", Dependencies: List[Int] = List(), Version: List[String] = List()){
  def get_json {
    val url = s"https://registry.npmjs.org/${Name}"
    val response = requests.get(url)
    if (response.statusCode == 200) {
      val json = ujson.read(response.text)
      val versions = json.obj("versions").obj.toList
      print(versions)
      for ((version, remain) <- versions){
        Version.appended(version)
      }
    }
    else {
      println(response.statusCode)
    }
  }
}
