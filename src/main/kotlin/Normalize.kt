import Core.Term
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
  term: Term,
): Term {
  return quote(next(), next(), eval(this, this, term))
}

fun eval(
  terms: Env,
  types: Env,
  term: Term,
): Value {
  return when (term) {
    is Term.Type   -> {
      Value.Type
    }

    is Term.Func   -> {
      val param = lazy { eval(terms, types, term.param) }
      val result = Closure(terms, types, term.name, null, term.result)
      Value.Func(param, result)
    }

    is Term.FuncOf -> {
      val body = Closure(terms, types, term.name, (term.type as Term.Func).name, term.body)
      val type = lazy { eval(types, emptyEnv(), term.type) }
      Value.FuncOf(body, type)
    }

    is Term.App    -> {
      val arg = lazy { eval(terms, types, term.arg) }
      when (val func = eval(terms, types, term.func)) {
        is Value.FuncOf -> func.body(arg)
        else            -> {
          val type = lazy { eval(types, emptyEnv(), term.type) }
          Value.App(func, arg, type)
        }
      }
    }

    is Term.Unit   -> {
      Value.Unit
    }

    is Term.UnitOf -> {
      Value.UnitOf
    }

    is Term.Let    -> {
      val init = lazy { eval(terms, types, term.init) }
      eval(terms + init, types + init, term.body)
    }

    is Term.Var    -> {
      terms[term.index.toLvl(terms.next()).value].value
    }
  }
}

fun quote(
  terms: Lvl,
  types: Lvl,
  value: Value,
): Term {
  return when (value) {
    is Value.Type   -> {
      Term.Type
    }

    is Value.Func   -> {
      val param = quote(terms, types, value.param.value)
      val result = quote(
        (terms + if (value.result.termName == null) 0 else 1),
        (types + if (value.result.typeName == null) 0 else 1),
        value.result(lazyOf(Value.Var(terms, value.param))),
      )
      Term.Func(value.result.termName, param, result)
    }

    is Value.FuncOf -> {
      when (val funcType = value.type.value) {
        is Value.Func -> {
          val body = quote(
            (terms + if (value.body.termName == null) 0 else 1),
            (types + if (value.body.typeName == null) 0 else 1),
            value.body(lazyOf(Value.Var(terms, funcType.param))),
          )
          val type = quote(types, Lvl(0), value.type.value)
          Term.FuncOf(value.body.termName, body, type)
        }
        else          -> error("expected func, got $funcType")
      }
    }

    is Value.App    -> {
      val func = quote(terms, types, value.func)
      val arg = quote(terms, types, value.arg.value)
      val type = quote(types, Lvl(0), value.type.value)
      Term.App(func, arg, type)
    }

    is Value.Unit   -> {
      Term.Unit
    }

    is Value.UnitOf -> {
      Term.UnitOf
    }

    is Value.Var    -> {
      val index = value.level.toIdx(terms)
      val type = quote(types, Lvl(0), value.type.value)
      Term.Var(index, type)
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
