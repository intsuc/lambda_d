@JvmInline
value class Lvl(val value: Int) {
  fun suc(): Lvl {
    return Lvl(value + 1)
  }
}
