sealed interface Core {
  data object Type : Core

  data class Func(
    val name: String?,
    val param: Core,
    val result: Core,
  ) : Core

  data class FuncOf(
    val name: String?,
    val body: Core,
  ) : Core

  data class App(
    val func: Core,
    val arg: Core,
  ) : Core

  data class Let(
    val name: String?,
    val init: Core,
    val body: Core,
  ) : Core

  data class Var(
    val level: Int,
  ) : Core
}
