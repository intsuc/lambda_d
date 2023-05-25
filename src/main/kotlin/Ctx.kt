import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import Value as V

/**
 * A context for elaboration.
 * [entries] and [env0] are always the same size.
 * [env1] may or may not be the same size as [entries] and [env0], depending on the desynchronization of bindings.
 */
class Ctx private constructor(
  private val entries: PersistentList<Entry>,
  val env0: Env,
  val env1: Env,
) {
  fun next(): Lvl {
    return Lvl(entries.size)
  }

  fun bind(
    binder0: V.Pattern,
    binder1: V.Pattern?,
  ): Ctx {
    val vars0 = env0.next().vars(binder0)
    return Ctx(
      entries = entries + vars0.map { Entry(it.value.name, it.value.type.value) },
      env0 = env0 + vars0,
      env1 = env1 + (binder1?.let { env1.next().vars(binder1) } ?: emptyList()),
    )
  }

  fun define(
    binder0: V.Pattern,
    binder1: V.Pattern?,
    value: Lazy<V.Term>,
  ): Ctx {
    val vars0 = env0.next().vars(binder0)
    return Ctx(
      entries = entries + vars0.map { Entry(it.value.name, it.value.type.value) },
      env0 = env0 + (binder0 matches value).also { check(it.size == vars0.size) },
      env1 = env1 + (binder1?.let { it matches value } ?: emptyList()),
    )
  }

  fun lookup(
    name: String,
  ): Pair<Idx, V.Term>? {
    return when (val level = entries.indexOfLast { it.name == name }) {
      -1   -> null
      else -> Lvl(level).toIdx(next()) to entries[level].type
    }
  }

  private data class Entry(
    val name: String,
    val type: V.Term,
  )

  companion object {
    operator fun invoke(): Ctx {
      return Ctx(persistentListOf(), emptyEnv(), emptyEnv())
    }
  }
}
