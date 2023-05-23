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
        λ("A", λ("a", v("a"))) of Π("A", Us, Π("a", v("A"), v("A"))),
        v("id")(Us)(Us),
      ),
      null,
    )
    assertEquals(
      run {
        val A = v(1, Uc)
        val AA = Π("a", v(0, Uc), A)
        val UAA = Π("A", Uc, AA)
        val UU = Π("a", Uc, Uc)
        let(
          "id",
          λ("A", λ("a", v(0, of = A), of = AA), of = UAA),
          v(0, of = UAA)(Uc, of = UU)(Uc, of = Uc),
        )
      },
      result.core,
    )
    assertEquals(
      V.Univ,
      result.type,
    )
  }

  @Test
  fun illTypedFuncOf() {
    assertThrows<IllegalStateException> {
      Ctx().elaborate(
        λ("x", v("x")) of S.Univ,
        null,
      )
    }
  }

  @Test
  fun illTypedApp() {
    assertThrows<IllegalStateException> {
      Ctx().elaborate(
        Us(Us),
        null,
      )
    }
  }
}
