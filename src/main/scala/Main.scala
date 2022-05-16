package httpqueens

import akka.actor.ActorSystem
import akka.event.Logging.DebugLevel
import play.api.libs.json.{Format, Json}

case class QueensSolution(solutions: Array[Array[Int]])
object QueensSolution {
  implicit val format: Format[QueensSolution] = Json.format
}

object Main {
  val PORT: Int = 8080

  def main(args: Array[String]): Unit = {
    val system: ActorSystem = ActorSystem("MySystem")
    system.eventStream.setLogLevel(DebugLevel)

    val server: WebServer = new WebServer(PORT, system, "static")

    server.addRoute("/queens", queens)

    server.listen()
  }

  def queens(connParams: ConnParams): RouteData = {
    val y_vals = connParams.params.values.toList.map(x => x.toInt)
    val x_vals = connParams.params.keys.toList.map(x => x.toInt)

    val solver = new QueensSolver(y_vals, x_vals)
    val solutions = solver.queensSolve()

    val body = Json.toJson(QueensSolution(solutions)).toString()
    RouteData("application/json", body)
  }
}