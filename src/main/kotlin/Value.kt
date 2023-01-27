sealed interface Value {
  object Type : Value

  class Func(
    val param: Lazy<Value>,
    val result: (Lazy<Value>) -> Value,
  ) : Value

  data class FuncOf(
    val body: (Lazy<Value>) -> Value,
  ) : Value

  data class App(
    val func: Value,
    val arg: Lazy<Value>,
  ) : Value

  data class Var(
    val level: Lvl,
  ) : Value
}
