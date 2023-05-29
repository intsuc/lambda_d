/**
 * A de Bruijn index.
 */
@JvmInline
value class Index(val value: Int)

/**
 * A de Bruijn level.
 */
@JvmInline
value class Level(val value: Int) {
  operator fun plus(offset: Int): Level {
    return Level(value + offset)
  }
}

/**
 * Converts [this] [Index] de Bruijn index [Index] to the corresponding de Bruijn level [Level] in an environment of [size].
 */
fun Index.toLevel(size: Level): Level {
  return Level(size.value - this.value - 1).also { check(it.value >= 0) }
}

/**
 * Converts [this] [Index] de Bruijn level [Level] to the corresponding de Bruijn index [Index] in an environment of [size].
 */
fun Level.toIndex(size: Level): Index {
  return Index(size.value - this.value - 1).also { check(it.value >= 0) }
}
