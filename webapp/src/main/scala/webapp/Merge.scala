package webapp

import outwatch._
import outwatch.dsl._
import typings.diffMatchPatch.mod._

object Merge {
  val dmp = new diffMatchPatch();

  enum Error {
    case PatchOrderRelevant(merged1: String, merged2: String)
    case ChangeNotPreserved(conflict: Conflict, merged: String)
  }

  def merge(conflict: Conflict): Either[Error, String] = {

    val patchesA = dmp.patch_make(conflict.base, conflict.a)
    val patchesB = dmp.patch_make(conflict.base, conflict.b)
    val merged1  = dmp.patch_apply(patchesA ++ patchesB, conflict.base)(0)
    val merged2  = dmp.patch_apply(patchesB ++ patchesA, conflict.base)(0)

    val patchOrderRelevant = merged1 != merged2
    if (patchOrderRelevant) return Left(Error.PatchOrderRelevant(merged1, merged2))

    val merged = merged1

    val diffBaseToA        = diffChanges(conflict.base, conflict.a)
    val diffBToMerged      = diffChanges(conflict.b, merged)
    val changeAisPreserved = diffBaseToA == diffBToMerged
    println(diffBaseToA)
    println(diffBToMerged)
    println(changeAisPreserved)
    if (!changeAisPreserved)
      return Left(Error.ChangeNotPreserved(conflict, merged))

    val diffBaseToB        = diffChanges(conflict.base, conflict.b)
    val diffAToMerged      = diffChanges(conflict.a, merged)
    val changeBisPreserved = diffBaseToB == diffAToMerged
    if (!changeBisPreserved)
      return Left(Error.ChangeNotPreserved(conflict.flipped, merged))

    Right(merged)
  }

  enum Change {
    case Insert(str: String)
    case Delete(str: String)
  }

  def diffChanges(original: String, target: String): Vector[Change] = {

    val diff = dmp.diff_main(original, target)
    dmp.diff_cleanupSemantic(diff) // TODO: necessary?

    diff.view.collect {
      case part if part(0) == 1  => Change.Insert(part(1))
      case part if part(0) == -1 => Change.Delete(part(1))
    }.toVector
  }
}
