import kotlinx.collections.immutable.PersistentList

sealed interface Value {
  object Type : Value

  class Func(
    val param: Lazy<Value>,
    val result: Closure,
  ) : Value

  data class FuncOf(
    val body: Closure,
  ) : Value

  data class App(
    val func: Value,
    val arg: Lazy<Value>,
  ) : Value

  data class Var(
    val level: Int,
  ) : Value
}

typealias Env = PersistentList<Lazy<Value>>

data class Closure(
  val env: Env,
  val body: Core,
)
