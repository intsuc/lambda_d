import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import Core as C
import Surface as S
import Value as V

data class Result<out T>(
  val value: T,
  val type: V.Term,
)

infix fun <T> T.of(type: V.Term): Result<T> {
  return Result(this, type)
}

inline fun <T> Ctx.resultOf(type: V.Term, build: (type: C.Term) -> T): Result<T> {
  return build(quoteTerm(env1.next(), Lvl(0), type)) of type
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
fun Ctx.elaborateTerm(
  term: S.Term,
  type: V.Term?,
): Result<C.Term> {
  return when {
    term is S.Term.Type &&
    synth(type)              -> {
      C.Term.Type of V.Term.Type
    }

    term is S.Term.Func &&
    synth(type)              -> {
      val param = elaborateTerm(term.param, V.Term.Type)
      val vParam = evalTerm(env0, env0, param.value)
      val binder = elaboratePattern(term.binder, null, vParam)
      val vBinder = evalPattern(env1, binder.value.first)
      with(binder.value.second) {
        val result = bind(vBinder, null).elaborateTerm(term.result, V.Term.Type)
        C.Term.Func(binder.value.first, param.value, result.value) of V.Term.Type
      }
    }

    term is S.Term.FuncOf &&
    synth(type)              -> {
      error("failed to synthesize: $term")
    }

    term is S.Term.FuncOf &&
    check<V.Term.Func>(type) -> {
      val binder = elaboratePattern(term.binder, type.result.binder0, type.param.value)
      with(binder.value.second) {
        val body = elaborateTerm(term.body, type.result.open())
        resultOf(type) { C.Term.FuncOf(binder.value.first, body.value, it) }
      }
    }

    term is S.Term.FuncOf &&
    check<V.Term>(type)      -> {
      error("expected: func, actual: $type")
    }

    term is S.Term.App &&
    synth(type)              -> {
      val func = elaborateTerm(term.func, null)
      when (val funcType = func.type) {
        is V.Term.Func -> {
          val arg = elaborateTerm(term.arg, funcType.param.value)
          val vArg = lazy { evalTerm(env0, env0, arg.value) }
          val type = funcType.result(vArg)
          resultOf(type) { C.Term.App(func.value, arg.value, it) }
        }
        else           -> error("expected: func, actual: $funcType")
      }
    }

    term is S.Term.Unit &&
    synth(type)              -> {
      C.Term.Unit of V.Term.Type
    }

    term is S.Term.UnitOf &&
    synth(type)              -> {
      C.Term.UnitOf of V.Term.Unit
    }

    term is S.Term.Let &&
    match<V.Term>(type)      -> {
      val binder = elaboratePattern(term.binder, null, null)
      val vBinder = evalPattern(env1, binder.value.first)
      with(binder.value.second) {
        val init = elaborateTerm(term.init, binder.type)
        val vInit = lazy { evalTerm(env0, env0, init.value) }
        val body = define(vBinder, vBinder, vInit).elaborateTerm(term.body, type)
        C.Term.Let(binder.value.first, init.value, body.value) of (type ?: body.type)
      }
    }

    term is S.Term.Var &&
    synth(type)              -> {
      val (index, type) = lookup(term.name) ?: error("var not found: ${term.name}")
      resultOf(type) { C.Term.Var(term.name, index, it) }
    }

    term is S.Term.Anno &&
    synth(type)              -> {
      val type = elaborateTerm(term.type, V.Term.Type)
      val vType = evalTerm(env0, env0, type.value)
      elaborateTerm(term.target, vType)
    }

    check<V.Term>(type)      -> {
      val actual = elaborateTerm(term, null)
      if (next().convTerm(type, actual.type)) {
        actual
      } else {
        error("expected: $type, actual: ${actual.type}")
      }
    }

    else                     -> {
      error("unreachable")
    }
  }
}

fun Ctx.elaboratePattern(
  pattern0: S.Pattern,
  pattern1: V.Pattern?,
  type: V.Term?,
): Result<Pair<C.Pattern, Ctx>> {
  return when {
    pattern0 is S.Pattern.UnitOf &&
    synth(type)         -> {
      C.Pattern.UnitOf to this of V.Term.Unit
    }

    pattern0 is S.Pattern.Var &&
    synth(type)         -> {
      error("failed to synthesize: $pattern0")
    }

    pattern0 is S.Pattern.Var &&
    check<V.Term>(type) -> {
      resultOf(type) {
        val pattern = C.Pattern.Var(pattern0.name, it)
        val vBinder0 = evalPattern(env1, pattern)
        val ctx = bind(vBinder0, pattern1)
        pattern to ctx
      }
    }

    pattern0 is S.Pattern.Drop &&
    synth(type)         -> {
      error("failed to synthesize: $pattern0")
    }

    pattern0 is S.Pattern.Drop &&
    check<V.Term>(type) -> {
      resultOf(type) {
        val pattern = C.Pattern.Drop(it)
        val vBinder0 = evalPattern(env1, pattern)
        val ctx = bind(vBinder0, pattern1)
        pattern to ctx
      }
    }

    check<V.Term>(type) -> {
      val actual = elaboratePattern(pattern0, pattern1, null)
      if (next().convTerm(type, actual.type)) {
        actual
      } else {
        error("expected: $type, actual: ${actual.type}")
      }
    }

    else                -> {
      error("unreachable")
    }
  }
}
