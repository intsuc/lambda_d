import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import Core as C
import Value as V

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
  arg: Lazy<V.Term>,
): V.Term {
  return (env + arg).eval(body)
}

fun Env.next(): Level {
  return Level(size)
}

fun Env.normalize(
  term: C.Term,
): C.Term {
  return next().quote(eval(term))
}

fun Env.eval(
  term: C.Term,
): V.Term {
  return when (term) {
    is C.Term.Type   -> {
      V.Term.Type
    }

    is C.Term.Func   -> {
      val param = lazy { eval(term.param) }
      val result = Closure(this, term.name, term.result)
      V.Term.Func(param, result)
    }

    is C.Term.FuncOf -> {
      val body = Closure(this, term.name, term.body)
      val type = lazy { eval(term.type) }
      V.Term.FuncOf(body, type)
    }

    is C.Term.App    -> {
      val arg = lazy { eval(term.arg) }
      when (val func = eval(term.func)) {
        is V.Term.FuncOf -> func.body(arg)
        else             -> {
          val type = lazy { eval(term.type) }
          V.Term.App(func, arg, type)
        }
      }
    }

    is C.Term.Unit   -> {
      V.Term.Unit
    }

    is C.Term.UnitOf -> {
      V.Term.UnitOf
    }

    is C.Term.Let    -> {
      val init = lazy { eval(term.init) }
      (this + init).eval(term.body)
    }

    is C.Term.Var    -> {
      this[term.index.toLevel(next()).value].value
    }
  }
}

fun Level.quote(
  value: V.Term,
): C.Term {
  return when (value) {
    is V.Term.Type   -> {
      C.Term.Type
    }

    is V.Term.Func   -> {
      val param = quote(value.param.value)
      val result = (this + 1).quote(value.result(lazyOf(V.Term.Var(this, value.param))))
      C.Term.Func(value.result.name, param, result)
    }

    is V.Term.FuncOf -> {
      when (val funcType = value.type.value) {
        is V.Term.Func -> {
          val body = (this + 1).quote(value.body(lazyOf(V.Term.Var(this, funcType.param))))
          val type = quote(value.type.value)
          C.Term.FuncOf(value.body.name, body, type)
        }
        else           -> error("expected func, got $funcType")
      }
    }

    is V.Term.App    -> {
      val func = quote(value.func)
      val arg = quote(value.arg.value)
      val type = quote(value.type.value)
      C.Term.App(func, arg, type)
    }

    is V.Term.Unit   -> {
      C.Term.Unit
    }

    is V.Term.UnitOf -> {
      C.Term.UnitOf
    }

    is V.Term.Var    -> {
      val index = value.level.toIndex(this)
      val type = quote(value.type.value)
      C.Term.Var(index, type)
    }
  }
}

fun Level.conv(
  value1: V.Term,
  value2: V.Term,
): Boolean {
  return when (value1) {
    is V.Term.Type   -> {
      value2 is V.Term.Type
    }

    is V.Term.Func   -> {
      value2 is V.Term.Func &&
      conv(value1.param.value, value2.param.value) &&
      (value1.result.name == null) == (value2.result.name == null) &&
      lazyOf(V.Term.Var(this, value1.param)).let { (this + 1).conv(value1.result(it), value2.result(it)) }
    }

    is V.Term.FuncOf -> {
      value2 is V.Term.FuncOf &&
      (value1.body.name == null) == (value2.body.name == null) &&
      lazyOf(V.Term.Var(this, (value1.type.value as V.Term.Func).param)).let { (this + 1).conv(value1.body(it), value2.body(it)) }
    }

    is V.Term.App    -> {
      value2 is V.Term.App &&
      conv(value1.func, value2.func) &&
      conv(value1.arg.value, value2.arg.value)
    }

    is V.Term.Unit   -> {
      value2 is V.Term.Unit
    }

    is V.Term.UnitOf -> {
      value2 is V.Term.UnitOf
    }

    is V.Term.Var    -> {
      value2 is V.Term.Var &&
      value1.level == value2.level
    }
  }
}
