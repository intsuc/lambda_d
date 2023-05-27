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
}
