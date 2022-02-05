package uz.scala.messenger
import org.scalajs.dom
import org.scalajs.dom.document
import uz.scala.messenger.pages.{IndexPage, LoginPage, NotFound}

object Main extends App {

  val renderedPage = dom.window.location.pathname match {
    case "/login" =>
      LoginPage().component
    case "/" =>
      IndexPage().component
    case other =>
      println(other)
      NotFound().component
  }
  renderedPage.renderIntoDOM(document.getElementById("app"))

}
