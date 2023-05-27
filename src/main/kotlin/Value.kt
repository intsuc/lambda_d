import Core.Term
import kotlinx.collections.immutable.PersistentList

sealed class Value {
  /**
   * A semantic term.
   * Must be well-typed.
   */
  sealed class Term {
    /**
     * The type of this term, and is used to restore the type information when [quote]ing this term.
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
      val level: Level,
      override val type: Lazy<Term>,
    ) : Term()
  }
}

typealias Env = PersistentList<Lazy<Value.Term>>

data class Closure(
  val env: Env,
  val name: String?,
  val body: Term,
)
