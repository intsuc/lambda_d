sealed class Surface {
  /**
   * A raw surface term.
   * [Term] may or may not be well-formed.
   */
  sealed class Term {
    data object Type : Term()

    data class Func(
      val name: String?,
      val param: Term,
      val result: Term,
    ) : Term()

    data class FuncOf(
      val name: String?,
      val body: Term,
    ) : Term()

    data class App(
      val func: Term,
      val arg: Term,
    ) : Term()

    data object Unit : Term()

    data object UnitOf : Term()

    data class Let(
      val name: String?,
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
}
