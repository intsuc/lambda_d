import kotlin.test.Test
import kotlin.test.assertEquals

object NormalizeTest {
  @Test
  fun idSync() {
    assertEquals(
      unitC,
      emptyEnv ().normalize(idSync),
    )
  }

  @Test
  fun idDesync1() {
    assertEquals(
      unitC,
      emptyEnv ().normalize(idDesync1),
    )
  }

  @Test
  fun idDesync2() {
    assertEquals(
      unitC,
      emptyEnv ().normalize(idDesync2),
    )
  }
}
