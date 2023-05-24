import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus

// Temporarily disable laziness to find bugs eagerly.
inline fun <T> lazy(
  block: () -> T,
): Lazy<T> {
  return lazyOf(block())
}

fun emptyEnv(): Env {
  return persistentListOf()
}

operator fun Closure.invoke(
  arg: Lazy<Value>,
): Value {
  val terms = if (termName == null) terms else terms + arg
  val types = if (typeName == null) types else types + arg
  return eval(terms, types, body)
}

fun Env.next(): Lvl {
  return Lvl(size)
}

fun Env.normalize(
  core: Core,
): Core {
  return next().quote(eval(this, this, core))
}

fun eval(
  terms: Env,
  types: Env,
  core: Core,
): Value {
  return when (core) {
    is Core.Type   -> {
      Value.Type
    }

    is Core.Func   -> {
      val param = lazy { eval(terms, types, core.param) }
      val result = Closure(terms, types, core.name, null, core.result)
      Value.Func(param, result)
    }

    is Core.FuncOf -> {
      val body = Closure(terms, types, core.name, (core.type as Core.Func).name, core.body)
      val type = lazy { eval(types, emptyEnv(), core.type) }
      Value.FuncOf(body, type)
    }

    is Core.App    -> {
      val arg = lazy { eval(terms, types, core.arg) }
      when (val func = eval(terms, types, core.func)) {
        is Value.FuncOf -> func.body(arg)
        else            -> {
          val type = lazy { eval(types, emptyEnv(), core.type) }
          Value.App(func, arg, type)
        }
      }
    }

    is Core.Unit   -> {
      Value.Unit
    }

    is Core.UnitOf -> {
      Value.UnitOf
    }

    is Core.Let    -> {
      val init = lazy { eval(terms, types, core.init) }
      eval(terms + init, types + init, core.body)
    }

    is Core.Var    -> {
      terms[core.index.toLvl(terms.next()).value].value
    }
  }
}

fun Lvl.quote(
  value: Value,
): Core {
  return when (value) {
    is Value.Type   -> {
      Core.Type
    }

    is Value.Func   -> {
      val param = quote(value.param.value)
      val result = (this + if (value.result.termName == null) 0 else 1).quote(value.result(lazyOf(Value.Var(this, value.param))))
      Core.Func(value.result.termName, param, result)
    }

    is Value.FuncOf -> {
      when (val funcType = value.type.value) {
        is Value.Func -> {
          val body = (this + if (value.body.termName == null) 0 else 1).quote(value.body(lazyOf(Value.Var(this, funcType.param))))
          val type = quote(value.type.value)
          Core.FuncOf(value.body.termName, body, type)
        }
        else          -> error("expected func, got $funcType")
      }
    }

    is Value.App    -> {
      val func = quote(value.func)
      val arg = quote(value.arg.value)
      val type = quote(value.type.value)
      Core.App(func, arg, type)
    }

    is Value.Unit   -> {
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
    is Value.Type   -> {
      value2 is Value.Type
    }

    is Value.Func   -> {
      value2 is Value.Func &&
      conv(value1.param.value, value2.param.value) &&
      (value1.result.termName == null) == (value2.result.termName == null) &&
      lazyOf(Value.Var(this, value1.param)).let { (this + if (value1.result.termName == null) 0 else 1).conv(value1.result(it), value2.result(it)) }
    }

    is Value.FuncOf -> {
      value2 is Value.FuncOf &&
      when (val funcType = value1.type.value) {
        is Value.Func -> {
          (value1.body.termName == null) == (value2.body.termName == null) &&
          lazyOf(Value.Var(this, funcType.param)).let { (this + if (value1.body.termName == null) 0 else 1).conv(value1.body(it), value2.body(it)) }
        }
        else          -> error("expected func, got $funcType")
      }
    }

    is Value.App    -> {
      value2 is Value.App &&
      conv(value1.func, value2.func) &&
      conv(value1.arg.value, value2.arg.value)
    }

    is Value.Unit   -> {
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
