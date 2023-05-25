import kotlin.test.Test
import kotlin.test.assertEquals

object NormalizeTest {
  @Test
  fun idSync() {
    assertEquals(
      unitC,
      emptyEnv().normalizeTerm(idSync),
    )
  }

  /*
  @Test
  fun idDesync1() {
    assertEquals(
      unitC,
      emptyEnv().normalizeTerm(idDesync1),
    )
  }

  @Test
  fun idDesync2() {
    assertEquals(
      unitC,
      emptyEnv().normalizeTerm(idDesync2),
    )
  }

  @Test
  fun idDesync3() {
    assertEquals(
      unitC,
      emptyEnv().normalizeTerm(idDesync3),
    )
  }

  @Test
  fun idSyncPartial() {
    assertEquals(
      λ("a", v(0, of = UnitC), of = Π("a", UnitC, UnitC)),
      emptyEnv().normalizeTerm(idSyncPartial),
    )
  }

  @Test
  fun idDesync1Partial() {
    assertEquals(
      λ("a", v(0, of = UnitC), of = Π("a", UnitC, UnitC)),
      emptyEnv().normalizeTerm(idDesync1Partial),
    )
  }

  @Test
  fun idDesync2Partial() {
    assertEquals(
      λ("a", v(0, of = UnitC), of = Π(UnitC, UnitC)),
      emptyEnv().normalizeTerm(idDesync2Partial),
    )
  }

  @Test
  fun idDesync3Partial() {
    assertEquals(
      λ("a", v(0, of = UnitC), of = Π(UnitC, UnitC)),
      emptyEnv().normalizeTerm(idDesync3Partial),
    )
  }

  @Test
  fun idSyncPartial1() {
    val A = v(1, TypeC)
    val AA = Π("a", v(0, TypeC), A)
    val TAA = Π("A", TypeC, AA)
    assertEquals(
      λ("A", λ("a", v(0, of = A), of = AA), of = TAA),
      emptyEnv().normalizeTerm(idSyncPartial1),
    )
  }

  @Test
  fun idDesync1Partial1() {
    val A = v(1, TypeC)
    val AA = Π("a", v(0, TypeC), A)
    val TAA = Π("A", TypeC, AA)
    assertEquals(
      λ(λ("a", v(0, of = A), of = AA), of = TAA),
      emptyEnv().normalizeTerm(idDesync1Partial1),
    )
  }

  @Test
  fun idDesync2Partial1() {
    val A = v(0, TypeC)
    val AA = Π(v(0, TypeC), A)
    val TAA = Π("A", TypeC, AA)
    assertEquals(
      λ("A", λ("a", v(0, of = A), of = AA), of = TAA),
      emptyEnv().normalizeTerm(idDesync2Partial1),
    )
  }

  @Test
  fun idDesync3Partial1() {
    val A = v(0, TypeC)
    val AA = Π(v(0, TypeC), A)
    val TAA = Π("A", TypeC, AA)
    assertEquals(
      λ(λ("a", v(0, of = A), of = AA), of = TAA),
      emptyEnv().normalizeTerm(idDesync3Partial1),
    )
  }
   */
}
