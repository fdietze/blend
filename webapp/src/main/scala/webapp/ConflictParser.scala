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

  val c = new js.RegExp(
    raw"""^.*<<<<<<< .*
(.*)
\|\|\|\|\|\|\| .*
(.*)
=======
(.*)
>>>>>>> .*$$""",
    "m",
  )

  def apply(str: String) = scala.util.Try {
    val matches = str.`match`(c)
    Conflict(base = matches(2), a = matches(1), b = matches(3))
  }

}
