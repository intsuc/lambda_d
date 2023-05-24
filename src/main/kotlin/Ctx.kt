import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus

class Ctx private constructor(
  private val entries: PersistentList<Entry>,
  val terms: Env,
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
    termName: String?,
    typeName: String?,
    type: Lazy<Value>,
    value: Lazy<Value>,
  ): Ctx {
    return if (termName == null) {
      Ctx(
        entries = entries,
        terms = terms,
        types = types + if (typeName == null) 0 else 1,
      )
    } else {
      Ctx(
        entries = entries + Entry(termName, type.value),
        terms = terms + value,
        types = types + if (typeName == null) 0 else 1,
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
