package webapp

import org.scalajs.dom._
import outwatch._
import outwatch.dsl._
import org.scalatest.flatspec.AnyFlatSpec

import cats.effect.IO

class ConflictParserSpec extends AnyFlatSpec {

  it should "parse single line" in {
    assert(
      ConflictParser("""<<<<<<< ours
puts 'hola world'
||||||| base
puts 'hello world'
=======
puts 'hello mundo'
>>>>>>> theirs
""") === Right(
        Conflict("puts 'hello world'\n", "base", "puts 'hola world'\n", "ours", "puts 'hello mundo'\n", "theirs"),
      ),
    )
  }

  it should "parse without lines" in {
    assert(ConflictParser("""<<<<<<< ours
||||||| base
=======
>>>>>>> theirs
""") === Right(Conflict("", "base", "", "ours", "", "theirs")))
  }

  it should "parse empty lines" in {
    assert(ConflictParser("""<<<<<<< ours

||||||| base

=======

>>>>>>> theirs
""") === Right(Conflict("\n", "base", "\n", "ours", "\n", "theirs")))
  }
}
