import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

data class Entry(
  val name: String,
  val type: Value,
)

data class Ctx(
  val types: PersistentList<Entry>,
  val env: Env,
)

fun emptyCtx(): Ctx {
  return Ctx(persistentListOf(), persistentListOf())
}

val Ctx.next: Lvl
  get() {
    return types.size.lvl
  }

fun Ctx.nextVar(): Lazy<Value> {
  return lazyOf(Value.Var(next))
}

fun Ctx.extend(
  name: String,
  type: Value,
  value: Lazy<Value>,
): Ctx {
  return copy(
    types = types + Entry(name, type),
    env = env + value,
  )
}

data class Result(
  val core: Core,
  val type: Value,
)

@OptIn(ExperimentalContracts::class)
inline fun synth(expected: Value?): Boolean {
  contract {
    returns(true) implies (expected == null)
  }
  return expected == null
}

@OptIn(ExperimentalContracts::class)
inline fun <reified V : Value> check(expected: Value?): Boolean {
  contract {
    returns(true) implies (expected is V)
  }
  return expected is V
}

@OptIn(ExperimentalContracts::class)
inline fun <reified V : Value> match(expected: Value?): Boolean {
  contract {
    returns(true) implies (expected is V?)
  }
  return expected is V?
}

fun Ctx.elaborate(
  surface: Surface,
  expected: Value?,
): Result {
  return when {
    surface is Surface.Type &&
    synth(expected)             -> {
      Result(Core.Type, Value.Type)
    }

    surface is Surface.Func &&
    synth(expected)             -> {
      val param = elaborate(surface.param, Value.Type)
      val result = extend(surface.name, env.eval(param.core), nextVar()).elaborate(surface.result, Value.Type)
      Result(Core.Func(param.core, result.core), Value.Type)
    }

    surface is Surface.FuncOf &&
    synth(expected)             -> {
      error("failed to synthesize: $surface")
    }

    surface is Surface.FuncOf &&
    check<Value.Func>(expected) -> {
      val next = nextVar()
      val body = extend(surface.name, expected.param.value, next).elaborate(surface.body, expected.result(next))
      Result(Core.FuncOf(body.core), expected)
    }

    surface is Surface.FuncOf &&
    check<Value>(expected)      -> {
      error("expected: func, actual: $expected")
    }

    surface is Surface.App &&
    synth(expected)             -> {
      val func = elaborate(surface.func, null)
      when (val funcType = func.type) {
        is Value.Func -> {
          val arg = elaborate(surface.arg, funcType.param.value)
          Result(Core.App(func.core, arg.core), funcType.result(lazy { env.eval(arg.core) }))
        }
        else          -> error("expected: func, actual: $funcType")
      }
    }

    surface is Surface.Let &&
    match<Value>(expected)      -> {
      val init = elaborate(surface.init, null)
      val body = extend(surface.name, init.type, lazy { env.eval(init.core) }).elaborate(surface.body, expected)
      Result(Core.Let(init.core, body.core), expected ?: body.type)
    }

    surface is Surface.Var &&
    synth(expected)             -> when (val level = types.indexOfLast { (name, _) -> name == surface.name }) {
      -1   -> error("var not found: ${surface.name}")
      else -> Result(Core.Var(level.lvl), types[level].type)
    }

    surface is Surface.Anno &&
    synth(expected)             -> {
      val type = elaborate(surface.type, Value.Type)
      elaborate(surface.term, env.eval(type.core))
    }

    surface is Surface &&
    check<Value>(expected)      -> {
      val actual = elaborate(surface, null)
      if (next.conv(expected, actual.type)) {
        actual
      } else {
        error("expected: $expected, actual: ${actual.type}")
      }
    }

    else                        -> error("unreachable")
  }
}
