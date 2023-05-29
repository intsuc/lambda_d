sealed class Surface {
  /**
   * A raw term.
   * May or may not be well-formed.
   */
  sealed class Term {
    data object Type : Term()

    data class Func(
      val binder: Pattern,
      val param: Term,
      val result: Term,
    ) : Term()

    data class FuncOf(
      val binder: Pattern,
      val result: Term,
    ) : Term()

    data class Apply(
      val func: Term,
      val arg: Term,
    ) : Term()

    data object Unit : Term()

    data object UnitOf : Term()

    data class Pair(
      val binder: Pattern,
      val first: Term,
      val second: Term,
    ) : Term()

    data class PairOf(
      val first: Term,
      val second: Term,
    ) : Term()

    data class First(
      val pair: Term,
    ) : Term()

    data class Second(
      val pair: Term,
    ) : Term()

    data class Let(
      val binder: Pattern,
      val init: Term,
      val body: Term,
    ) : Term()

    data class Var(
      val name: String,
    ) : Term()

    data class Anno(
      val target: Term,
      val type: Term,
    ) : Term()
  }

  /**
   * A raw pattern.
   */
  sealed class Pattern {
    data class PairOf(
      val first: Pattern,
      val second: Pattern,
    ) : Pattern()

    data class Var(
      val name: String,
    ) : Pattern()

    data object Drop : Pattern()
  }
}
