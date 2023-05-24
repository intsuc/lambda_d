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
 * Converts [this] de Bruijn index [Idx] to the corresponding de Bruijn level [Lvl] in a context of [size].
 */
fun Idx.toLvl(size: Lvl): Lvl {
  return Lvl(size.value - this.value - 1).also { check(it.value >= 0) }
}

/**
 * Converts [this] de Bruijn level [Lvl] to the corresponding de Bruijn index [Idx] in a context of [size].
 */
fun Lvl.toIdx(size: Lvl): Idx {
  return Idx(size.value - this.value - 1).also { check(it.value >= 0) }
}
