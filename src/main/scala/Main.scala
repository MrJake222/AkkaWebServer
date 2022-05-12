package httpqueens

import akka.actor.ActorSystem
import akka.event.Logging.DebugLevel
import play.api.libs.json.{Format, Json}
import scala.util.control.Breaks._

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

    val y_vals = connParams.params.values.toList.map(x => x.toInt)
    val x_vals = connParams.params.keys.toList.map(x => x.toInt)
    var forbidden = Array[Int]()
    val n = y_vals.size
    for (fig <- 0 until n) {
      val fig_x = x_vals(fig)
      val fig_y = y_vals(fig)
      var mini = List(fig_x, fig_y).min
      val maxi = List(fig_x - mini, fig_y - mini).max
      val iters = 8 - maxi
      for (i <- 0 until iters) {
        val x = fig_x - mini + i
        val y = fig_y - mini + i
        val index = 8 * y + x
        if (!forbidden.contains(index))
          forbidden = forbidden :+ index
      }
      mini = List(7 - fig_x, fig_y).min
      breakable {
        for (i <- 0 until 8) {
          if (fig_y - mini + i > 7 || fig_x + mini - i < 0)
            break
          val x = fig_x + mini - i
          val y = fig_y - mini + i
          val index = 8 * y + x
          if (!forbidden.contains(index))
            forbidden = forbidden :+ index
        }
      }
    }

    val next_row = find_next_empty_row(y_vals, Array[Int]())
    put_queen_in_row(y_vals, x_vals, Array[Int](), Array[Int](), forbidden, next_row)

    def put_queen_in_row(y_vals: List[Int], x_vals: List[Int], y_used: Array[Int], x_used: Array[Int], forbidden: Array[Int], current_row: Int): Unit = {
      if (current_row == -1) {
        val dic = Array[Int](0, 0, 0, 0, 0, 0, 0, 0)
        for (fig <- x_vals.indices)
          dic(x_vals(fig)) = y_vals(fig)
        for (fig <- x_used.indices)
          dic(x_used(fig)) = y_used(fig)
        solutions = solutions :+ dic
      }
      for (j <- 0 until 8) {
        if (!x_vals.contains(j)) {
          val index = current_row * 8 + j
          if (!forbidden.contains(index) && check(current_row, j, y_used, x_used)) {
            val next_row = find_next_empty_row(y_vals, y_used :+ current_row)
            put_queen_in_row(y_vals, x_vals, y_used :+ current_row, x_used :+ j, forbidden, next_row)
          }
        }
      }
    }

    solutions
    val body = Json.toJson(QueensSolution(solutions)).toString()
    RouteData("application/json", body)
  }

  def find_next_empty_row(y_vals: List[Int], y_used: Array[Int]): Int = {
    for (i <- 0 until 8) {
      if (!y_vals.contains(i) && !y_used.contains(i))
        return i
    }
    -1
  }

  def check(current_row: Int, j: Int, y_used: Array[Int], x_used: Array[Int]): Boolean = {
    if (y_used.contains(current_row))
      return false
    if (x_used.contains(j))
      return false
    for (fig <- y_used.indices) {
      val fig_x = x_used(fig)
      val fig_y = y_used(fig)
      val diff_y = (current_row - fig_y).abs
      val diff_x = (j - fig_x).abs
      if (diff_x == diff_y)
        return false
    }
    true
  }

  def queenshtml(connParams: ConnParams): RouteData = {
    // Można tak ale raczej to słaby pomysł
    val body = "<!DOCTYPE html><html><head><meta charset=\"utf-8\"></head><body><h1>Rozwiazanie hetmanow</h1></body></html>\n"
    RouteData("text/html", body)
  }
}