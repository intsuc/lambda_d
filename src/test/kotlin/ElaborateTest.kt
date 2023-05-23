import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import Core as C
import Surface as S
import Value as V

object ElaborateTest {
  @Test
  fun id() {
    val result = emptyCtx().elaborate(
      let(
        "id",
        λ("A", λ("a", !"a")) of Π("A", S.Type, Π("a", !"A", !"A")),
        (!"id")(S.Type)(S.Type),
      ),
      null,
    )
    assertEquals(
      let(
        "id",
        λ("A", λ("a", !1)),
        (!0)(C.Type)(C.Type),
      ),
      result.core,
    )
    assertEquals(
      V.Type,
      result.type,
    )
  }

  @Test
  fun illTypedFuncOf() {
    assertThrows<IllegalStateException> {
      emptyCtx().elaborate(
        λ("x", !"x") of S.Type,
        null,
      )
    }
  }

  @Test
  fun illTypedApp() {
    assertThrows<IllegalStateException> {
      emptyCtx().elaborate(
        S.Type(S.Type),
        null,
      )
    }
  }
}
