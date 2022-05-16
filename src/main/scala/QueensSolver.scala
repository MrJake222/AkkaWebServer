package httpqueens

import scala.util.control.Breaks._

class QueensSolver(y_vals: List[Int], x_vals: List[Int]) {

  def queensSolve(): Array[Array[Int]] = {
    var solutions = Array[Array[Int]]()

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
  }

  private def find_next_empty_row(y_vals: List[Int], y_used: Array[Int]): Int = {
    for (i <- 0 until 8) {
      if (!y_vals.contains(i) && !y_used.contains(i))
        return i
    }
    -1
  }

  private def check(current_row: Int, j: Int, y_used: Array[Int], x_used: Array[Int]): Boolean = {
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
}
