package webapp

import outwatch._
import outwatch.dsl._
import typings.diff.mod._

// https://github.com/kpdecker/jsdiff

object Diff {

  def apply(original: String, result: String) = {
    p(
      diffWordsWithSpace(original, result).map { part =>
        val isAdded   = part.added.getOrElse(false)
        val isRemoved = part.removed.getOrElse(false)
        span(
          // showControlChars(part.value),
          part.value,
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
