import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import Core as C

object ElaborateTest {
  private fun test(
    term: C.Term?,
    text: String,
  ) {
    val result = emptyCtx().elaborateTerm(Parse(text), null)
    term?.let { assertEquals(it, result.element) }
  }

  @Test
  fun unit() {
    test(
      Programs.unit,
      """
        let () = ();
        ()
      """.trimIndent(),
    )
  }

  @Test
  fun pair() {
    test(
      Programs.pair,
      """
        let (a, b) = (Unit, ());
        (a, b)
      """.trimIndent(),
    )
  }

  @Test
  fun pairNested() {
    test(
      Programs.pairNested,
      """
        let (_, (b, _)) = (Type, (Unit, ()));
        b
      """.trimIndent(),
    )
  }

  @Test
  fun idSync() {
    test(
      Programs.idSync,
      """
        let id = (λA. λa. a : Π(A : Type). Π(a : A). A);
        id Unit ()
      """.trimIndent(),
    )
  }

  @Test
  fun idUncurry() {
    test(
      Programs.idUncurry,
      """
        let id = (λP. (P.2) : Π(P : Σ(A : Type). A). (P.1));
        id (Unit, ())
      """.trimIndent(),
    )
  }

  @Test
  fun idUncurryPattern() {
    test(
      Programs.idUncurryPattern,
      """
        let id = (λ(_, a). a : Π((A, _) : Σ(A : Type). A). A);
        id (Unit, ())
      """.trimIndent(),
    )
  }

  @Test
  fun idConst() {
    test(
      Programs.idConst,
      """
        let id = (λ_. λa. a : Π(A : Type). ΠA. A);
        let const = (λ_. λ_. λa. λb. a : Π(A : Type). Π(B : Type). ΠA. ΠB. A);
        id (Π(A : Type). Π(B : Type). ΠA. ΠB. A) const
      """.trimIndent(),
    )
  }

  @Test
  fun illTypedFuncOf() {
    assertThrows<IllegalStateException> {
      test(
        null,
        """
          (λx. x : Type)
        """.trimIndent(),
      )
    }
  }

  @Test
  fun illTypedApp() {
    assertThrows<IllegalStateException> {
      test(
        null,
        """
          Type Type
        """.trimIndent(),
      )
    }
  }
}
