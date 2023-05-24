import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus

class Ctx private constructor(
  private val types: PersistentList<Entry>,
  val env: Env,
) {
  fun next(): Lvl {
    return Lvl(types.size)
  }

  fun nextVar(
    type: Lazy<Value>,
  ): Lazy<Value> {
    return lazyOf(Value.Var(next(), type))
  }

  fun extend(
    name: String?,
    type: Lazy<Value>,
    value: Lazy<Value>,
  ): Ctx {
    return if (name == null) {
      this
    } else {
      Ctx(
        types = types + Entry(name, type.value),
        env = env + value,
      )
    }
  }

  fun lookup(
    name: String,
  ): Pair<Idx, Value>? {
    return when (val level = types.indexOfLast { it.name == name }) {
      -1   -> null
      else -> Lvl(level).toIdx(next()) to types[level].type
    }
  }

  private data class Entry(
    val name: String,
    val type: Value,
  )

  companion object {
    operator fun invoke(): Ctx {
      return Ctx(persistentListOf(), emptyEnv())
    }
  }
}
