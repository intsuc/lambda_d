import kotlin.test.Test
import kotlin.test.assertEquals

object ParseTest {
  @Test
  fun idSync() {
    assertEquals(
      let(
        "id",
        λ("A", λ("a", v("a"))) of Π("A", TypeS, Π("a", v("A"), v("A"))),
        v("id")(UnitS)(unitS),
      ),
      Parse("""
        let id = (λA. λa. a : Π(A : Type). Π(a : A). A); id Unit ()
      """.trimIndent()),
    )
  }

  @Test
  fun idDesync() {
    assertEquals(
      let(
        "id",
        λ(λ("a", v("a"))) of Π("A", TypeS, Π(v("A"), v("A"))),
        v("id")(UnitS)(unitS),
      ),
      Parse("""
        let id = (λ. λa. a : Π(A : Type). ΠA. A); id Unit ()
      """.trimIndent()),
    )
  }

  @Test
  fun idConst() {
    assertEquals(
      let(
        "id",
        λ(λ("a", v("a"))) of Π("A", TypeS, Π(v("A"), v("A"))),
        let(
          "const",
          λ("A", λ("B", λ("a", λ("b", v("a"))))) of Π("A", TypeS, Π("B", TypeS, Π(v("A"), Π(v("B"), v("A"))))),
          v("id")(Π("A", TypeS, Π("B", TypeS, Π(v("A"), Π(v("B"), v("A"))))))(v("const")),
        )
      ),
      Parse("""
        let id = (λ. λa. a : Π(A : Type). ΠA. A);
        let const = (λA. λB. λa. λb. a : Π(A : Type). Π(B : Type). ΠA. ΠB. A);
        id (Π(A : Type). Π(B : Type). ΠA. ΠB. A) const
      """.trimIndent())
    )
  }
}
