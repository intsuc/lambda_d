import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import Value as V

/**
 * A context for elaboration.
 * [entries] and [terms] are always the same size.
 * [types] may or may not be the same size as [entries] and [terms], depending on the desynchronization of term-level and type-level bindings.
 * We only need to keep track of [types] as [Level] because it is only used to [quote] types.
 */
class Ctx private constructor(
  private val entries: PersistentList<Entry>,
  val terms: Env,
  val types: Level,
) {
  fun next(): Level {
    return Level(entries.size)
  }

  fun nextVar(
    type: Lazy<V.Term>,
  ): Lazy<V.Term> {
    return lazyOf(V.Term.Var(next(), type))
  }

  fun extend(
    termName: String?,
    typeName: String?,
    type: Lazy<V.Term>,
    value: Lazy<V.Term>,
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
  ): Pair<Index, V.Term>? {
    return when (val level = entries.indexOfLast { it.name == name }) {
      -1   -> null
      else -> Level(level).toIndex(next()) to entries[level].type
    }
  }

  private data class Entry(
    val name: String,
    val type: V.Term,
  )

  companion object {
    operator fun invoke(): Ctx {
      return Ctx(persistentListOf(), emptyEnv(), Level(0))
    }
  }
}
