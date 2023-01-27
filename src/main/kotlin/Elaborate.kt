import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus

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

val Ctx.top: Lvl
  get() {
    return Lvl(types.size)
  }

fun Ctx.freshVar(): Lazy<Value> {
  return lazyOf(Value.Var(top))
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

fun Ctx.elaborate(
  surface: Surface,
  expected: Value?,
): Result {
  return when {
    surface is Surface.Type &&
    expected == null          -> Result(Core.Type, Value.Type)

    surface is Surface.Func &&
    expected == null          -> {
      val param = elaborate(surface.param, Value.Type)
      val result = extend(surface.name, env.eval(param.core), freshVar()).elaborate(surface.result, Value.Type)
      Result(Core.Func(param.core, result.core), Value.Type)
    }

    surface is Surface.FuncOf -> when (expected) {
      is Value.Func -> {
        val freshVar = freshVar()
        val body = extend(surface.name, expected.param.value, freshVar).elaborate(surface.body, expected.result(freshVar))
        Result(Core.FuncOf(body.core), expected)
      }
      null          -> error("failed to synthesize: $surface")
      else          -> error("expected: func, actual: $expected")
    }

    surface is Surface.App &&
    expected == null          -> {
      val func = elaborate(surface.func, null)
      when (val funcType = func.type) {
        is Value.Func -> {
          val arg = elaborate(surface.arg, funcType.param.value)
          Result(Core.App(func.core, arg.core), funcType.result(lazy { env.eval(arg.core) }))
        }
        else          -> error("expected: func, actual: $funcType")
      }
    }

    surface is Surface.Let    -> {
      val init = elaborate(surface.init, null)
      val body = extend(surface.name, init.type, lazy { env.eval(init.core) }).elaborate(surface.body, expected)
      Result(Core.Let(init.core, body.core), expected ?: body.type)
    }

    surface is Surface.Var &&
    expected == null          -> when (val level = types.indexOfLast { (name, _) -> name == surface.name }) {
      -1   -> error("var not found: ${surface.name}")
      else -> Result(Core.Var(Lvl(level)), types[level].type)
    }

    surface is Surface.Anno &&
    expected == null          -> {
      val type = elaborate(surface.type, Value.Type)
      elaborate(surface.term, env.eval(type.core))
    }

    expected != null          -> {
      val actual = elaborate(surface, null)
      if (top.conv(expected, actual.type)) {
        actual
      } else {
        error("expected: $expected, actual: ${actual.type}")
      }
    }

    else                      -> error("unreachable")
  }
}
