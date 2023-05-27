import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import Value as V

/**
 * A context for elaboration.
 * [entries] and [env] are always the same size.
 */
class Ctx private constructor(
  private val entries: PersistentList<Entry>,
  val env: Env,
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
    name: String?,
    type: Lazy<V.Term>,
    value: Lazy<V.Term>,
  ): Ctx {
    return Ctx(
      entries = entries + Entry(name, type.value),
      env = env + value,
    )
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
    val name: String?,
    val type: V.Term,
  )

  companion object {
    operator fun invoke(): Ctx {
      return Ctx(persistentListOf(), emptyEnv())
    }
  }
}
