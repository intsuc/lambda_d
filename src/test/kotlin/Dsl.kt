import Core as C
import Surface as S

inline fun Π(
  name: String,
  param: S,
  result: S,
): S =
  S.Func(name, param, result)

inline fun λ(
  name: String,
  body: S,
): S =
  S.FuncOf(name, body)

inline operator fun S.invoke(
  arg: S,
): S =
  S.App(this, arg)

inline fun let(
  name: String,
  init: S,
  body: S,
): S =
  S.Let(name, init, body)

inline operator fun String.not(): S =
  S.Var(this)

inline infix fun S.of(
  type: S,
): S =
  S.Anno(this, type)

inline fun Π(
  param: C,
  result: C,
): C =
  C.Func(param, result)

inline fun λ(
  body: C,
): C =
  C.FuncOf(body)

inline operator fun C.invoke(
  arg: C,
): C =
  C.App(this, arg)

inline fun let(
  init: C,
  body: C,
): C =
  C.Let(init, body)

inline operator fun Int.not(): C =
  C.Var(lvl)
