@JvmInline
value class Lvl(val value: Int) {
  fun suc(): Lvl {
    return (value + 1).lvl
  }
}

inline val Int.lvl: Lvl
  get() {
    return Lvl(this)
  }
