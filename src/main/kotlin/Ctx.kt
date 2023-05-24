import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus

class Ctx private constructor(
  private val entries: PersistentList<Entry>,
  val env: Env,
  val types: Lvl,
) {
  fun next(): Lvl {
    return Lvl(entries.size)
  }

  fun nextVar(
    type: Lazy<Value>,
  ): Lazy<Value> {
    return lazyOf(Value.Var(next(), type))
  }

  fun extend(
    termBinder: String?,
    typeBinder: String?,
    type: Lazy<Value>,
    value: Lazy<Value>,
  ): Ctx {
    return if (termBinder == null) {
      Ctx(
        entries = entries,
        env = env,
        types = types + if (typeBinder == null) 0 else 1,
      )
    } else {
      Ctx(
        entries = entries + Entry(termBinder, type.value),
        env = env + value,
        types = types + if (typeBinder == null) 0 else 1,
      )
    }
  }

  fun lookup(
    name: String,
  ): Pair<Idx, Value>? {
    return when (val level = entries.indexOfLast { it.name == name }) {
      -1   -> null
      else -> Lvl(level).toIdx(next()) to entries[level].type
    }
  }

  private data class Entry(
    val name: String,
    val type: Value,
  )

  companion object {
    operator fun invoke(): Ctx {
      return Ctx(persistentListOf(), emptyEnv(), Lvl(0))
    }
  }
}
