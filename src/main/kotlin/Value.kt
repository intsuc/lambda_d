import kotlinx.collections.immutable.PersistentList

sealed class Value {
  /**
   * A semantic term.
   * [Term] must be well-typed.
   */
  sealed class Term {
    /**
     * The type of this value, and is used to restore the type information when [quoteTerm]ing this term.
     */
    abstract val type: Lazy<Term>

    data object Type : Term() {
      val TYPE: Lazy<Term> = lazyOf(Type)
      override val type: Lazy<Term> get() = TYPE
    }

    data class Func(
      val param: Lazy<Term>,
      val result: Closure,
    ) : Term() {
      override val type: Lazy<Term> get() = Type.TYPE
    }

    data class FuncOf(
      val body: Closure,
      override val type: Lazy<Term>,
    ) : Term()

    data class App(
      val func: Term,
      val arg: Lazy<Term>,
      override val type: Lazy<Term>,
    ) : Term()

    data object Unit : Term() {
      val UNIT: Lazy<Term> = lazyOf(Unit)
      override val type: Lazy<Term> get() = Type.TYPE
    }

    data object UnitOf : Term() {
      override val type: Lazy<Term> get() = Unit.UNIT
    }

    data class Var(
      val name: String,
      val level: Lvl,
      override val type: Lazy<Term>,
    ) : Term()
  }

  /**
   * A semantic pattern.
   * [Pattern] must be well-typed.
   */
  sealed class Pattern {
    /**
     * The type of this pattern, and can be used in subsequent compilation passes.
     */
    abstract val type: Lazy<Term>

    data object UnitOf : Pattern() {
      override val type: Lazy<Term> get() = Term.Unit.UNIT
    }

    data class Var(
      val name: String,
      override val type: Lazy<Term>,
    ) : Pattern()

    data class Drop(
      override val type: Lazy<Term>,
    ) : Pattern()
  }
}

typealias Env = PersistentList<Lazy<Value.Term>>

data class Closure(
  val env0: Env,
  val env1: Env,
  val binder0: Value.Pattern,
  val binder1: Value.Pattern,
  val body0: Core.Term,
)
