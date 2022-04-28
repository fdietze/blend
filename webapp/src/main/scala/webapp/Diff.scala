package webapp

import outwatch._
import outwatch.dsl._
import typings.diffMatchPatch.mod._

object Diff {
  val dmp = new diffMatchPatch();

  def apply(original: String, result: String) = {
    // https://github.com/google/diff-match-patch/wiki/API#diff_maintext1-text2--diffs
    val diff = dmp.diff_main(original, result)
    dmp.diff_cleanupSemantic(diff)

    p(
      diff.map { part =>
        val str       = part(1)
        val isAdded   = part(0) == 1
        val isRemoved = part(0) == -1
        span(
          // showControlChars(str),
          str,
          if (isAdded) (cls := "text-green-500 dark:text-green-400")
          else if (isRemoved) (cls := "text-red-500 dark:text-red-400")
          else VDomModifier.empty,
        ),
      },
    )

  }

  def showControlChars(str: String) = str
    .replaceAll("\n", "¶\n")
    .replaceAll(" ", "·")

}
