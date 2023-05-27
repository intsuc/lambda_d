import Core as C
import Surface as S

val TypeS: S.Term = S.Term.Type

fun Π(
  param: S.Term,
  result: S.Term,
): S.Term {
  return S.Term.Func(null, param, result)
}

fun Π(
  name: String,
  param: S.Term,
  result: S.Term,
): S.Term {
  return S.Term.Func(name, param, result)
}

fun λ(
  body: S.Term,
): S.Term {
  return S.Term.FuncOf(null, body)
}

fun λ(
  name: String,
  body: S.Term,
): S.Term {
  return S.Term.FuncOf(name, body)
}

operator fun S.Term.invoke(
  arg: S.Term,
): S.Term {
  return S.Term.App(this, arg)
}

@JvmField
val UnitS: S.Term = S.Term.Unit

@JvmField
val unitS: S.Term = S.Term.UnitOf

fun Σ(
  param: S.Term,
  result: S.Term,
): S.Term {
  return S.Term.Pair(null, param, result)
}

fun Σ(
  name: String,
  param: S.Term,
  result: S.Term,
): S.Term {
  return S.Term.Pair(name, param, result)
}

infix fun S.Term.to(
  second: S.Term,
): S.Term {
  return S.Term.PairOf(this, second)
}

val S.Term.first: S.Term
  get() {
    return S.Term.First(this)
  }

val S.Term.second: S.Term
  get() {
    return S.Term.Second(this)
  }

fun let(
  init: S.Term,
  body: S.Term,
): S.Term {
  return S.Term.Let(null, init, body)
}

fun let(
  name: String,
  init: S.Term,
  body: S.Term,
): S.Term {
  return S.Term.Let(name, init, body)
}

fun v(
  name: String,
): S.Term {
  return S.Term.Var(name)
}

infix fun S.Term.of(
  type: S.Term,
): S.Term {
  return S.Term.Anno(this, type)
}

val TypeC: C.Term = C.Term.Type

fun Π(
  param: C.Term,
  result: C.Term,
): C.Term {
  return C.Term.Func(param, result)
}

fun λ(
  body: C.Term,
  of: C.Term,
): C.Term {
  return C.Term.FuncOf(body, of)
}

operator fun C.Term.invoke(
  arg: C.Term,
  of: C.Term,
): C.Term {
  return C.Term.App(this, arg, of)
}

@JvmField
val UnitC: C.Term = C.Term.Unit

@JvmField
val unitC: C.Term = C.Term.UnitOf

fun Σ(
  param: C.Term,
  result: C.Term,
): C.Term {
  return C.Term.Pair(param, result)
}

fun C.Term.to(
  second: C.Term,
  of: C.Term,
): C.Term {
  return C.Term.PairOf(this, second, of)
}

fun C.Term.first(
  of: C.Term,
): C.Term {
  return C.Term.First(this, of)
}

fun C.Term.second(
  of: C.Term,
): C.Term {
  return C.Term.Second(this, of)
}

fun let(
  init: C.Term,
  body: C.Term,
): C.Term {
  return C.Term.Let(init, body)
}

fun v(
  index: Int,
  of: C.Term,
): C.Term {
  return C.Term.Var(Index(index), of)
}
