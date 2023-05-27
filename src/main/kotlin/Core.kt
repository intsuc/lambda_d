sealed class Core {
  /**
   * An elaborated term.
   * Must be well-typed.
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
      val param: Term,
      val result: Term,
    ) : Term() {
      override val type: Term get() = Type
    }

    data class FuncOf(
      val body: Term,
      override val type: Term,
    ) : Term()

    data class Apply(
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

    data class Pair(
      val first: Term,
      val second: Term,
    ) : Term() {
      override val type: Term get() = Type
    }

    data class PairOf(
      val first: Term,
      val second: Term,
      override val type: Term,
    ) : Term()

    data class First(
      val pair: Term,
      override val type: Term,
    ) : Term()

    data class Second(
      val pair: Term,
      override val type: Term,
    ) : Term()

    data class Let(
      val init: Term,
      val body: Term,
    ) : Term() {
      // Store the type of the body to avoid cascading field accesses
      override val type: Term = body.type
    }

    data class Var(
      val index: Index,
      override val type: Term,
    ) : Term()
  }
}
