import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import Surface as S
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
    assertEquals(idSync, result.core)
    assertEquals(V.Unit, result.type)
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
    assertEquals(idDesync1, result.core)
    assertEquals(V.Unit, result.type)
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
    assertEquals(idDesync2, result.core)
    assertEquals(V.Unit, result.type)
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
    assertEquals(idDesync3, result.core)
    assertEquals(V.Unit, result.type)
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
    assertEquals(idSyncPartial, result.core)
    assertEquals(Π("a", UnitC, UnitC), Lvl(0).quote(result.type))
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
    assertEquals(idDesync1Partial, result.core)
    assertEquals(Π("a", UnitC, UnitC), Lvl(0).quote(result.type))
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
    assertEquals(idDesync2Partial, result.core)
    assertEquals(Π(UnitC, UnitC), Lvl(0).quote(result.type))
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
    assertEquals(idDesync3Partial, result.core)
    assertEquals(Π(UnitC, UnitC), Lvl(0).quote(result.type))
  }

  @Test
  fun illTypedFuncOf() {
    assertThrows<IllegalStateException> {
      Ctx().elaborate(
        λ("x", v("x")) of S.Type,
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
