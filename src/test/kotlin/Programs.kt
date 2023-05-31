import Core.Term

object Programs {
  val unit: Term = run {
    let(
      unitCp,
      unitC,
      unitC,
    )
  }

  val pair: Term = run {
    val T = Σ(TypeC, UnitC)
    val p = v(0, T)
    let(
      vp("a").to(vp("b")),
      UnitC.to(unitC, of = T),
      p.first(of = TypeC).to(p.second(of = UnitC), of = T),
    )
  }

  val pairNested: Term = run {
    val TU = Σ(TypeC, UnitC)
    val TTU = Σ(TypeC, TU)
    let(
      _p().to(vp("b").to(_p())),
      TypeC.to(UnitC.to(unitC, of = TU), of = TTU),
      v(0, of = TTU).second(of = TU).first(of = TypeC),
    )
  }

  val idSync: Term = run {
    val A0 = v(0, TypeC)
    val A1 = v(1, TypeC)
    val AA = Π(v(0, TypeC), A1)
    val TAA = Π(TypeC, AA)
    val UU = Π(UnitC, UnitC)
    let(
      vp("id"),
      λ(vp("A"), λ(vp("a"), v(0, of = A1), of = AA), of = TAA),
      v(0, of = TAA)(UnitC, of = UU)(unitC, of = UnitC),
    )
  }

  val idUncurry: Term = run {
    val TA = Σ(TypeC, v(0, of = TypeC))
    val TAA = Π(TA, v(0, of = TA).first(of = TypeC))
    val TU = Σ(TypeC, UnitC)
    let(
      vp("id"),
      λ(vp("P"), v(0, of = TA).second(of = v(0, of = TA).first(of = TypeC)), of = TAA),
      v(0, of = TAA)(UnitC.to(unitC, of = TU), of = UnitC)
    )
  }

  val idUncurryPattern: Term = run {
    val TA = Σ(TypeC, v(0, of = TypeC))
    val TAA = Π(TA, v(0, of = TA).first(of = TypeC))
    val TU = Σ(TypeC, UnitC)
    let(
      vp("id"),
      λ(_p().to(vp("a")), v(0, of = TA).second(of = v(0, of = TA).first(of = TypeC)), of = TAA),
      v(0, of = TAA)(UnitC.to(unitC, of = TU), of = UnitC)
    )
  }

  val idConst: Term = run {
    val A0 = v(0, of = TypeC)
    val A1 = v(1, of = TypeC)
    val A3 = v(3, of = TypeC)
    val B1 = v(1, of = TypeC)
    val AA = Π(A0, A1)
    val TAA = Π(TypeC, AA)
    val BA = Π(B1, A3)
    val ABA = Π(A1, BA)
    val TABA = Π(TypeC, ABA)
    val TTABA = Π(TypeC, TABA)
    val TTABATTABA = Π(TTABA, TTABA)
    val a0 = v(0, of = A1)
    val a1 = v(1, of = A3)
    val id = v(1, of = TAA)
    val const = v(0, of = TTABA)
    let(
      vp("id"),
      λ(_p(), λ(vp("a"), a0, of = AA), of = TAA),
      let(
        vp("const"),
        λ(_p(), λ(_p(), λ(vp("a"), λ(vp("b"), a1, of = BA), of = ABA), of = TABA), of = TTABA),
        id(TTABA, of = TTABATTABA)(const, of = TTABA),
      )
    )
  }
}
