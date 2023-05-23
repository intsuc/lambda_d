import Core as C
import Surface as S

fun Π(
  param: S,
  result: S,
): S {
  return S.Func(null, param, result)
}

fun Π(
  name: String,
  param: S,
  result: S,
): S {
  return S.Func(name, param, result)
}

fun λ(
  body: S,
): S {
  return S.FuncOf(null, body)
}

fun λ(
  name: String,
  body: S,
): S {
  return S.FuncOf(name, body)
}

operator fun S.invoke(
  arg: S,
): S {
  return S.App(this, arg)
}

fun let(
  init: S,
  body: S,
): S {
  return S.Let(null, init, body)
}

fun let(
  name: String,
  init: S,
  body: S,
): S {
  return S.Let(name, init, body)
}

operator fun String.not(): S {
  return S.Var(this)
}

infix fun S.of(
  type: S,
): S {
  return S.Anno(this, type)
}

fun Π(
  param: C,
  result: C,
): C {
  return C.Func(null, param, result)
}

fun Π(
  name: String,
  param: C,
  result: C,
): C {
  return C.Func(name, param, result)
}

fun λ(
  body: C,
): C {
  return C.FuncOf(null, body)
}

fun λ(
  name: String,
  body: C,
): C {
  return C.FuncOf(name, body)
}

operator fun C.invoke(
  arg: C,
): C {
  return C.App(this, arg)
}

fun let(
  init: C,
  body: C,
): C {
  return C.Let(null, init, body)
}

fun let(
  name: String,
  init: C,
  body: C,
): C {
  return C.Let(name, init, body)
}

operator fun Int.not(): C {
  return C.Var(this)
}
