import kotlinx.collections.immutable.PersistentList

sealed class Value {
  abstract val type: Lazy<Value>

  data object Univ : Value() {
    override val type: Lazy<Value> get() = UNIV
  }

  class Func(
    val name: String?,
    val param: Lazy<Value>,
    val result: Closure,
  ) : Value() {
    override val type: Lazy<Value> get() = UNIV
  }

  data class FuncOf(
    val name: String?,
    val body: Closure,
    override val type: Lazy<Value>,
  ) : Value()

  data class App(
    val func: Value,
    val arg: Lazy<Value>,
    override val type: Lazy<Value>,
  ) : Value()

  data class Var(
    val level: Lvl,
    override val type: Lazy<Value>,
  ) : Value()
}

private val UNIV: Lazy<Value> = lazyOf(Value.Univ)

typealias Env = PersistentList<Lazy<Value>>

data class Closure(
  val env: Env,
  val body: Core,
)
