import kotlinx.collections.immutable.plus

// TODO: fix offset shift
operator fun Closure.invoke(arg: Lazy<Value>): Value {
  return (env + arg).eval(body)
}

fun Env.next(): Lvl {
  return Lvl(size)
}

fun Env.eval(
  core: Core,
): Value {
  return when (core) {
    is Core.Type -> {
      Value.Type
    }

    is Core.Func   -> {
      val param = lazy { eval(core.param) }
      val result = Closure(this, core.result)
      Value.Func(core.name, param, result)
    }

    is Core.FuncOf -> {
      val body = Closure(this, core.body)
      val type = lazy { eval(core.type) }
      Value.FuncOf(core.name, body, type)
    }

    is Core.App    -> {
      val arg = lazy { eval(core.arg) }
      when (val func = eval(core.func)) {
        is Value.FuncOf -> func.body(arg)
        else            -> {
          val type = lazy { eval(core.type) }
          Value.App(func, arg, type)
        }
      }
    }

    is Core.Unit -> {
      Value.Unit
    }

    is Core.UnitOf -> {
      Value.UnitOf
    }

    is Core.Let    -> {
      val init = lazy { eval(core.init) }
      (this + init).eval(core.body)
    }

    is Core.Var    -> {
      this[core.index.toLvl(next()).value].value
    }
  }
}

fun Lvl.quote(
  value: Value,
): Core {
  return when (value) {
    is Value.Type -> {
      Core.Type
    }

    is Value.Func   -> {
      val param = quote(value.param.value)
      val result = (this + 1).quote(value.result(lazyOf(Value.Var(this, value.param))))
      Core.Func(value.name, param, result)
    }

    is Value.FuncOf -> {
      when (val funcType = value.type.value) {
        is Value.Func -> {
          val body = (this + 1).quote(value.body(lazyOf(Value.Var(this, funcType.param))))
          val type = quote(value.type.value)
          Core.FuncOf(value.name, body, type)
        }
        else          -> error("expected function type, got $funcType")
      }
    }

    is Value.App    -> {
      val func = quote(value.func)
      val arg = quote(value.arg.value)
      val type = quote(value.type.value)
      Core.App(func, arg, type)
    }

    is Value.Unit -> {
      Core.Unit
    }

    is Value.UnitOf -> {
      Core.UnitOf
    }

    is Value.Var    -> {
      val index = value.level.toIdx(this)
      val type = quote(value.type.value)
      Core.Var(index, type)
    }
  }
}

fun Lvl.conv(
  value1: Value,
  value2: Value,
): Boolean {
  return when (value1) {
    is Value.Type -> {
      value2 is Value.Type
    }

    is Value.Func   -> {
      value2 is Value.Func &&
      conv(value1.param.value, value2.param.value) &&
      lazyOf(Value.Var(this, value1.param)).let { (this + 1).conv(value1.result(it), value2.result(it)) }
    }

    is Value.FuncOf -> {
      value2 is Value.FuncOf &&
      when (val funcType = value1.type.value) {
        is Value.Func -> lazyOf(Value.Var(this, funcType.param)).let { (this + 1).conv(value1.body(it), value2.body(it)) }
        else          -> error("expected func, got $funcType")
      }
    }

    is Value.App    -> {
      value2 is Value.App &&
      conv(value1.func, value2.func) &&
      conv(value1.arg.value, value2.arg.value)
    }

    is Value.Unit -> {
      value2 is Value.Unit
    }

    is Value.UnitOf -> {
      value2 is Value.UnitOf
    }

    is Value.Var    -> {
      value2 is Value.Var &&
      value1.level == value2.level
    }
  }
}
