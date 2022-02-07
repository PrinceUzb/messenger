package uz.scala.messenger
import org.scalajs.dom.document
import uz.scala.messenger.pages.{IndexPage, LoginPage}

object Main extends App {

  val renderedPage = document.body.id match {
    case "index" => IndexPage().component
    case "login" => LoginPage().component
    case _ =>
      throw new Exception("Identity of the body doesn't match")
  }
  renderedPage.renderIntoDOM(document.getElementById("app"))

}
