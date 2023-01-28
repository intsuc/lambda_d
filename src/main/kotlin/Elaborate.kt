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

inline fun emptyCtx(): Ctx {
  return Ctx(persistentListOf(), persistentListOf())
}

inline val Ctx.next: Lvl
  get() {
    return types.size.lvl
  }

inline fun Ctx.nextVar(): Lazy<Value> {
  return lazyOf(Value.Var(next))
}

inline fun Ctx.extend(
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

inline infix fun Core.of(type: Value): Result {
  return Result(this, type)
}

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
      val result = extend(surface.name, env.eval(param.core), nextVar()).elaborate(surface.result, Value.Type)
      Core.Func(param.core, result.core) of Value.Type
    }

    surface is Surface.FuncOf &&
    synth(type)             -> {
      error("failed to synthesize: $surface")
    }

    surface is Surface.FuncOf &&
    check<Value.Func>(type) -> {
      val next = nextVar()
      val body = extend(surface.name, type.param.value, next).elaborate(surface.body, type.result(next))
      Core.FuncOf(body.core) of type
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
          Core.App(func.core, arg.core) of funcType.result(lazy { env.eval(arg.core) })
        }
        else          -> error("expected: func, actual: $funcType")
      }
    }

    surface is Surface.Let &&
    match<Value>(type)      -> {
      val init = elaborate(surface.init, null)
      val body = extend(surface.name, init.type, lazy { env.eval(init.core) }).elaborate(surface.body, type)
      Core.Let(init.core, body.core) of (type ?: body.type)
    }

    surface is Surface.Var &&
    synth(type)             -> when (val level = types.indexOfLast { (name, _) -> name == surface.name }) {
      -1   -> error("var not found: ${surface.name}")
      else -> Core.Var(level.lvl) of types[level].type
    }

    surface is Surface.Anno &&
    synth(type)             -> {
      val type = elaborate(surface.type, Value.Type)
      elaborate(surface.term, env.eval(type.core))
    }

    surface is Surface &&
    check<Value>(type)      -> {
      val actual = elaborate(surface, null)
      if (next.conv(type, actual.type)) {
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
