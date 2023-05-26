import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import Core as C
import Surface as S

// TODO: redundant?
data class Result(
  val term: C.Term,
  val type: Value,
)

infix fun C.Term.of(type: Value): Result {
  return Result(this, type)
}

inline fun Ctx.of(type: Value, build: (C.Term) -> C.Term): Result {
  return build(quote(types, Lvl(0), type)) of type
}

@OptIn(ExperimentalContracts::class)
fun synth(type: Value?): Boolean {
  contract {
    returns(true) implies (type == null)
  }
  return type == null
}

@OptIn(ExperimentalContracts::class)
inline fun <reified V : Value> check(type: Value?): Boolean {
  contract {
    returns(true) implies (type is V)
  }
  return type is V
}

@OptIn(ExperimentalContracts::class)
inline fun <reified V : Value> match(type: Value?): Boolean {
  contract {
    returns(true) implies (type is V?)
  }
  return type is V?
}

@Suppress("NAME_SHADOWING")
fun Ctx.elaborate(
  term: S.Term,
  type: Value?,
): Result {
  return when {
    term is S.Term.Type &&
    synth(type)             -> {
      C.Term.Type of Value.Type
    }

    term is S.Term.Func &&
    synth(type)             -> {
      val param = elaborate(term.param, Value.Type)
      val vParam = lazy { eval(terms, terms, param.term) }
      val result = extend(term.name, null, vParam, nextVar(vParam)).elaborate(term.result, Value.Type)
      C.Term.Func(term.name, param.term, result.term) of Value.Type
    }

    term is S.Term.FuncOf &&
    synth(type)             -> {
      error("failed to synthesize: $term")
    }

    term is S.Term.FuncOf &&
    check<Value.Func>(type) -> {
      val param = lazyOf(type.param.value)
      val next = nextVar(param)
      val body = extend(term.name, type.result.termName, param, next).elaborate(term.body, type.result(next))
      of(type) { C.Term.FuncOf(term.name, body.term, it) }
    }

    term is S.Term.FuncOf &&
    check<Value>(type)      -> {
      error("expected: func, actual: $type")
    }

    term is S.Term.App &&
    synth(type)             -> {
      val func = elaborate(term.func, null)
      when (val funcType = func.type) {
        is Value.Func -> {
          val arg = elaborate(term.arg, funcType.param.value)
          val vArg = lazy { eval(terms, terms, arg.term) }
          val type = funcType.result(vArg)
          of(type) { C.Term.App(func.term, arg.term, it) }
        }
        else          -> error("expected: func, actual: $funcType")
      }
    }

    term is S.Term.Unit &&
    synth(type) -> {
      C.Term.Unit of Value.Type
    }

    term is S.Term.UnitOf &&
    synth(type) -> {
      C.Term.UnitOf of Value.Unit
    }

    term is S.Term.Let &&
    match<Value>(type)      -> {
      val init = elaborate(term.init, null)
      val vInit = lazy { eval(terms, terms, init.term) }
      val body = extend(term.name, term.name, lazyOf(init.type), vInit).elaborate(term.body, type)
      C.Term.Let(term.name, init.term, body.term) of (type ?: body.type)
    }

    term is S.Term.Var &&
    synth(type)             -> {
      val (index, type) = lookup(term.name) ?: error("var not found: ${term.name}")
      of(type) { C.Term.Var(index, it) }
    }

    term is S.Term.Anno &&
    synth(type)             -> {
      val type = elaborate(term.type, Value.Type)
      val vType = eval(terms, terms, type.term)
      elaborate(term.target, vType)
    }

    check<Value>(type)      -> {
      val actual = elaborate(term, null)
      if (next().conv(type, actual.type)) {
        actual
      } else {
        error("expected: $type, actual: ${actual.type}")
      }
    }

    else                    -> {
      error("unreachable")
    }
  }
}
