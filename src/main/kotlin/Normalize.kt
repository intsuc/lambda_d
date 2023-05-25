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
  val env0 = env0 + (binder0 matches arg)
  val env1 = env1 + (binder1 matches arg)
  return evalTerm(env0, env1, body0)
}

fun Closure.open(): V.Term {
  val env0 = env0 + env0.next().vars(binder0)
  val env1 = env1 + env1.next().vars(binder1)
  return evalTerm(env0, env1, body0)
}

fun Env.next(): Lvl {
  return Lvl(size)
}

fun Env.normalizeTerm(
  term: C.Term,
): C.Term {
  return quoteTerm(next(), next(), evalTerm(this, this, term))
}

/**
 * Evaluates [term0] under [env0] and [env1].
 * @param env0 The environment at level *n*.
 * @param env1 The environment at level *n+1*.
 * @param term0 The term to be evaluated at level *n*.
 */
fun evalTerm(
  env0: Env,
  env1: Env,
  term0: C.Term,
): V.Term {
  return when (term0) {
    is C.Term.Type   -> {
      V.Term.Type
    }

    is C.Term.Func   -> {
      val param = lazy { evalTerm(env0, env1, term0.param) }
      val binder0 = evalPattern(env1, term0.binder)
      val result = Closure(env0, env1, binder0, V.Pattern.Drop(V.Term.Type.TYPE) /* TODO */, term0.result)
      V.Term.Func(param, result)
    }

    is C.Term.FuncOf -> {
      val binder0 = evalPattern(env1, term0.binder)
      val binder1 = evalPattern(env1, (term0.type as C.Term.Func /* TODO: safe? */).binder)
      val body = Closure(env0, env1, binder0, binder1, term0.body)
      val type = lazy { evalTerm(env1, emptyEnv(), term0.type) }
      V.Term.FuncOf(body, type)
    }

    is C.Term.App    -> {
      val arg = lazy { evalTerm(env0, env1, term0.arg) }
      when (val func = evalTerm(env0, env1, term0.func)) {
        is V.Term.FuncOf -> func.body(arg)
        else             -> {
          val type = lazy { evalTerm(env1, emptyEnv(), term0.type) }
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
      val init = lazy { evalTerm(env0, env1, term0.init) }
      evalTerm(env0 + init, env1 + init, term0.body)
    }

    is C.Term.Var    -> {
      env0[term0.index.toLvl(env0.next()).value].value
    }
  }
}

fun quoteTerm(
  size0: Lvl,
  size1: Lvl,
  value0: V.Term,
): C.Term {
  return when (value0) {
    is V.Term.Type   -> {
      C.Term.Type
    }

    is V.Term.Func   -> {
      val binder = quotePattern(size1, value0.result.binder0)
      val param = quoteTerm(size0, size1, value0.param.value)
      val result = quoteTerm(
        size0 + size0.vars(value0.result.binder0).size,
        size1 + size1.vars(value0.result.binder1).size,
        value0.result.open(),
      )
      C.Term.Func(binder, param, result)
    }

    is V.Term.FuncOf -> {
      val binder = quotePattern(size1, value0.body.binder0)
      val body = quoteTerm(
        size0 + size0.vars(value0.body.binder0).size,
        size1 + size1.vars(value0.body.binder1).size,
        value0.body.open(),
      )
      val type = quoteTerm(size1, Lvl(0), value0.type.value)
      C.Term.FuncOf(binder, body, type)
    }

    is V.Term.App    -> {
      val func = quoteTerm(size0, size1, value0.func)
      val arg = quoteTerm(size0, size1, value0.arg.value)
      val type = quoteTerm(size1, Lvl(0), value0.type.value)
      C.Term.App(func, arg, type)
    }

    is V.Term.Unit   -> {
      C.Term.Unit
    }

    is V.Term.UnitOf -> {
      C.Term.UnitOf
    }

    is V.Term.Var    -> {
      val index = value0.level.toIdx(size0)
      val type = quoteTerm(size1, Lvl(0), value0.type.value)
      C.Term.Var(value0.name, index, type)
    }
  }
}

fun evalPattern(
  env1: Env,
  pattern0: C.Pattern,
): V.Pattern {
  return when (pattern0) {
    is C.Pattern.UnitOf -> {
      V.Pattern.UnitOf
    }

    is C.Pattern.Var    -> {
      val type = lazy { evalTerm(env1, emptyEnv(), pattern0.type) }
      V.Pattern.Var(pattern0.name, type)
    }

    is C.Pattern.Drop   -> {
      val type = lazy { evalTerm(env1, emptyEnv(), pattern0.type) }
      V.Pattern.Drop(type)
    }
  }
}

fun quotePattern(
  lvl1: Lvl,
  pattern0: V.Pattern,
): C.Pattern {
  return when (pattern0) {
    is V.Pattern.UnitOf -> {
      C.Pattern.UnitOf
    }

    is V.Pattern.Var    -> {
      val type = quoteTerm(lvl1, Lvl(0), pattern0.type.value)
      C.Pattern.Var(pattern0.name, type)
    }

    is V.Pattern.Drop   -> {
      val type = quoteTerm(lvl1, Lvl(0), pattern0.type.value)
      C.Pattern.Drop(type)
    }
  }
}

infix fun V.Pattern.matches(
  term: Lazy<V.Term>,
): List<Lazy<V.Term>> {
  return when (this) {
    is V.Pattern.UnitOf -> {
      emptyList()
    }

    is V.Pattern.Var    -> {
      listOf(term)
    }

    is V.Pattern.Drop   -> {
      emptyList()
    }
  }
}

fun Lvl.vars(
  pattern: V.Pattern,
): List<Lazy<V.Term.Var>> {
  return when (pattern) {
    is V.Pattern.UnitOf -> {
      emptyList()
    }

    is V.Pattern.Var    -> {
      listOf(lazyOf(V.Term.Var(pattern.name, this, pattern.type)))
    }

    is V.Pattern.Drop   -> {
      emptyList()
    }
  }
}

fun Lvl.convTerm(
  valueL: V.Term,
  valueR: V.Term,
): Boolean {
  return when (valueL) {
    is V.Term.Type   -> {
      valueR is V.Term.Type
    }

    is V.Term.Func   -> {
      valueR is V.Term.Func &&
      convTerm(valueL.param.value, valueR.param.value) &&
      convPattern(valueL.result.binder0, valueR.result.binder0) &&
      (this + vars(valueL.result.binder0).size).convTerm(valueL.result.open(), valueR.result.open())
    }

    is V.Term.FuncOf -> {
      valueR is V.Term.FuncOf &&
      convPattern(valueL.body.binder0, valueR.body.binder0) &&
      (this + vars(valueL.body.binder0).size).convTerm(valueL.body.open(), valueR.body.open())
    }

    is V.Term.App    -> {
      valueR is V.Term.App &&
      convTerm(valueL.func, valueR.func) &&
      convTerm(valueL.arg.value, valueR.arg.value)
    }

    is V.Term.Unit   -> {
      valueR is V.Term.Unit
    }

    is V.Term.UnitOf -> {
      valueR is V.Term.UnitOf
    }

    is V.Term.Var    -> {
      valueR is V.Term.Var &&
      valueL.level == valueR.level
    }
  }
}

fun convPattern(
  patternL: V.Pattern,
  patternR: V.Pattern,
): Boolean {
  return when (patternL) {
    is V.Pattern.UnitOf -> {
      patternR is V.Pattern.UnitOf
    }

    is V.Pattern.Var    -> {
      patternR is V.Pattern.Var
    }

    is V.Pattern.Drop   -> {
      patternR is V.Pattern.Drop
    }
  }
}
