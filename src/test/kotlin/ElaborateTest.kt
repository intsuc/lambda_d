import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import Value as V

object ElaborateTest {
  private fun elaborate(text: String): Result {
    return Ctx().elaborate(Parse(text), null)
  }

  @Test
  fun idSync() {
    val result = elaborate("""
      let id = (λA. λa. a : Π(A : Type). Π(a : A). A);
      id Unit ()
    """.trimIndent())
    assertEquals(idSync, result.term)
    assertEquals(V.Term.Unit, result.type)
  }

  @Test
  fun idUncurry() {
    val result = elaborate("""
      let id = (λP. (P.2) : Π(P : Σ(A : Type). A). (P.1));
      id (Unit, ())
    """.trimIndent())
    assertEquals(idUncurry, result.term)
    assertEquals(V.Term.Unit, result.type)
  }

  @Test
  fun idConst() {
    val result = elaborate("""
      let id = (λ. λa. a : Π(A : Type). ΠA. A);
      let const = (λ. λ. λa. λb. a : Π(A : Type). Π(B : Type). ΠA. ΠB. A);
      id (Π(A : Type). Π(B : Type). ΠA. ΠB. A) const
    """.trimIndent())
    assertEquals(idConst, result.term)
  }

  @Test
  fun illTypedFuncOf() {
    assertThrows<IllegalStateException> {
      elaborate("""
        (λx. x : Type)
      """.trimIndent())
    }
  }

  @Test
  fun illTypedApp() {
    assertThrows<IllegalStateException> {
      elaborate("""
        Type Type
      """.trimIndent())
    }
  }
}
