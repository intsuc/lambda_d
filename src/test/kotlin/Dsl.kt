import Core as C
import Surface as S

val TypeS: S.Term = S.Term.Type

fun Π(
  name: S.Pattern,
  param: S.Term,
  result: S.Term,
): S.Term {
  return S.Term.Func(name, param, result)
}

fun λ(
  name: S.Pattern,
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
  name: S.Pattern,
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

val unitPS: S.Pattern = S.Pattern.UnitOf

fun vP(
  name: String,
): S.Pattern {
  return S.Pattern.Var(name)
}

val _S: S.Pattern.Drop = S.Pattern.Drop

infix fun S.Pattern.of(
  type: S.Term,
): S.Pattern {
  return S.Pattern.Anno(this, type)
}

val TypeC: C.Term = C.Term.Type

fun Π(
  name: C.Pattern,
  param: C.Term,
  result: C.Term,
): C.Term {
  return C.Term.Func(name, param, result)
}

fun λ(
  name: C.Pattern,
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
  name: C.Pattern,
  init: C.Term,
  body: C.Term,
): C.Term {
  return C.Term.Let(name, init, body)
}

fun v(
  name: String,
  index: Int,
  of: C.Term,
): C.Term {
  return C.Term.Var(name, Idx(index), of)
}

val unitPC: C.Pattern = C.Pattern.UnitOf

fun vP(
  name: String,
  of: C.Term,
): C.Pattern {
  return C.Pattern.Var(name, of)
}

fun _C(
  of: C.Term,
): C.Pattern.Drop {
  return C.Pattern.Drop(of)
}
