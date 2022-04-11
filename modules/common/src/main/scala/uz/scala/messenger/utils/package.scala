package uz.scala.messenger
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

package object utils {
  def formatter: String => DateTimeFormatter = DateTimeFormatter.ofPattern

  def ldtToStr(date: LocalDateTime, format: String = "MMM dd, yyyy HH:mm"): String =
    date.format(formatter(format))
}
