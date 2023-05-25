sealed class Surface {
  /**
   * A raw surface term.
   * [Term] may or may not be well-formed.
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
      val body: Term,
    ) : Term()

    data class App(
      val func: Term,
      val arg: Term,
    ) : Term()

    data object Unit : Term()

    data object UnitOf : Term()

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
   * A raw surface pattern.
   * [Pattern] may or may not be well-formed.
   */
  sealed class Pattern {
    data object UnitOf : Pattern()

    data class Var(
      val name: String,
    ) : Pattern()

    data object Drop : Pattern()

    data class Anno(
      val target: Pattern,
      val type: Term,
    ) : Pattern()
  }
}
