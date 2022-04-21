package webapp

import outwatch._
import outwatch.dsl._
import typings.diff.mod.diffChars
import typings.diff.mod.diffWords

object Diff {
  def apply(original: String, result: String) = {
    p(
      diffWords(original, result)
        .map(part =>
          span(
            part.value,
            cls := (if (part.added.getOrElse(false)) "text-green-400"
                    else if (part.removed.getOrElse(false)) "text-red-400"
                    else ""),
          ),
        ),
    )

  }
}
