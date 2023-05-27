import Core
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import Core as C
import Surface as S
import Value as V

data class Result(
  val term: Core.Term,
  val type: V.Term,
)

infix fun Core.Term.of(type: V.Term): Result {
  return Result(this, type)
}

inline fun Ctx.resultOf(type: V.Term, build: (C.Term) -> C.Term): Result {
  return build(next().quote(type)) of type
}

@OptIn(ExperimentalContracts::class)
fun synth(type: V.Term?): Boolean {
  contract {
    returns(true) implies (type == null)
  }
  return type == null
}

@OptIn(ExperimentalContracts::class)
inline fun <reified T : V.Term> check(type: V.Term?): Boolean {
  contract {
    returns(true) implies (type is T)
  }
  return type is T
}

@OptIn(ExperimentalContracts::class)
inline fun <reified T : V.Term> match(type: V.Term?): Boolean {
  contract {
    returns(true) implies (type is T?)
  }
  return type is T?
}

@Suppress("NAME_SHADOWING")
fun Ctx.elaborate(
  term: S.Term,
  type: V.Term?,
): Result {
  return when {
    term is S.Term.Type &&
    synth(type)              -> {
      C.Term.Type of V.Term.Type
    }

    term is S.Term.Func &&
    synth(type)                  -> {
      val param = elaborate(term.param, V.Term.Type)
      val vParam = lazy { env.eval(param.term) }
      val result = extend(term.name, vParam, nextVar(vParam)).elaborate(term.result, V.Term.Type)
      C.Term.Func(param.term, result.term) of V.Term.Type
    }

    term is S.Term.FuncOf &&
    synth(type)                  -> {
      error("failed to synthesize: $term")
    }

    term is S.Term.FuncOf &&
    check<V.Term.Func>(type) -> {
      val param = lazyOf(type.param.value)
      val next = nextVar(param)
      val body = extend(term.name, param, next).elaborate(term.result, type.result(next))
      resultOf(type) { C.Term.FuncOf(body.term, it) }
    }

    term is S.Term.FuncOf &&
    check<V.Term>(type)      -> {
      error("expected: func, actual: $type")
    }

    term is S.Term.App &&
    synth(type)              -> {
      val func = elaborate(term.func, null)
      when (val funcType = func.type) {
        is V.Term.Func -> {
          val arg = elaborate(term.arg, funcType.param.value)
          val vArg = lazy { env.eval(arg.term) }
          val type = funcType.result(vArg)
          resultOf(type) { C.Term.App(func.term, arg.term, it) }
        }
        else           -> {
          error("expected: func, actual: $funcType")
        }
      }
    }

    term is S.Term.Unit &&
    synth(type)              -> {
      C.Term.Unit of V.Term.Type
    }

    term is S.Term.UnitOf &&
    synth(type)                  -> {
      C.Term.UnitOf of V.Term.Unit
    }

    term is S.Term.Pair &&
    synth(type)                  -> {
      val first = elaborate(term.first, V.Term.Type)
      val vFirst = lazy { env.eval(first.term) }
      val second = extend(term.name, vFirst, nextVar(vFirst)).elaborate(term.second, V.Term.Type)
      C.Term.Pair(first.term, second.term) of V.Term.Type
    }

    term is S.Term.PairOf &&
    match<V.Term.Pair>(type)     -> {
      val first = elaborate(term.first, type?.first?.value)
      val vFirst = lazy { env.eval(first.term) }
      val second = elaborate(term.second, type?.second(vFirst))
      val type = type ?: V.Term.Pair(lazyOf(first.type), Closure(env, next().quote(second.type)))
      resultOf(type) { C.Term.PairOf(first.term, second.term, it) }
    }

    term is S.Term.First &&
    synth(type)                  -> {
      val pair = elaborate(term.pair, null)
      when (val pairType = pair.type) {
        is V.Term.Pair -> {
          val type = pairType.first.value
          resultOf(type) { C.Term.First(pair.term, it) }
        }
        else           -> {
          error("expected: pair, actual: $pairType")
        }
      }
    }

    term is S.Term.Second &&
    synth(type)                  -> {
      val pair = elaborate(term.pair, null)
      when (val pairType = pair.type) {
        is V.Term.Pair -> {
          val vFirst = lazy { env.eval(C.Term.First(pair.term, next().quote(pairType.first.value))) }
          val type = pairType.second(vFirst)
          resultOf(type) { C.Term.Second(pair.term, it) }
        }
        else           -> {
          error("expected: pair, actual: $pairType")
        }
      }
    }

    term is S.Term.Let &&
    match<V.Term>(type)          -> {
      val init = elaborate(term.init, null)
      val vInit = lazy { env.eval(init.term) }
      val body = extend(term.name, lazyOf(init.type), vInit).elaborate(term.body, type)
      C.Term.Let(init.term, body.term) of (type ?: body.type)
    }

    term is S.Term.Var &&
    synth(type)                  -> {
      val (index, type) = lookup(term.name) ?: error("var not found: ${term.name}")
      resultOf(type) { C.Term.Var(index, it) }
    }

    term is S.Term.Anno &&
    synth(type)                  -> {
      val type = elaborate(term.type, V.Term.Type)
      val vType = env.eval(type.term)
      elaborate(term.target, vType)
    }

    check<V.Term>(type)      -> {
      val actual = elaborate(term, null)
      if (next().conv(type, actual.type)) {
        actual
      } else {
        error("expected: $type, actual: ${actual.type}")
      }
    }

    else                         -> {
      error("unreachable")
    }
  }
}
