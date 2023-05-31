val TypeS: Surface.Term = Surface.Term.Type

fun Π(
  param: Surface.Term,
  result: Surface.Term,
): Surface.Term {
  return Surface.Term.Func(Surface.Pattern.Drop, param, result)
}

fun Π(
  binder: Surface.Pattern,
  param: Surface.Term,
  result: Surface.Term,
): Surface.Term {
  return Surface.Term.Func(binder, param, result)
}

fun λ(
  body: Surface.Term,
): Surface.Term {
  return Surface.Term.FuncOf(Surface.Pattern.Drop, body)
}

fun λ(
  binder: Surface.Pattern,
  body: Surface.Term,
): Surface.Term {
  return Surface.Term.FuncOf(binder, body)
}

operator fun Surface.Term.invoke(
  arg: Surface.Term,
): Surface.Term {
  return Surface.Term.Apply(this, arg)
}

@JvmField
val UnitS: Surface.Term = Surface.Term.Unit

@JvmField
val unitS: Surface.Term = Surface.Term.UnitOf

fun Σ(
  param: Surface.Term,
  result: Surface.Term,
): Surface.Term {
  return Surface.Term.Pair(Surface.Pattern.Drop, param, result)
}

fun Σ(
  binder: Surface.Pattern,
  param: Surface.Term,
  result: Surface.Term,
): Surface.Term {
  return Surface.Term.Pair(binder, param, result)
}

infix fun Surface.Term.to(
  second: Surface.Term,
): Surface.Term {
  return Surface.Term.PairOf(this, second)
}

val Surface.Term.first: Surface.Term
  get() {
    return Surface.Term.First(this)
  }

val Surface.Term.second: Surface.Term
  get() {
    return Surface.Term.Second(this)
  }

fun let(
  init: Surface.Term,
  body: Surface.Term,
): Surface.Term {
  return Surface.Term.Let(Surface.Pattern.Drop, init, body)
}

fun let(
  binder: Surface.Pattern,
  init: Surface.Term,
  body: Surface.Term,
): Surface.Term {
  return Surface.Term.Let(binder, init, body)
}

operator fun String.unaryPlus(): Surface.Term {
  return Surface.Term.Var(this)
}

infix fun Surface.Term.of(
  type: Surface.Term,
): Surface.Term {
  return Surface.Term.Anno(this, type)
}

infix fun Surface.Pattern.to(
  second: Surface.Pattern,
): Surface.Pattern {
  return Surface.Pattern.PairOf(this, second)
}

operator fun String.unaryMinus(): Surface.Pattern {
  return Surface.Pattern.Var(this)
}

val _p: Surface.Pattern = Surface.Pattern.Drop

val TypeC: Core.Term = Core.Term.Type

fun Π(
  param: Core.Term,
  result: Core.Term,
): Core.Term {
  return Core.Term.Func(param, result)
}

fun λ(
  binder: Core.Pattern,
  body: Core.Term,
  of: Core.Term,
): Core.Term {
  return Core.Term.FuncOf(binder, body, of)
}

operator fun Core.Term.invoke(
  arg: Core.Term,
  of: Core.Term,
): Core.Term {
  return Core.Term.Apply(this, arg, of)
}

@JvmField
val UnitC: Core.Term = Core.Term.Unit

@JvmField
val unitC: Core.Term = Core.Term.UnitOf

fun Σ(
  param: Core.Term,
  result: Core.Term,
): Core.Term {
  return Core.Term.Pair(param, result)
}

fun Core.Term.to(
  second: Core.Term,
  of: Core.Term,
): Core.Term {
  return Core.Term.PairOf(this, second, of)
}

fun Core.Term.first(
  of: Core.Term,
): Core.Term {
  return Core.Term.First(this, of)
}

fun Core.Term.second(
  of: Core.Term,
): Core.Term {
  return Core.Term.Second(this, of)
}

fun let(
  binder: Core.Pattern,
  init: Core.Term,
  body: Core.Term,
): Core.Term {
  return Core.Term.Let(binder, init, body)
}

fun v(
  index: Int,
  of: Core.Term,
): Core.Term {
  return Core.Term.Var(Idx(index), of)
}

val unitCp: Core.Pattern = Core.Pattern.UnitOf

fun Core.Pattern.to(
  second: Core.Pattern,
): Core.Pattern {
  return Core.Pattern.PairOf(this, second)
}

fun vp(
  name: String,
): Core.Pattern {
  return Core.Pattern.Var(name)
}

fun _p(): Core.Pattern {
  return Core.Pattern.Drop
}
