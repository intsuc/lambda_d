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
      result.core,
      let(
        λ(λ(!1)),
        (!0)(C.Type)(C.Type),
      ),
    )
    assertEquals(
      result.type,
      V.Type,
    )
  }
}
