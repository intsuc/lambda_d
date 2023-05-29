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

/**
 * Creates an empty environment.
 */
fun emptyEnv(): Env {
  return persistentListOf()
}

/**
 * Applies [this] [Closure] closure to [arg].
 */
operator fun Closure.invoke(
  arg: Lazy<V.Term>,
): V.Term {
  return (env + arg).eval(body)
}

/**
 * Returns the next level of [this] [Env] environment.
 */
fun Env.next(): Level {
  return Level(size)
}

/**
 * Returns the next variable of [type] under the context of [this] [Level] size.
 */
fun Level.nextVar(
  type: Lazy<V.Term>,
): Lazy<V.Term> {
  return lazyOf(V.Term.Var(this, type))
}

/**
 * Normalizes [term] under [this] [Env] environment.
 */
fun Env.normalize(
  term: C.Term,
): C.Term {
  return next().quote(eval(term))
}

/**
 * Evaluates [term] under [this] [Env] environment.
 */
fun Env.eval(
  term: C.Term,
): V.Term {
  return when (term) {
    is C.Term.Type   -> {
      V.Term.Type
    }

    is C.Term.Func   -> {
      val param = lazy { eval(term.param) }
      val result = Closure(this, term.result)
      V.Term.Func(param, result)
    }

    is C.Term.FuncOf -> {
      val body = Closure(this, term.body)
      val type = lazy { eval(term.type) }
      V.Term.FuncOf(body, type)
    }

    is C.Term.Apply  -> {
      val arg = lazy { eval(term.arg) }
      when (val func = eval(term.func)) {
        is V.Term.FuncOf -> {
          func.body(arg)
        }
        else             -> {
          val type = lazy { eval(term.type) }
          V.Term.Apply(func, arg, type)
        }
      }
    }

    is C.Term.Unit   -> {
      V.Term.Unit
    }

    is C.Term.UnitOf -> {
      V.Term.UnitOf
    }

    is C.Term.Pair   -> {
      val first = lazy { eval(term.first) }
      val second = Closure(this, term.second)
      V.Term.Pair(first, second)
    }

    is C.Term.PairOf -> {
      val first = lazy { eval(term.first) }
      val second = lazy { eval(term.second) }
      val type = lazy { eval(term.type) }
      V.Term.PairOf(first, second, type)
    }

    is C.Term.First  -> {
      when (val pair = eval(term.pair)) {
        is V.Term.PairOf -> {
          pair.first.value
        }
        else             -> {
          val type = lazy { eval(term.type) }
          V.Term.First(pair, type)
        }
      }
    }

    is C.Term.Second -> {
      when (val pair = eval(term.pair)) {
        is V.Term.PairOf -> {
          pair.second.value
        }
        else             -> {
          val type = lazy { eval(term.type) }
          V.Term.Second(pair, type)
        }
      }
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

/**
 * Quotes [term] under the environment of [this] [Level] size.
 */
fun Level.quote(
  term: V.Term,
): C.Term {
  return when (term) {
    is V.Term.Type   -> {
      C.Term.Type
    }

    is V.Term.Func   -> {
      val param = quote(term.param.value)
      val result = (this + 1).quote(term.result(nextVar(term.param)))
      C.Term.Func(param, result)
    }

    is V.Term.FuncOf -> {
      when (val funcType = term.type.value) {
        is V.Term.Func -> {
          val body = (this + 1).quote(term.body(nextVar(funcType.param)))
          val type = quote(term.type.value)
          C.Term.FuncOf(body, type)
        }
        else           -> error("expected func, got $funcType")
      }
    }

    is V.Term.Apply  -> {
      val func = quote(term.func)
      val arg = quote(term.arg.value)
      val type = quote(term.type.value)
      C.Term.Apply(func, arg, type)
    }

    is V.Term.Unit   -> {
      C.Term.Unit
    }

    is V.Term.UnitOf -> {
      C.Term.UnitOf
    }

    is V.Term.Pair   -> {
      val first = quote(term.first.value)
      val second = (this + 1).quote(term.second(nextVar(term.first)))
      C.Term.Pair(first, second)
    }

    is V.Term.PairOf -> {
      val first = quote(term.first.value)
      val second = quote(term.second.value)
      val type = quote(term.type.value)
      C.Term.PairOf(first, second, type)
    }

    is V.Term.First  -> {
      val pair = quote(term.pair)
      val type = quote(term.type.value)
      C.Term.First(pair, type)
    }

    is V.Term.Second -> {
      val pair = quote(term.pair)
      val type = quote(term.type.value)
      C.Term.Second(pair, type)
    }

    is V.Term.Var    -> {
      val index = term.level.toIndex(this)
      val type = quote(term.type.value)
      C.Term.Var(index, type)
    }
  }
}

/**
 * Checks if [term1] and [term2] are β-convertible under the environment of [this] [Level] size.
 */
fun Level.conv(
  term1: V.Term,
  term2: V.Term,
): Boolean {
  return when (term1) {
    is V.Term.Type   -> {
      term2 is V.Term.Type
    }

    is V.Term.Func   -> {
      term2 is V.Term.Func &&
      conv(term1.param.value, term2.param.value) &&
      nextVar(term1.param).let { (this + 1).conv(term1.result(it), term2.result(it)) }
    }

    is V.Term.FuncOf -> {
      term2 is V.Term.FuncOf &&
      nextVar((term1.type.value as V.Term.Func).param).let { (this + 1).conv(term1.body(it), term2.body(it)) }
    }

    is V.Term.Apply  -> {
      term2 is V.Term.Apply &&
      conv(term1.func, term2.func) &&
      conv(term1.arg.value, term2.arg.value)
    }

    is V.Term.Unit   -> {
      term2 is V.Term.Unit
    }

    is V.Term.UnitOf -> {
      term2 is V.Term.UnitOf
    }

    is V.Term.Pair   -> {
      term2 is V.Term.Pair &&
      conv(term1.first.value, term2.first.value) &&
      nextVar(term1.first).let { (this + 1).conv(term1.second(it), term2.second(it)) }
    }

    is V.Term.PairOf -> {
      term2 is V.Term.PairOf &&
      conv(term1.first.value, term2.first.value) &&
      conv(term1.second.value, term2.second.value)
    }

    is V.Term.First  -> {
      term2 is V.Term.First &&
      conv(term1.pair, term2.pair)
    }

    is V.Term.Second -> {
      term2 is V.Term.Second &&
      conv(term1.pair, term2.pair)
    }

    is V.Term.Var    -> {
      term2 is V.Term.Var &&
      term1.level == term2.level
    }
  }
}
