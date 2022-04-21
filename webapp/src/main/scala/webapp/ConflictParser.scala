package webapp

import cats.effect.IO
import cats.effect.SyncIO
import colibri.Subject
import org.scalajs.dom.console
import outwatch._
import outwatch.dsl._
import typings.diffMatchPatch.mod._

import scala.scalajs.js

import scalajs.js.JSStringOps.enableJSStringOps

object ConflictParser {
  /*
<<<<<<< ours
  puts 'hola world'
||||||| base
  puts 'hello world'
=======
  puts 'hello mundo'
>>>>>>> theirs
   */

  val pattern = new js.RegExp(
    raw"""^.*<<<<<<< (.*)
([\s\S]*)\|\|\|\|\|\|\| (.*)
([\s\S]*)=======
([\s\S]*)>>>>>>> (.*)$$""",
    "m",
  )

  def apply(str: String) = scala.util.Try {
    val matches = str.`match`(pattern)
    Conflict(
      base = matches(4),
      baseName = matches(3),
      a = matches(2),
      aName = matches(1),
      b = matches(5),
      bName = matches(6),
    )
  }.toEither.left.map(_ => new Exception("Could not parse git conflict"))

}
