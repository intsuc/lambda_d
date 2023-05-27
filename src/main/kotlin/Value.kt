import kotlinx.collections.immutable.PersistentList
import Core as C
import Value as V

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

    data class Apply(
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

    data class Pair(
      val first: Lazy<Term>,
      val second: Closure,
    ) : Term() {
      override val type: Lazy<Term> get() = Type.TYPE
    }

    data class PairOf(
      val first: Lazy<Term>,
      val second: Lazy<Term>,
      override val type: Lazy<Term>,
    ) : Term()

    data class First(
      val pair: Term,
      override val type: Lazy<Term>,
    ) : Term()

    data class Second(
      val pair: Term,
      override val type: Lazy<Term>,
    ) : Term()

    data class Var(
      val level: Level,
      override val type: Lazy<Term>,
    ) : Term()
  }
}

typealias Env = PersistentList<Lazy<V.Term>>

data class Closure(
  val env: Env,
  val body: C.Term,
)
