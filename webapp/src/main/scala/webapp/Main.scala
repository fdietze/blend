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
        cls := "flex flex-wrap",
        textArea(
          rows := 10,
          value <-- rawConflict,
          onInput.value --> rawConflict,
          cls  := "font-mono p-2",
          cls  := "flex-1 border-2 border-blue-400 rounded-lg",
        ),
        conflict.map {
          case Right(conflict) =>
            val codeStyle = cls := "bg-gray-100 rounded-lg p-4 mt-2 whitespace-pre-wrap"
            div(
              cls := "ml-4 flex-1",
              b(s"${conflict.aName}"),
              " diff:",
              div(Diff(conflict.base, conflict.a), codeStyle),
              b(s"${conflict.bName}"),
              " diff:",
              div(Diff(conflict.base, conflict.b), codeStyle),
            )
          case Left(error)     => VDomModifier.empty
        },
      ),
      merged.map {
        case Right((conflict, mergedResult)) =>
          val codeStyle = cls := "bg-gray-100 rounded-lg p-4 mt-2 whitespace-pre-wrap font-mono"
          div(
            div(
              cls := "flex mt-4",
              div("merged result:", cls := "mr-auto"),
              VDomModifier.ifTrue(canWriteClipboard)(
                button(
                  title := "Copy to Clipboard",
                  cls   := "cursor-pointer",
                  onClick.foreach {
                    navigator.clipboard.writeText(mergedResult)
                  },
                  "copy",
                  cls   := "btn btn-sm text-white bg-blue-400 rounded",
                ),
              ),
            ),
            pre(
              code(mergedResult),
              codeStyle,
            ),
            "merged diff:",
            div(Diff(conflict.base, mergedResult), codeStyle),
            s"merged diff against ",
            b(s"${conflict.aName}:"),
            div(Diff(conflict.a, mergedResult), codeStyle),
            s"merged diff against ",
            b(s"${conflict.bName}:"),
            div(Diff(conflict.b, mergedResult), codeStyle),
          )
        case Left(error)                     => div(error.getMessage)
      },
    )
  }
}
