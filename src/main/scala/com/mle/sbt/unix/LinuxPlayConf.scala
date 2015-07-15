package com.mle.sbt.unix

/**
 * Useful settings for Play Framework.
 *
 * @author Michael
 */
case class LinuxPlayConf(httpPort: Option[Int], httpsPort: Option[Int], appName: String) {
  def stringifyPort(port: Option[Int]) = port map (_.toString) getOrElse "disabled"

  val httpValue = stringifyPort(httpPort)
  val httpsValue = stringifyPort(httpsPort)
  val javaOptionsMap = Map(
    "http.port" -> httpValue,
    "https.port" -> httpsValue,
    "pidfile.path" -> s"/var/run/$appName/$appName.pid",
    s"$appName.home" -> s"/var/run/$appName")
  val javaOptions = javaOptionsMap.map(kv => {
    val (key, value) = kv
    s"-D$key=$value"
  }).toSeq
}
