package uz.scala.messenger
import org.scalajs.dom.{document, window}
import uz.scala.messenger.pages.{IndexPage, LoginPage}

object Main extends App {
  def flashAlert: Option[String] = Option(window.sessionStorage.getItem("error"))
  val renderedPage = document.body.id match {
    case "index" => IndexPage().component
    case "login" => LoginPage().component(flashAlert)
    case _ =>
      throw new Exception("Identity of the body doesn't match")
  }
  renderedPage.renderIntoDOM(document.getElementById("app"))

}
