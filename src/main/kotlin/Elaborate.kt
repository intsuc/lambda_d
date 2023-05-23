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

val Ctx.next: Int
  get() {
    return types.size
  }

fun Ctx.nextVar(): Lazy<Value> {
  return lazyOf(Value.Var(next))
}

fun Ctx.extend(
  name: String?,
  type: Lazy<Value>,
  value: Lazy<Value>,
): Ctx {
  return if (name == null) {
    this
  } else {
    copy(
      types = types + Entry(name, type.value),
      env = env + value,
    )
  }
}

data class Result(
  val core: Core,
  val type: Value,
)

infix fun Core.of(type: Value): Result {
  return Result(this, type)
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
      val result = extend(surface.name, lazy { env.eval(param.core) }, nextVar()).elaborate(surface.result, Value.Type)
      Core.Func(surface.name, param.core, result.core) of Value.Type
    }

    surface is Surface.FuncOf &&
    synth(type)             -> {
      error("failed to synthesize: $surface")
    }

    surface is Surface.FuncOf &&
    check<Value.Func>(type) -> {
      val next = nextVar()
      val body = extend(surface.name, lazyOf(type.param.value), next).elaborate(surface.body, type.result(next))
      Core.FuncOf(surface.name, body.core) of type
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
      val body = extend(surface.name, lazyOf(init.type), lazy { env.eval(init.core) }).elaborate(surface.body, type)
      Core.Let(surface.name, init.core, body.core) of (type ?: body.type)
    }

    surface is Surface.Var &&
    synth(type)             -> when (val level = types.indexOfLast { (name, _) -> name == surface.name }) {
      -1   -> error("var not found: ${surface.name}")
      else -> Core.Var(level) of types[level].type
    }

    surface is Surface.Anno &&
    synth(type)             -> {
      @Suppress("NAME_SHADOWING")
      val type = elaborate(surface.type, Value.Type)
      elaborate(surface.target, env.eval(type.core))
    }

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
