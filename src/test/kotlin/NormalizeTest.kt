import kotlin.test.Test
import kotlin.test.assertEquals

object NormalizeTest {
  @Test
  fun idSync() {
    assertEquals(
      unitC,
      emptyEnv().normalize(idSync),
    )
  }

  @Test
  fun idDesync1() {
    assertEquals(
      unitC,
      emptyEnv().normalize(idDesync1),
    )
  }

  @Test
  fun idDesync2() {
    assertEquals(
      unitC,
      emptyEnv().normalize(idDesync2),
    )
  }

  @Test
  fun idDesync3() {
    assertEquals(
      unitC,
      emptyEnv().normalize(idDesync3),
    )
  }

  @Test
  fun idSyncPartial() {
    assertEquals(
      λ("a", v(0, of = UnitC), of = Π("a", UnitC, UnitC)),
      emptyEnv().normalize(idSyncPartial),
    )
  }

  @Test
  fun idDesync1Partial() {
    assertEquals(
      λ("a", v(0, of = UnitC), of = Π("a", UnitC, UnitC)),
      emptyEnv().normalize(idDesync1Partial),
    )
  }

  @Test
  fun idDesync2Partial() {
    assertEquals(
      λ("a", v(0, of = UnitC), of = Π(UnitC, UnitC)),
      emptyEnv().normalize(idDesync2Partial),
    )
  }

  @Test
  fun idDesync3Partial() {
    assertEquals(
      λ("a", v(0, of = UnitC), of = Π(UnitC, UnitC)),
      emptyEnv().normalize(idDesync3Partial),
    )
  }
}
