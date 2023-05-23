import org.junit.jupiter.api.Disabled
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
    assertEquals(
      run {
        val A = v(1, TypeC)
        val AA = Π("a", v(0, TypeC), A)
        val TAA = Π("A", TypeC, AA)
        val UU = Π("a", UnitC, UnitC)
        let(
          "id",
          λ("A", λ("a", v(0, of = A), of = AA), of = TAA),
          v(0, of = TAA)(UnitC, of = UU)(unitC, of = UnitC),
        )
      },
      result.core,
    )
    assertEquals(
      V.Unit,
      result.type,
    )
  }

  @Disabled
  @Test
  fun idDesync() {
    val result = Ctx().elaborate(
      let(
        "id",
        λ(λ("a", v("a"))) of Π("A", TypeS, Π(v("A"), v("A"))),
        v("id")(UnitS)(unitS),
      ),
      null,
    )
    assertEquals(
      run {
        val A = v(1, TypeC)
        val AA = Π(v(0, TypeC), A)
        val TAA = Π("A", TypeC, AA)
        val UU = Π(UnitC, UnitC)
        let(
          "id",
          λ(λ("a", v(0, of = A), of = AA), of = TAA),
          v(0, of = TAA)(UnitC, of = UU)(unitC, of = UnitC),
        )
      },
      result.core,
    )
    assertEquals(
      V.Unit,
      result.type,
    )
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
