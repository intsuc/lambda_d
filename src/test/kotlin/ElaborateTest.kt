import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import Value as V

object ElaborateTest {
  @Test
  fun idSync() {
    val result = Ctx().elaborateTerm(
      let(
        vP("id") of Π(vP("A"), TypeS, Π(vP("a"), v("A"), v("A"))),
        λ(vP("A"), λ(vP("a"), v("a"))),
        v("id")(UnitS)(unitS),
      ),
      null,
    )
    assertEquals(idSync, result.value)
    assertEquals(V.Term.Unit, result.type)
  }

  /*
  @Test
  fun idDesync1() {
    val result = Ctx().elaborateTerm(
      let(
        "id",
        λ(λ("a", v("a"))) of Π("A", TypeS, Π("a", v("A"), v("A"))),
        v("id")(UnitS)(unitS),
      ),
      null,
    )
    assertEquals(idDesync1, result.value)
    assertEquals(V.Unit, result.type)
  }

  @Test
  fun idDesync2() {
    val result = Ctx().elaborateTerm(
      let(
        "id",
        λ("A", λ("a", v("a"))) of Π("A", TypeS, Π(v("A"), v("A"))),
        v("id")(UnitS)(unitS),
      ),
      null,
    )
    assertEquals(idDesync2, result.value)
    assertEquals(V.Unit, result.type)
  }

  @Test
  fun idDesync3() {
    val result = Ctx().elaborateTerm(
      let(
        "id",
        λ(λ("a", v("a"))) of Π("A", TypeS, Π(v("A"), v("A"))),
        v("id")(UnitS)(unitS),
      ),
      null,
    )
    assertEquals(idDesync3, result.value)
    assertEquals(V.Unit, result.type)
  }

  @Test
  fun idSyncPartial() {
    val result = Ctx().elaborateTerm(
      let(
        "id",
        λ("A", λ("a", v("a"))) of Π("A", TypeS, Π("a", v("A"), v("A"))),
        v("id")(UnitS),
      ),
      null,
    )
    assertEquals(idSyncPartial, result.value)
    assertEquals(Π("a", UnitC, UnitC), quoteTerm(Lvl(0), Lvl(0), result.type))
  }

  @Test
  fun idDesync1Partial() {
    val result = Ctx().elaborateTerm(
      let(
        "id",
        λ(λ("a", v("a"))) of Π("A", TypeS, Π("a", v("A"), v("A"))),
        v("id")(UnitS),
      ),
      null,
    )
    assertEquals(idDesync1Partial, result.value)
    assertEquals(Π("a", UnitC, UnitC), quoteTerm(Lvl(0), Lvl(0), result.type))
  }

  @Test
  fun idDesync2Partial() {
    val result = Ctx().elaborateTerm(
      let(
        "id",
        λ("A", λ("a", v("a"))) of Π("A", TypeS, Π(v("A"), v("A"))),
        v("id")(UnitS),
      ),
      null,
    )
    assertEquals(idDesync2Partial, result.value)
    assertEquals(Π(UnitC, UnitC), quoteTerm(Lvl(0), Lvl(0), result.type))
  }

  @Test
  fun idDesync3Partial() {
    val result = Ctx().elaborateTerm(
      let(
        "id",
        λ(λ("a", v("a"))) of Π("A", TypeS, Π(v("A"), v("A"))),
        v("id")(UnitS),
      ),
      null,
    )
    assertEquals(idDesync3Partial, result.value)
    assertEquals(Π(UnitC, UnitC), quoteTerm(Lvl(0), Lvl(0), result.type))
  }

  @Test
  fun idSyncPartial1() {
    val result = Ctx().elaborateTerm(
      let(
        "id",
        λ("A", λ("a", v("a"))) of Π("A", TypeS, Π("a", v("A"), v("A"))),
        v("id"),
      ),
      null,
    )
    assertEquals(idSyncPartial1, result.value)
    assertEquals(Π("A", TypeC, Π("a", v(0, TypeC), v(1, TypeC))), quoteTerm(Lvl(0), Lvl(0), result.type))
  }

  @Test
  fun idDesync1Partial1() {
    val result = Ctx().elaborateTerm(
      let(
        "id",
        λ(λ("a", v("a"))) of Π("A", TypeS, Π("a", v("A"), v("A"))),
        v("id"),
      ),
      null,
    )
    assertEquals(idDesync1Partial1, result.value)
    assertEquals(Π("A", TypeC, Π("a", v(0, TypeC), v(1, TypeC))), quoteTerm(Lvl(0), Lvl(0), result.type))
  }

  @Test
  fun idDesync2Partial1() {
    val result = Ctx().elaborateTerm(
      let(
        "id",
        λ("A", λ("a", v("a"))) of Π("A", TypeS, Π(v("A"), v("A"))),
        v("id"),
      ),
      null,
    )
    assertEquals(idDesync2Partial1, result.value)
    assertEquals(Π("A", TypeC, Π(v(0, TypeC), v(0, TypeC))), quoteTerm(Lvl(0), Lvl(0), result.type))
  }

  @Test
  fun idDesync3Partial1() {
    val result = Ctx().elaborateTerm(
      let(
        "id",
        λ(λ("a", v("a"))) of Π("A", TypeS, Π(v("A"), v("A"))),
        v("id"),
      ),
      null,
    )
    assertEquals(idDesync3Partial1, result.value)
    assertEquals(Π("A", TypeC, Π(v(0, TypeC), v(0, TypeC))), quoteTerm(Lvl(0), Lvl(0), result.type))
  }

   */

  @Test
  fun illTypedFuncOf() {
    assertThrows<IllegalStateException> {
      Ctx().elaborateTerm(
        λ(vP("x"), v("x")) of TypeS,
        null,
      )
    }
  }

  @Test
  fun illTypedApp() {
    assertThrows<IllegalStateException> {
      Ctx().elaborateTerm(
        TypeS(TypeS),
        null,
      )
    }
  }
}
