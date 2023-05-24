import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

data class Result(
  val core: Core,
  val type: Value,
)

infix fun Core.of(type: Value): Result {
  return Result(this, type)
}

inline fun Ctx.of(type: Value, build: (Core) -> Core): Result {
  return build(types.quote(type)) of type
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
  surface: Surface,
  type: Value?,
): Result {
  return when {
    surface is Surface.Type &&
    synth(type)             -> {
      Core.Type of Value.Type
    }

    surface is Surface.Func &&
    synth(type)             -> {
      val param = elaborate(surface.param, Value.Type)
      val vParam = lazy { env.eval(param.core) }
      val result = extend(surface.name, null, vParam, nextVar(vParam)).elaborate(surface.result, Value.Type)
      Core.Func(surface.name, param.core, result.core) of Value.Type
    }

    surface is Surface.FuncOf &&
    synth(type)             -> {
      error("failed to synthesize: $surface")
    }

    surface is Surface.FuncOf &&
    check<Value.Func>(type) -> {
      val param = lazyOf(type.param.value)
      val next = nextVar(param)
      val body = extend(surface.name, type.result.name, param, next).elaborate(surface.body, type.result(next))
      of(type) { Core.FuncOf(surface.name, body.core, it) }
    }

    surface is Surface.FuncOf &&
    check<Value>(type)      -> {
      error("expected: func, actual: $type")
    }

    surface is Surface.App &&
    synth(type)             -> {
      val func = elaborate(surface.func, null)
      when (val funcType = func.type) {
        is Value.Func -> {
          val arg = elaborate(surface.arg, funcType.param.value)
          val type = funcType.result(lazy { env.eval(arg.core) })
          of(type) { Core.App(func.core, arg.core, it) }
        }
        else          -> error("expected: func, actual: $funcType")
      }
    }

    surface is Surface.Unit &&
    synth(type) -> {
      Core.Unit of Value.Type
    }

    surface is Surface.UnitOf &&
    synth(type) -> {
      Core.UnitOf of Value.Unit
    }

    surface is Surface.Let &&
    match<Value>(type)      -> {
      val init = elaborate(surface.init, null)
      val body = extend(surface.name, surface.name, lazyOf(init.type), lazy { env.eval(init.core) }).elaborate(surface.body, type)
      Core.Let(surface.name, init.core, body.core) of (type ?: body.type)
    }

    surface is Surface.Var &&
    synth(type)             -> {
      val (index, type) = lookup(surface.name) ?: error("var not found: ${surface.name}")
      of(type) { Core.Var(index, it) }
    }

    surface is Surface.Anno &&
    synth(type)             -> {
      val type = elaborate(surface.type, Value.Type)
      elaborate(surface.target, env.eval(type.core))
    }

    check<Value>(type)      -> {
      val actual = elaborate(surface, null)
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
