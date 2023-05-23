@JvmInline
value class Idx(val value: Int)

@JvmInline
value class Lvl(val value: Int) {
  operator fun plus(offset: Int): Lvl {
    return Lvl(value + offset)
  }
}

fun Idx.toLvl(next: Lvl): Lvl {
  return Lvl(next.value - this.value - 1)
}

fun Lvl.toIdx(next: Lvl): Idx {
  return Idx(next.value - this.value - 1)
}
