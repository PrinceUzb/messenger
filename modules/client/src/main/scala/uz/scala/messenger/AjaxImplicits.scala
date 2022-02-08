package uz.scala.messenger

import io.circe.{Decoder, Encoder}
import japgolly.scalajs.react.callback.Callback
import japgolly.scalajs.react.extra.Ajax
import japgolly.scalajs.react.extra.Ajax.Step2
import japgolly.scalajs.react.extra.internal.AjaxException
import org.scalajs.dom.{FormData, XMLHttpRequest}
import uz.scala.messenger.implicits.{CirceDecoderOps, GenericTypeOps}

abstract class AjaxImplicits {

  def onUnauthorized: Callback =
    Callback.alert("Your session has been expired!\nPlease log in.")

  def onError: AjaxException => Callback = {
    case error if (error.xhr.status == 200 || error.xhr.status == 400) && error.xhr.responseText.nonEmpty =>
      Callback.alert(error.xhr.responseText)
    case error if error.xhr.status == 401 =>
      onUnauthorized
    case _ =>
      Callback.alert("Something went wrong!")
  }

  private[this] def isSuccessFull: XMLHttpRequest => Boolean = xhr =>
    (xhr.status >= 200 && xhr.status < 300 && xhr.responseText.nonEmpty &&
      xhr.getResponseHeader("Content-Type") == "application/json") || xhr.status == 204 || xhr.status == 303

  def post[T: Encoder](url: String, body: T): Step2 =
    Ajax("POST", url).setRequestContentTypeJsonUtf8
      .send(body.toJson)

  def post(url: String, body: FormData): Step2 =
    Ajax("POST", url)
      .send(body)

  def put(url: String, body: FormData): Step2 =
    Ajax("PUT", url)
      .send(body)

  def get(url: String): Step2 =
    Ajax("GET", url).setRequestContentTypeJsonUtf8.send

  implicit class Step2Ops(val step2: Step2) {
    final def fail(onFailure: AjaxException => Callback): Step2 =
      step2.onComplete { xhr =>
        Callback.unless(isSuccessFull(xhr))(onFailure(AjaxException(xhr)))
      }

    final def done[T: Decoder](onSuccess: T => Callback): Step2 =
      step2.onComplete { implicit xhr =>
        Callback.when(isSuccessFull(xhr))(onSuccess(xhr.responseText.as[T]))
      }

    final def doneWithoutContent(onSuccess: => Callback): Step2 =
      step2.onComplete { implicit xhr =>
        Callback.when(isSuccessFull(xhr))(onSuccess)
      }
  }

}
