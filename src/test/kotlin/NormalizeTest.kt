import kotlin.test.Test
import kotlin.test.assertEquals
import Core as C

object NormalizeTest {
  private fun test(
    expected: C.Term,
    actual: C.Term,
  ) {
    assertEquals(
      expected,
      emptyEnv().normalize(actual),
    )
  }

  @Test
  fun unit() {
    test(unitC, Programs.unit)
  }

  @Test
  fun pair() {
    test(UnitC.to(unitC, of = Σ(TypeC, UnitC)), Programs.pair)
  }

  @Test
  fun pairNested() {
    test(UnitC, Programs.pairNested)
  }

  @Test
  fun idSync() {
    test(unitC, Programs.idSync)
  }

  @Test
  fun idUncurry() {
    test(unitC, Programs.idUncurry)
  }
}
