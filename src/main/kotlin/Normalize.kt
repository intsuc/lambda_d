import kotlinx.collections.immutable.plus

fun Env.eval(
  core: Core,
): Value {
  return when (core) {
    is Core.Type   -> {
      Value.Type
    }

    is Core.Func   -> {
      val param = lazy { eval(core.param) }
      val result = Closure(this, core.result)
      Value.Func(param, result)
    }

    is Core.FuncOf -> {
      val body = Closure(this, core.body)
      Value.FuncOf(body)
    }

    is Core.App    -> {
      val arg = lazy { eval(core.arg) }
      when (val func = eval(core.func)) {
        is Value.FuncOf -> func.body(arg)
        else            -> Value.App(func, arg)
      }
    }

    is Core.Let    -> {
      val init = lazy { eval(core.init) }
      (this + init).eval(core.body)
    }

    is Core.Var    -> {
      this[core.level.value].value
    }
  }
}

operator fun Closure.invoke(arg: Lazy<Value>): Value {
  return (env + arg).eval(body)
}

fun Lvl.conv(
  value1: Value,
  value2: Value,
): Boolean {
  return when (value1) {
    is Value.Type   -> {
      value2 is Value.Type
    }

    is Value.Func   -> {
      value2 is Value.Func &&
      conv(value1.param.value, value2.param.value) &&
      lazyOf(Value.Var(this)).let { suc().conv(value1.result(it), value2.result(it)) }
    }

    is Value.FuncOf -> {
      value2 is Value.FuncOf &&
      lazyOf(Value.Var(this)).let { suc().conv(value1.body(it), value2.body(it)) }
    }

    is Value.App    -> {
      value2 is Value.App &&
      conv(value1.func, value2.func) &&
      conv(value1.arg.value, value2.arg.value)
    }

    is Value.Var    -> {
      value2 is Value.Var &&
      value1.level == value2.level
    }
  }
}
