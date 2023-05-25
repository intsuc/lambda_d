val idSync: Core.Term = run {
  val A0 = v("A", 0, TypeC)
  val A1 = v("A", 1, TypeC)
  val AA = Π(vP("a", of = A0), A0, A1)
  val TAA = Π(vP("A", of = TypeC), TypeC, AA)
  val UU = Π(vP("a", of = UnitC), UnitC, UnitC)
  let(
    vP("id", of = TAA),
    λ(vP("A", of = TypeC), λ(vP("a", of = A0), v("a", 0, of = A1), of = AA), of = TAA),
    v("id", 0, of = TAA)(UnitC, of = UU)(unitC, of = UnitC),
  )
}

/*
val idDesync1: Core.Term = run {
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

val idDesync2: Core.Term = run {
  val A = v(0, TypeC)
  val AA = Π(v(0, TypeC), A)
  val TAA = Π("A", TypeC, AA)
  val UU = Π(UnitC, UnitC)
  let(
    "id",
    λ("A", λ("a", v(0, of = A), of = AA), of = TAA),
    v(0, of = TAA)(UnitC, of = UU)(unitC, of = UnitC),
  )
}

val idDesync3: Core.Term = run {
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

val idSyncPartial: Core.Term = run {
  val A = v(1, TypeC)
  val AA = Π("a", v(0, TypeC), A)
  val TAA = Π("A", TypeC, AA)
  val UU = Π("a", UnitC, UnitC)
  let(
    "id",
    λ("A", λ("a", v(0, of = A), of = AA), of = TAA),
    v(0, of = TAA)(UnitC, of = UU),
  )
}

val idDesync1Partial: Core.Term = run {
  val A = v(1, TypeC)
  val AA = Π("a", v(0, TypeC), A)
  val TAA = Π("A", TypeC, AA)
  val UU = Π("a", UnitC, UnitC)
  let(
    "id",
    λ(λ("a", v(0, of = A), of = AA), of = TAA),
    v(0, of = TAA)(UnitC, of = UU),
  )
}

val idDesync2Partial: Core.Term = run {
  val A = v(0, TypeC)
  val AA = Π(v(0, TypeC), A)
  val TAA = Π("A", TypeC, AA)
  val UU = Π(UnitC, UnitC)
  let(
    "id",
    λ("A", λ("a", v(0, of = A), of = AA), of = TAA),
    v(0, of = TAA)(UnitC, of = UU),
  )
}

val idDesync3Partial: Core.Term = run {
  val A = v(0, TypeC)
  val AA = Π(v(0, TypeC), A)
  val TAA = Π("A", TypeC, AA)
  val UU = Π(UnitC, UnitC)
  let(
    "id",
    λ(λ("a", v(0, of = A), of = AA), of = TAA),
    v(0, of = TAA)(UnitC, of = UU),
  )
}

val idSyncPartial1: Core.Term = run {
  val A = v(1, TypeC)
  val AA = Π("a", v(0, TypeC), A)
  val TAA = Π("A", TypeC, AA)
  let(
    "id",
    λ("A", λ("a", v(0, of = A), of = AA), of = TAA),
    v(0, of = TAA),
  )
}

val idDesync1Partial1: Core.Term = run {
  val A = v(1, TypeC)
  val AA = Π("a", v(0, TypeC), A)
  val TAA = Π("A", TypeC, AA)
  let(
    "id",
    λ(λ("a", v(0, of = A), of = AA), of = TAA),
    v(0, of = TAA),
  )
}

val idDesync2Partial1: Core.Term = run {
  val A = v(0, TypeC)
  val AA = Π(v(0, TypeC), A)
  val TAA = Π("A", TypeC, AA)
  let(
    "id",
    λ("A", λ("a", v(0, of = A), of = AA), of = TAA),
    v(0, of = TAA),
  )
}

val idDesync3Partial1: Core.Term = run {
  val A = v(0, TypeC)
  val AA = Π(v(0, TypeC), A)
  val TAA = Π("A", TypeC, AA)
  let(
    "id",
    λ(λ("a", v(0, of = A), of = AA), of = TAA),
    v(0, of = TAA),
  )
}
*/
