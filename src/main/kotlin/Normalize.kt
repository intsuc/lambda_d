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
  val terms = if (termName == null) terms else terms + arg
  val types = if (typeName == null) types else types + arg
  return eval(terms, types, body)
}

fun Env.next(): Level {
  return Level(size)
}

fun Env.normalize(
  term: C.Term,
): C.Term {
  return quote(next(), next(), eval(this, this, term))
}

fun eval(
  terms: Env,
  types: Env,
  term: C.Term,
): V.Term {
  return when (term) {
    is C.Term.Type   -> {
      V.Term.Type
    }

    is C.Term.Func   -> {
      val param = lazy { eval(terms, types, term.param) }
      val result = Closure(terms, types, term.name, null, term.result)
      V.Term.Func(param, result)
    }

    is C.Term.FuncOf -> {
      val body = Closure(terms, types, term.name, (term.type as C.Term.Func).name, term.body)
      val type = lazy { eval(types, emptyEnv(), term.type) }
      V.Term.FuncOf(body, type)
    }

    is C.Term.App    -> {
      val arg = lazy { eval(terms, types, term.arg) }
      when (val func = eval(terms, types, term.func)) {
        is V.Term.FuncOf -> func.body(arg)
        else             -> {
          val type = lazy { eval(types, emptyEnv(), term.type) }
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
      val init = lazy { eval(terms, types, term.init) }
      eval(terms + init, types + init, term.body)
    }

    is C.Term.Var    -> {
      terms[term.index.toLevel(terms.next()).value].value
    }
  }
}

fun quote(
  terms: Level,
  types: Level,
  value: V.Term,
): C.Term {
  return when (value) {
    is V.Term.Type   -> {
      C.Term.Type
    }

    is V.Term.Func   -> {
      val param = quote(terms, types, value.param.value)
      val result = quote(
        (terms + if (value.result.termName == null) 0 else 1),
        (types + if (value.result.typeName == null) 0 else 1),
        value.result(lazyOf(V.Term.Var(terms, value.param))),
      )
      C.Term.Func(value.result.termName, param, result)
    }

    is V.Term.FuncOf -> {
      when (val funcType = value.type.value) {
        is V.Term.Func -> {
          val body = quote(
            (terms + if (value.body.termName == null) 0 else 1),
            (types + if (value.body.typeName == null) 0 else 1),
            value.body(lazyOf(V.Term.Var(terms, funcType.param))),
          )
          val type = quote(types, Level(0), value.type.value)
          C.Term.FuncOf(value.body.termName, body, type)
        }
        else           -> error("expected func, got $funcType")
      }
    }

    is V.Term.App    -> {
      val func = quote(terms, types, value.func)
      val arg = quote(terms, types, value.arg.value)
      val type = quote(types, Level(0), value.type.value)
      C.Term.App(func, arg, type)
    }

    is V.Term.Unit   -> {
      C.Term.Unit
    }

    is V.Term.UnitOf -> {
      C.Term.UnitOf
    }

    is V.Term.Var    -> {
      val index = value.level.toIndex(terms)
      val type = quote(types, Level(0), value.type.value)
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
      (value1.result.termName == null) == (value2.result.termName == null) &&
      lazyOf(V.Term.Var(this, value1.param)).let { (this + if (value1.result.termName == null) 0 else 1).conv(value1.result(it), value2.result(it)) }
    }

    is V.Term.FuncOf -> {
      value2 is V.Term.FuncOf &&
      (value1.body.termName == null) == (value2.body.termName == null) &&
      lazyOf(V.Term.Var(this, (value1.type.value as V.Term.Func).param)).let { (this + if (value1.body.termName == null) 0 else 1).conv(value1.body(it), value2.body(it)) }
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
