package webapp

import scala.scalajs.js
import outwatch._
import outwatch.dsl._
import cats.effect.{IO, SyncIO}
import colibri.Subject
import org.scalajs.dom.window.navigator

import typings.diffMatchPatch.mod._
import scala.util.Success
import scala.util.Failure
import scala.util.Try

case class Conflict(base: String, baseName: String, a: String, aName: String, b: String, bName: String)

object Main {

  LoadCss()

  val canReadClipboard  =
    Try(navigator.clipboard.asInstanceOf[js.Dynamic].readText.asInstanceOf[js.UndefOr[js.Any]] != js.undefined).toOption
      .getOrElse(false)
  val canWriteClipboard =
    Try(
      navigator.clipboard.asInstanceOf[js.Dynamic].writeText.asInstanceOf[js.UndefOr[js.Any]] != js.undefined,
    ).toOption.getOrElse(false)

  def main(args: Array[String]): Unit = {
    OutWatch.renderInto[IO]("#app", app).unsafeRunSync()
  }

  def app = {
    val dmp  = new diffMatchPatch();
    val diff = dmp.diff_main("dogs bark", "cats bark")

    val rawConflict = Subject.behavior("""<<<<<<< ours
puts 'hola world'
||||||| base
puts 'hello world'
=======
puts 'hello mundo'
>>>>>>> theirs
""")
    val conflict    = rawConflict.map(ConflictParser.apply)
    val merged      = conflict.map {
      _.map { conflict =>
        val patchesA = dmp.patch_make(conflict.base, conflict.a)
        val patchesB = dmp.patch_make(conflict.base, conflict.b)
        (conflict, dmp.patch_apply(patchesA ++ patchesB, conflict.base)(0))
      }
    }

    div(
      cls := "p-4",
      div(
        textArea(
          rows := 10,
          value <-- rawConflict,
          onInput.value --> rawConflict,
          cls  := "font-mono p-2",
          cls  := "w-full border-2 border-blue-400 rounded-lg",
        ),
      ),
      merged.map {
        case Right((conflict, mergedResult)) =>
          def showCode(
            codeRendered: VDomModifier,
            description: VDomModifier = VDomModifier.empty,
            codeStr: Option[String] = None,
            codeModifiers: VDomModifier = VDomModifier.empty,
          ) =
            div(
              div(
                cls := "flex",
                div(description, cls := "mr-auto whitespace-nowrap"),
                codeStr.map(copyButton),
              ),
              pre(
                code(codeRendered),
                cls := "overflow-x-auto",
                cls := "bg-gray-100 rounded-lg p-4 mt-2 whitespace-pre font-mono",
                codeModifiers,
              ),
            )

          div(
            div(
              cls := "flex",
              showCode(conflict.a, b(conflict.aName), Some(conflict.a), cls := "bg-blue-100")(
                cls      := "flex-1 m-1",
                minWidth := "0px",
              ),
              showCode(conflict.base, b(conflict.baseName), Some(conflict.base))(
                cls      := "flex-1 m-1",
                minWidth := "0px",
              ),
              showCode(conflict.b, b(conflict.bName), Some(conflict.b), cls := "bg-violet-100")(
                cls      := "flex-1 m-1",
                minWidth := "0px",
              ),
            ),
            div(
              cls := "flex",
              showCode(
                Diff(conflict.base, conflict.a),
                VDomModifier(b(conflict.aName), " diff ", b(conflict.baseName)),
              )(
                cls                                                    := "flex-1 m-1",
                minWidth                                               := "0px",
              ),
              showCode(mergedResult, "merged", Some(mergedResult))(cls := "flex-1 m-1", minWidth := "0px"),
              showCode(
                Diff(conflict.base, conflict.b),
                VDomModifier(b(conflict.bName), " diff ", b(conflict.baseName)),
              )(
                cls                                                    := "flex-1 m-1",
                minWidth                                               := "0px",
              ),
            ),
            div(
              cls := "flex",
              showCode(
                Diff(conflict.b, mergedResult),
                VDomModifier(b("merged"), " diff ", b(conflict.bName)),
                codeModifiers = cls := "bg-violet-100",
              )(
                cls      := "flex-1 m-1",
                minWidth := "0px",
              ),
              showCode(Diff(conflict.base, mergedResult), VDomModifier(b("merged"), " diff ", b(conflict.baseName)))(
                cls      := "flex-1 m-1",
                minWidth := "0px",
              ),
              showCode(
                Diff(conflict.a, mergedResult),
                VDomModifier(b("merged"), " diff ", b(conflict.aName)),
                codeModifiers = cls := "bg-blue-100",
              )(
                cls      := "flex-1 m-1",
                minWidth := "0px",
              ),
            ),
          )
        case Left(error)                     => div(error.getMessage)
      },
    )
  }

  def copyButton(value: String) = {

    VDomModifier.ifTrue(canWriteClipboard)(
      button(
        title := "Copy to Clipboard",
        cls   := "cursor-pointer",
        onClick.foreach {
          navigator.clipboard.writeText(value)
        },
        "copy",
        cls   := "btn btn-xs text-white bg-blue-400 rounded",
      ),
    ),
  }
}
