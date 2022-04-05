package webapp

import scala.scalajs.js
import outwatch._
import outwatch.dsl._
import cats.effect.{IO, SyncIO}
import colibri.Subject

import typings.diffMatchPatch.mod._
import webapp.util.form.{given, *}
import scala.util.Success

case class Conflict(base: String, a: String, b: String)

object Main {
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

    val conflict = Subject.behavior(summon[Form[Conflict]].default)

    div(
      textArea(
        rows  := 10,
        value <-- rawConflict,
        onInput.value --> rawConflict,
        width := "100%",
      ),
      // div(rawConflict.map(ConflictParser.apply).map(_.toString)),
      managedFunction(() =>
        rawConflict
          .map(ConflictParser.apply)
          .collect { case Success(conflict) =>
            conflict
          }
          .subscribe(conflict),
      ),
      // summon[Form[Conflict]].form(conflict),
      conflict.map { conflict =>
        val patchesA = dmp.patch_make(conflict.base, conflict.a)
        val patchesB = dmp.patch_make(conflict.base, conflict.b)
        val result   = dmp.patch_apply(patchesA ++ patchesB, conflict.base)
        div(
          "merged result:",
          pre(result(0)),
          "final diff:",
          div(Diff(conflict.base, result(0)), fontFamily  := "monospace", whiteSpace.pre),
          "first diff:",
          div(Diff(conflict.base, conflict.a), fontFamily := "monospace", whiteSpace.pre),
          "second diff:",
          div(Diff(conflict.base, conflict.b), fontFamily := "monospace", whiteSpace.pre),
        )
      },
    )
  }
}
