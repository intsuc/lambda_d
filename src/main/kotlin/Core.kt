sealed interface Core {
  object Type : Core

  data class Func(
    val param: Core,
    val result: Core,
  ) : Core

  data class FuncOf(
    val body: Core,
  ) : Core

  data class App(
    val func: Core,
    val arg: Core,
  ) : Core

  data class Let(
    val init: Core,
    val body: Core,
  ) : Core

  data class Var(
    val level: Int,
  ) : Core
}
