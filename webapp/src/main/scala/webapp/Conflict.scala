package webapp

case class Conflict(
  base: String,
  baseName: String,
  a: String,
  aName: String,
  b: String,
  bName: String,
) {
  def flipped: Conflict = {
    Conflict(
      base,
      baseName,
      a = b,
      aName = bName,
      b = a,
      bName = aName,
    )
  }
}
