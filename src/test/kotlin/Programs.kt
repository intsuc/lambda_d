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
  val AA = Π(v(0, TypeC), A)
  val TAA = Π("A", TypeC, AA)
  val UU = Π(UnitC, UnitC)
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

val idSyncPartial: Core = run {
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

val idDesync1Partial: Core = run {
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

val idDesync2Partial: Core = run {
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

val idDesync3Partial: Core = run {
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

val idSyncPartial1: Core = run {
  val A = v(1, TypeC)
  val AA = Π("a", v(0, TypeC), A)
  val TAA = Π("A", TypeC, AA)
  let(
    "id",
    λ("A", λ("a", v(0, of = A), of = AA), of = TAA),
    v(0, of = TAA),
  )
}

val idDesync1Partial1: Core = run {
  val A = v(1, TypeC)
  val AA = Π("a", v(0, TypeC), A)
  val TAA = Π("A", TypeC, AA)
  let(
    "id",
    λ(λ("a", v(0, of = A), of = AA), of = TAA),
    v(0, of = TAA),
  )
}

val idDesync2Partial1: Core = run {
  val A = v(0, TypeC)
  val AA = Π(v(0, TypeC), A)
  val TAA = Π("A", TypeC, AA)
  let(
    "id",
    λ("A", λ("a", v(0, of = A), of = AA), of = TAA),
    v(0, of = TAA),
  )
}

val idDesync3Partial1: Core = run {
  val A = v(0, TypeC)
  val AA = Π(v(0, TypeC), A)
  val TAA = Π("A", TypeC, AA)
  let(
    "id",
    λ(λ("a", v(0, of = A), of = AA), of = TAA),
    v(0, of = TAA),
  )
}

val idConst: Core = run {
  val A0 = v(0, of = TypeC)
  val A1 = v(1, of = TypeC)
  val B0 = v(0, of = TypeC)
  val AA = Π(A0, A0)
  val TAA = Π("A", TypeC, AA)
  val BA = Π(B0, A1)
  val ABA = Π(A1, BA)
  val TABA = Π("B", TypeC, ABA)
  val TTABA = Π("A", TypeC, TABA)
  val TTABATTABA = Π(TTABA, TTABA)
  val a0 = v(0, of = A0)
  val a1 = v(1, of = A1)
  val id = v(1, of = TAA)
  val const = v(0, of = TTABA)
  let(
    "id",
    λ("A", λ("a", a0, of = AA), of = TAA),
    let(
      "const",
      λ("A", λ("B", λ("a", λ("b", a1, of = BA), of = ABA), of = TABA), of = TTABA),
      id(TTABA, of = TTABATTABA)(const, of = TTABA),
    )
  )
}
