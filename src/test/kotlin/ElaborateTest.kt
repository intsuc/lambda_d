import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import Value as V

object ElaborateTest {
  @Test
  fun idSync() {
    val result = Ctx().elaborate(
      let(
        "id",
        λ("A", λ("a", v("a"))) of Π("A", TypeS, Π("a", v("A"), v("A"))),
        v("id")(UnitS)(unitS),
      ),
      null,
    )
    assertEquals(idSync, result.term)
    assertEquals(V.Term.Unit, result.type)
  }

  @Test
  fun idDesync1() {
    val result = Ctx().elaborate(
      let(
        "id",
        λ(λ("a", v("a"))) of Π("A", TypeS, Π("a", v("A"), v("A"))),
        v("id")(UnitS)(unitS),
      ),
      null,
    )
    assertEquals(idDesync1, result.term)
    assertEquals(V.Term.Unit, result.type)
  }

  @Test
  fun idDesync2() {
    val result = Ctx().elaborate(
      let(
        "id",
        λ("A", λ("a", v("a"))) of Π("A", TypeS, Π(v("A"), v("A"))),
        v("id")(UnitS)(unitS),
      ),
      null,
    )
    assertEquals(idDesync2, result.term)
    assertEquals(V.Term.Unit, result.type)
  }

  @Test
  fun idDesync3() {
    val result = Ctx().elaborate(
      let(
        "id",
        λ(λ("a", v("a"))) of Π("A", TypeS, Π(v("A"), v("A"))),
        v("id")(UnitS)(unitS),
      ),
      null,
    )
    assertEquals(idDesync3, result.term)
    assertEquals(V.Term.Unit, result.type)
  }

  @Test
  fun idSyncPartial() {
    val result = Ctx().elaborate(
      let(
        "id",
        λ("A", λ("a", v("a"))) of Π("A", TypeS, Π("a", v("A"), v("A"))),
        v("id")(UnitS),
      ),
      null,
    )
    assertEquals(idSyncPartial, result.term)
    assertEquals(Π("a", UnitC, UnitC), Level(0).quote(result.type))
  }

  @Test
  fun idDesync1Partial() {
    val result = Ctx().elaborate(
      let(
        "id",
        λ(λ("a", v("a"))) of Π("A", TypeS, Π("a", v("A"), v("A"))),
        v("id")(UnitS),
      ),
      null,
    )
    assertEquals(idDesync1Partial, result.term)
    assertEquals(Π("a", UnitC, UnitC), Level(0).quote(result.type))
  }

  @Test
  fun idDesync2Partial() {
    val result = Ctx().elaborate(
      let(
        "id",
        λ("A", λ("a", v("a"))) of Π("A", TypeS, Π(v("A"), v("A"))),
        v("id")(UnitS),
      ),
      null,
    )
    assertEquals(idDesync2Partial, result.term)
    assertEquals(Π(UnitC, UnitC), Level(0).quote(result.type))
  }

  @Test
  fun idDesync3Partial() {
    val result = Ctx().elaborate(
      let(
        "id",
        λ(λ("a", v("a"))) of Π("A", TypeS, Π(v("A"), v("A"))),
        v("id")(UnitS),
      ),
      null,
    )
    assertEquals(idDesync3Partial, result.term)
    assertEquals(Π(UnitC, UnitC), Level(0).quote(result.type))
  }

  @Test
  fun idSyncPartial1() {
    val result = Ctx().elaborate(
      let(
        "id",
        λ("A", λ("a", v("a"))) of Π("A", TypeS, Π("a", v("A"), v("A"))),
        v("id"),
      ),
      null,
    )
    assertEquals(idSyncPartial1, result.term)
    assertEquals(Π("A", TypeC, Π("a", v(0, TypeC), v(1, TypeC))), Level(0).quote(result.type))
  }

  @Test
  fun idDesync1Partial1() {
    val result = Ctx().elaborate(
      let(
        "id",
        λ(λ("a", v("a"))) of Π("A", TypeS, Π("a", v("A"), v("A"))),
        v("id"),
      ),
      null,
    )
    assertEquals(idDesync1Partial1, result.term)
    assertEquals(Π("A", TypeC, Π("a", v(0, TypeC), v(1, TypeC))), Level(0).quote(result.type))
  }

  @Test
  fun idDesync2Partial1() {
    val result = Ctx().elaborate(
      let(
        "id",
        λ("A", λ("a", v("a"))) of Π("A", TypeS, Π(v("A"), v("A"))),
        v("id"),
      ),
      null,
    )
    assertEquals(idDesync2Partial1, result.term)
    assertEquals(Π("A", TypeC, Π(v(0, TypeC), v(1, TypeC))), Level(0).quote(result.type))
  }

  @Test
  fun idDesync3Partial1() {
    val result = Ctx().elaborate(
      let(
        "id",
        λ(λ("a", v("a"))) of Π("A", TypeS, Π(v("A"), v("A"))),
        v("id"),
      ),
      null,
    )
    assertEquals(idDesync3Partial1, result.term)
    assertEquals(Π("A", TypeC, Π(v(0, TypeC), v(1, TypeC))), Level(0).quote(result.type))
  }

  @Test
  fun idConst() {
    val result = Ctx().elaborate(
      let(
        "id",
        λ(λ("a", v("a"))) of Π("A", TypeS, Π(v("A"), v("A"))),
        let(
          "const",
          λ(λ(λ("a", λ("b", v("a"))))) of Π("A", TypeS, Π("B", TypeS, Π(v("A"), Π(v("B"), v("A"))))),
          v("id")(Π("A", TypeS, Π("B", TypeS, Π(v("A"), Π(v("B"), v("A"))))))(v("const")),
        )
      ),
      null,
    )
    assertEquals(idConst, result.term)
    // assertEquals(
    //   Π("A", TypeC, Π("B", TypeC, Π(v(1, of = TypeC), Π(v(0, of = TypeC), v(1, of = TypeC))))),
    //   Level(0).quote(result.type),
    // )
  }

  @Test
  fun illTypedFuncOf() {
    assertThrows<IllegalStateException> {
      Ctx().elaborate(
        λ("x", v("x")) of TypeS,
        null,
      )
    }
  }

  @Test
  fun illTypedApp() {
    assertThrows<IllegalStateException> {
      Ctx().elaborate(
        TypeS(TypeS),
        null,
      )
    }
  }
}
