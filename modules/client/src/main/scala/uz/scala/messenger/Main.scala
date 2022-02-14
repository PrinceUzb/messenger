package uz.scala.messenger
import org.scalajs.dom.document
import uz.scala.messenger.pages.{IndexPage, LoginPage}

object Main extends App {
  def flashAlert: Option[String] = document.cookie.split(";").find(_ == "HTTP4S_FLASH")
  val renderedPage = document.body.id match {
    case "index" => IndexPage().component
    case "login" => LoginPage().component(flashAlert)
    case _ =>
      throw new Exception("Identity of the body doesn't match")
  }
  renderedPage.renderIntoDOM(document.getElementById("app"))

}
