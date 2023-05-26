sealed class Core {
  /**
   * An elaborated core term.
   * [Term] must be well-typed.
   * [type] is the type of this term, and can be used in subsequent compilation passes.
   */
  sealed class Term {
    abstract val type: Term

    data object Type : Term() {
      // Type in type
      override val type: Term get() = Type
    }

    data class Func(
      val name: String?,
      val param: Term,
      val result: Term,
    ) : Term() {
      override val type: Term get() = Type
    }

    data class FuncOf(
      val name: String?,
      val body: Term,
      override val type: Term,
    ) : Term()

    data class App(
      val func: Term,
      val arg: Term,
      override val type: Term,
    ) : Term()

    data object Unit : Term() {
      override val type: Term get() = Type
    }

    data object UnitOf : Term() {
      override val type: Term get() = Unit
    }

    data class Let(
      val name: String?,
      val init: Term,
      val body: Term,
    ) : Term() {
      // Store the type of the body to avoid cascading field accesses
      override val type: Term = body.type
    }

    data class Var(
      val index: Idx,
      override val type: Term,
    ) : Term()
  }
}
