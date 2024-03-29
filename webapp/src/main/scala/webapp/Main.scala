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
import org.scalajs.dom.HTMLElement

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
    Outwatch.renderInto[SyncIO]("#app", app).unsafeRunSync()
  }

  def app = {
    val dmp       = new diffMatchPatch();
    val diff      = dmp.diff_main("dogs bark", "cats bark")
    val scrollPos = Subject.behavior(0.0)

    val rawConflict = Subject.behavior("""<<<<<<< ours
x = myNumber + foo
||||||| base
x = foo + myNumber
=======
x = foo + otherNumber
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
          cls  := "dark:bg-slate-900 dark:text-slate-100",
        ),
      ),
      merged.map {
        case Right((conflict, mergedResult)) =>
          div(
            div(
              cls := "flex",
              showCode(
                conflict.a,
                b(conflict.aName),
                Some(conflict.a),
                scrollPos,
                colorClasses = "bg-blue-100 dark:bg-blue-900/50",
              )(
                cls      := "flex-1 m-1",
                minWidth := "0px",
              ),
              showCode(conflict.base, b(conflict.baseName), Some(conflict.base), scrollPos)(
                cls      := "flex-1 m-1",
                minWidth := "0px",
              ),
              showCode(
                conflict.b,
                b(conflict.bName),
                Some(conflict.b),
                scrollPos,
                colorClasses = "bg-violet-100 dark:bg-violet-900/50",
              )(
                cls      := "flex-1 m-1",
                minWidth := "0px",
              ),
            ),
            div(
              cls := "flex",
              showCode(
                Diff(conflict.base, conflict.a),
                VModifier(b(conflict.baseName), " → ", b(conflict.aName)),
                scrollPos = scrollPos,
              )(
                cls      := "flex-1 m-1",
                minWidth := "0px",
              ),
              showCode(
                mergedResult,
                "merged",
                Some(mergedResult),
                scrollPos = scrollPos,
                colorClasses = "bg-gray-100 dark:bg-gray-900 dark:text-slate-100 border-2 border-neutral",
              )(
                cls      := "flex-1 m-1",
                minWidth := "0px",
              ),
              showCode(
                Diff(conflict.base, conflict.b),
                VModifier(b(conflict.baseName), " → ", b(conflict.bName)),
                scrollPos = scrollPos,
              )(
                cls      := "flex-1 m-1",
                minWidth := "0px",
              ),
            ),
            div(
              cls := "flex",
              showCode(
                Diff(conflict.b, mergedResult),
                VModifier(b(conflict.bName), " → ", "merged"),
                scrollPos = scrollPos,
                colorClasses = "bg-violet-100 dark:bg-violet-900/50",
              )(
                cls      := "flex-1 m-1",
                minWidth := "0px",
              ),
              showCode(
                Diff(conflict.base, mergedResult),
                VModifier(b(conflict.baseName), " → ", "merged"),
                scrollPos = scrollPos,
              )(
                cls      := "flex-1 m-1",
                minWidth := "0px",
              ),
              showCode(
                Diff(conflict.a, mergedResult),
                VModifier(b(conflict.aName), " → ", "merged"),
                scrollPos = scrollPos,
                colorClasses = "bg-blue-100 dark:bg-blue-900/50",
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

  def showCode(
    codeRendered: VModifier,
    description: VModifier = VModifier.empty,
    codeStr: Option[String] = None,
    scrollPos: Subject[Double] = Subject.behavior(0.0),
    colorClasses: String = "bg-gray-100 dark:bg-gray-900 dark:text-slate-100",
  ) = {
    div(
      div(
        cls := "flex",
        div(description, cls := "mr-auto whitespace-nowrap text-ellipsis overflow-hidden"),
        codeStr.map(copyButton),
      ),
      pre(
        code(codeRendered),
        cls := "overflow-x-auto",
        syncedScrollPos(scrollPos),
        cls := "rounded-lg p-4 mt-2 whitespace-pre font-mono",
        cls := colorClasses,
      ),
    )
  }

  def syncedScrollPos(scrollPos: Subject[Double]) = {
    var ignoreNextScrollEvent = false
    VModifier(
      onScroll.filter { e =>
        val ignore = ignoreNextScrollEvent
        ignoreNextScrollEvent = false
        !ignore
      }.map { e =>
        val target = e.target.asInstanceOf[HTMLElement]
        // println(s"out: ${target.scrollLeft} / ${(target.scrollWidth - target.clientWidth + 1)}")
        target.scrollLeft / (target.scrollWidth - target.clientWidth + 1)
      } --> scrollPos,
      VModifier.managedElement.asHtml { target =>
        scrollPos.unsafeForeach { pos =>
          val newScrollLeft = (target.scrollWidth - target.clientWidth + 1) * pos
          if (newScrollLeft != target.scrollLeft) {
            // println("SET " + newScrollLeft)
            ignoreNextScrollEvent = true
            target.scrollLeft = newScrollLeft
          }
        // target.scrollLeft = (target.scrollWidth - target.clientWidth + 1) * pos
        }
      },
    )
  }

  def copyButton(value: String) = {
    VModifier.ifTrue(canWriteClipboard)(
      button(
        title := "Copy to Clipboard",
        cls   := "cursor-pointer",
        onClick.foreach { _ =>
          navigator.clipboard.writeText(value)
        },
        "copy",
        cls   := "btn btn-xs bg-transparent text-base-content hover:text-secondary-content border-1 border-base-content rounded",
      ),
    ),
  }
}
