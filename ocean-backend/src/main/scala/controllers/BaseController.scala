package org.abteilung6.ocean
package controllers

import akka.http.scaladsl.server.Route
import sttp.tapir.AnyEndpoint

trait BaseController {
  val basePath: String
  def route: Route
  def endpoints: List[AnyEndpoint]
}
