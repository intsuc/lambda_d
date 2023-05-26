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
  return C.Term.Func(null, param, result)
}

fun Π(
  name: String,
  param: C.Term,
  result: C.Term,
): C.Term {
  return C.Term.Func(name, param, result)
}

fun λ(
  body: C.Term,
  of: C.Term,
): C.Term {
  return C.Term.FuncOf(null, body, of)
}

fun λ(
  name: String,
  body: C.Term,
  of: C.Term,
): C.Term {
  return C.Term.FuncOf(name, body, of)
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

fun let(
  init: C.Term,
  body: C.Term,
): C.Term {
  return C.Term.Let(null, init, body)
}

fun let(
  name: String,
  init: C.Term,
  body: C.Term,
): C.Term {
  return C.Term.Let(name, init, body)
}

fun v(
  index: Int,
  of: C.Term,
): C.Term {
  return C.Term.Var(Idx(index), of)
}
