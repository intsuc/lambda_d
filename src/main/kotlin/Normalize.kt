import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.plus

typealias Env = PersistentList<Lazy<Value>>

fun Env.eval(
  core: Core,
): Value {
  return when (core) {
    is Core.Type   -> Value.Type

    is Core.Func   -> Value.Func(lazy { eval(core.param) }) { (this + it).eval(core.result) }

    is Core.FuncOf -> Value.FuncOf { (this + it).eval(core.body) }

    is Core.App    -> when (val func = eval(core.func)) {
      is Value.FuncOf -> func.body(lazy { eval(core.arg) })
      else            -> Value.App(func, lazy { eval(core.arg) })
    }

    is Core.Let    -> {
      val init = lazy { eval(core.init) }
      (this + init).eval(core.body)
    }

    is Core.Var    -> this[core.level.value].value
  }
}

@JvmInline
value class Lvl(val value: Int) {
  fun suc(): Lvl {
    return Lvl(value + 1)
  }
}

fun Lvl.conv(
  value1: Value,
  value2: Value,
): Boolean {
  return when (value1) {
    is Value.Type   ->
      value2 is Value.Type

    is Value.Func   ->
      value2 is Value.Func &&
      conv(value1.param.value, value2.param.value) &&
      lazyOf(Value.Var(this)).let { suc().conv(value1.result(it), value2.result(it)) }

    is Value.FuncOf ->
      value2 is Value.FuncOf &&
      lazyOf(Value.Var(this)).let { suc().conv(value1.body(it), value2.body(it)) }

    is Value.App    ->
      value2 is Value.App &&
      conv(value1.func, value2.func) &&
      conv(value1.arg.value, value2.arg.value)

    is Value.Var    ->
      value2 is Value.Var &&
      value1.level == value2.level
  }
}
