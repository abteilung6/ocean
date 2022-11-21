package org.abteilung6.ocean
package controllers

import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.server.Route
import sttp.tapir._

trait BaseController {
  val basePath: String
  val tag: String

  def route: Route

  def endpoints: List[AnyEndpoint]

  def withSubRoute(subRoute: String, parameters: Map[String, String] = Map()): String =
    Uri(s"/$basePath/$subRoute").withQuery(Query(parameters)).toString()

  def withSubEndpoint(subEndpoint: String): EndpointInput[Unit] =
    basePath / subEndpoint
}
