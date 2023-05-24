val idSync: Core = run {
  val A = v(1, TypeC)
  val AA = Π("a", v(0, TypeC), A)
  val TAA = Π("A", TypeC, AA)
  val UU = Π("a", UnitC, UnitC)
  let(
    "id",
    λ("A", λ("a", v(0, of = A), of = AA), of = TAA),
    v(0, of = TAA)(UnitC, of = UU)(unitC, of = UnitC),
  )
}

val idDesync1: Core = run {
  val A = v(1, TypeC)
  val AA = Π("a", v(0, TypeC), A)
  val TAA = Π("A", TypeC, AA)
  val UU = Π("a", UnitC, UnitC)
  let(
    "id",
    λ(λ("a", v(0, of = A), of = AA), of = TAA),
    v(0, of = TAA)(UnitC, of = UU)(unitC, of = UnitC),
  )
}

val idDesync2: Core = run {
  val A = v(0, TypeC)
  val AA = Π( v(0, TypeC), A)
  val TAA = Π("A", TypeC, AA)
  val UU = Π( UnitC, UnitC)
  let(
    "id",
    λ("A", λ("a", v(0, of = A), of = AA), of = TAA),
    v(0, of = TAA)(UnitC, of = UU)(unitC, of = UnitC),
  )
}

val idDesync3: Core = run {
  val A = v(0, TypeC)
  val AA = Π(v(0, TypeC), A)
  val TAA = Π("A", TypeC, AA)
  val UU = Π(UnitC, UnitC)
  let(
    "id",
    λ(λ("a", v(0, of = A), of = AA), of = TAA),
    v(0, of = TAA)(UnitC, of = UU)(unitC, of = UnitC),
  )
}
