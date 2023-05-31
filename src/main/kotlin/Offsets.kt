/**
 * A de Bruijn index.
 */
@JvmInline
value class Idx(val value: Int)

/**
 * A de Bruijn level.
 */
@JvmInline
value class Lvl(val value: Int) {
  operator fun plus(offset: Int): Lvl {
    return Lvl(value + offset)
  }
}

/**
 * Converts [this] [Idx] de Bruijn index [Idx] to the corresponding de Bruijn level [Lvl] in an environment of [size].
 */
fun Idx.toLevel(size: Lvl): Lvl {
  return Lvl(size.value - this.value - 1).also {
    check(it.value >= 0)
  }
}

/**
 * Converts [this] [Idx] de Bruijn level [Lvl] to the corresponding de Bruijn index [Idx] in an environment of [size].
 */
fun Lvl.toIndex(size: Lvl): Idx {
  return Idx(size.value - this.value - 1).also {
    check(it.value >= 0)
  }
}
