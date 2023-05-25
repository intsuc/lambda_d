sealed class Core {
  /**
   * An elaborated syntactic term.
   * [Term] must be well-typed.
   */
  sealed class Term {
    /**
     * The type of this term, and can be used in subsequent compilation passes.
     */
    abstract val type: Term

    data object Type : Term() {
      // Type in type
      override val type: Term get() = Type
    }

    data class Func(
      val binder: Pattern,
      val param: Term,
      val result: Term,
    ) : Term() {
      override val type: Term get() = Type
    }

    data class FuncOf(
      val binder: Pattern,
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
      val binder: Pattern,
      val init: Term,
      val body: Term,
    ) : Term() {
      // Store the type of the body to avoid possible cascading field accesses
      override val type: Term = body.type
    }

    data class Var(
      val name: String,
      val index: Idx,
      override val type: Term,
    ) : Term()
  }

  /**
   * An elaborated syntactic pattern.
   * [Pattern] must be well-typed.
   */
  sealed class Pattern {
    /**
     * The type of this term, and can be used in subsequent compilation passes.
     */
    abstract val type: Term

    data object UnitOf : Pattern() {
      override val type: Term get() = Term.Unit
    }

    data class Var(
      val name: String,
      override val type: Term,
    ) : Pattern()

    data class Drop(
      override val type: Term,
    ) : Pattern()
  }
}
