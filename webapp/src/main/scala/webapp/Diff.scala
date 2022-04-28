package webapp

import outwatch._
import outwatch.dsl._
import typings.diff.mod._

// https://github.com/kpdecker/jsdiff

object Diff {
  def apply(original: String, result: String) = {
    p(
      diffWordsWithSpace(original, result)
        .map(part =>
          span(
            part.value,
            cls := (if (part.added.getOrElse(false)) "text-green-500 dark:text-green-400"
                    else if (part.removed.getOrElse(false)) "text-red-500 dark:text-red-400"
                    else ""),
          ),
        ),
    )

  }
}
