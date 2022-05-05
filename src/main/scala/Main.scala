package httpqueens

import akka.actor.ActorSystem
import akka.event.Logging.DebugLevel
import play.api.libs.json.{Format, Json}

// tutaj możemy dodać parametry do zwracania
case class QueensSolution(solutions: Array[Array[Int]])
// to jest uniwersalne
object QueensSolution {
  implicit val format: Format[QueensSolution] = Json.format
}

object Main {
  val PORT: Int = 8080

  def main(args: Array[String]): Unit = {
    val system: ActorSystem = ActorSystem("MySystem")
    system.eventStream.setLogLevel(DebugLevel)

    val server: WebServer = new WebServer(PORT, system, "static")
    // Statyczne pliki w static w głównym katalogu projektu

    server.addRoute("/queens", queens)
    server.addRoute("/queenshtml", queenshtml)

    server.listen()
  }

  def queens(connParams: ConnParams): RouteData = {
    // Lepiej tak i jakiś fetch() w JS zrobić
    // obsługa tylko GET, parametry w connParams.params
    println(connParams.params)

    var solutions = Array[Array[Int]]()
    solutions = solutions :+ Array(0, 1, 2, 3, 4, 5, 6, 7)

    val body = Json.toJson(QueensSolution(solutions)).toString()
    RouteData("application/json", body)
  }

  def queenshtml(connParams: ConnParams): RouteData = {
    // Można tak ale raczej to słaby pomysł
    val body = "<!DOCTYPE html><html><head><meta charset=\"utf-8\"></head><body><h1>Rozwiazanie hetmanow</h1></body></html>\n"
    RouteData("text/html", body)
  }
}