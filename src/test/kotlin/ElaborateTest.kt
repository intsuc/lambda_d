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
