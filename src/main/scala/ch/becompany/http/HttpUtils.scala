package ch.becompany.http

object HttpUtils {

  def queryString(params: Map[String, String]): String =
    params.map { case (k, v) => s"$k=$v" }.mkString("&")

  def queryString(params: (String, String)*): String =
    queryString(params.toMap)

}
