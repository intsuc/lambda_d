import kotlinx.collections.immutable.PersistentList

sealed class Value {
  abstract val type: Lazy<Value>

  data object Type : Value() {
    val TYPE: Lazy<Value> = lazyOf(Type)
    override val type: Lazy<Value> get() = TYPE
  }

  data class Func(
    val param: Lazy<Value>,
    val result: Closure,
  ) : Value() {
    override val type: Lazy<Value> get() = Type.TYPE
  }

  data class FuncOf(
    val body: Closure,
    override val type: Lazy<Value>,
  ) : Value()

  data class App(
    val func: Value,
    val arg: Lazy<Value>,
    override val type: Lazy<Value>,
  ) : Value()

  data object Unit : Value() {
    val UNIT: Lazy<Value> = lazyOf(Unit)
    override val type: Lazy<Value> get() = Type.TYPE
  }

  data object UnitOf : Value() {
    override val type: Lazy<Value> get() = Unit.UNIT
  }

  data class Var(
    val level: Lvl,
    override val type: Lazy<Value>,
  ) : Value()
}

typealias Env = PersistentList<Lazy<Value>>

data class Closure(
  val terms: Env,
  val types: Env,
  val termName: String?,
  val typeName: String?,
  val body: Core,
)
