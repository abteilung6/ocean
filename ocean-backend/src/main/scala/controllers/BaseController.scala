package org.abteilung6.ocean
package controllers

import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.server.Route
import sttp.tapir._

trait BaseController {
  val apiPrefix: String = "api"
  val basePath: String
  val tag: String

  def route: Route

  def endpoints: List[AnyEndpoint]

  /**
   * Provides a relative URI based on overrides and parameters.
   * {{{
   *   > accountController.toRelativeURI(subRoute = "me", parameters = Map("page" -> "1"))
   *   > api/account/me?page=1
   * }}}
   */
  def toRelativeURI(subRoute: String = "", parameters: Map[String, String] = Map()): String = {
    val path = subRoute match {
      case "" => s"$apiPrefix/$basePath"
      case _  => s"$apiPrefix/$basePath/$subRoute"
    }
    Uri(path).withQuery(Query(parameters)).toString()
  }

  def toRelativeEndpoint(subEndpoint: String): EndpointInput[Unit] =
    apiPrefix / basePath / subEndpoint

  // Add https support when needed
  def toAbsoluteURI(
    baseURL: String,
    port: Int,
    subRoute: String,
    parameters: Map[String, String] = Map()
  ): String =
    Uri
      .from(scheme = "http", host = baseURL, port = port, path = s"/$subRoute")
      .withQuery(Query(parameters))
      .toString()
}
