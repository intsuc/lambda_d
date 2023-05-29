import kotlin.test.Test
import kotlin.test.assertEquals

object ParseTest {
  @Test
  fun idSync() {
    assertEquals(
      let(
        -"id",
        λ(-"A", λ(-"a", +"a")) of Π(-"A", TypeS, Π(-"a", +"A", +"A")),
        (+"id")(UnitS)(unitS),
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
        -"id",
        λ(λ(-"a", +"a")) of Π(-"A", TypeS, Π(+"A", +"A")),
        (+"id")(UnitS)(unitS),
      ),
      Parse("""
        let id = (λ_. λa. a : Π(A : Type). ΠA. A); id Unit ()
      """.trimIndent()),
    )
  }

  @Test
  fun idConst() {
    assertEquals(
      let(
        -"id",
        λ(λ(-"a", +"a")) of Π(-"A", TypeS, Π(+"A", +"A")),
        let(
          -"const",
          λ(-"A", λ(-"B", λ(-"a", λ(-"b", +"a")))) of Π(-"A", TypeS, Π(-"B", TypeS, Π(+"A", Π(+"B", +"A")))),
          (+"id")(Π(-"A", TypeS, Π(-"B", TypeS, Π(+"A", Π(+"B", +"A")))))(+"const"),
        )
      ),
      Parse("""
        let id = (λ_. λa. a : Π(A : Type). ΠA. A);
        let const = (λA. λB. λa. λb. a : Π(A : Type). Π(B : Type). ΠA. ΠB. A);
        id (Π(A : Type). Π(B : Type). ΠA. ΠB. A) const
      """.trimIndent())
    )
  }
}
