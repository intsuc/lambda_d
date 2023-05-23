sealed class Core {
  abstract val type: Core

  data object Univ : Core() {
    // Type in type
    override val type: Core get() = Univ
  }

  data class Func(
    val name: String?,
    val param: Core,
    val result: Core,
  ) : Core() {
    override val type: Core get() = Univ
  }

  data class FuncOf(
    val name: String?,
    val body: Core,
    override val type: Core,
  ) : Core()

  data class App(
    val func: Core,
    val arg: Core,
    override val type: Core,
  ) : Core()

  data class Let(
    val name: String?,
    val init: Core,
    val body: Core,
  ) : Core() {
    // Store the type of the body to avoid cascading field accesses
    override val type: Core = body.type
  }

  data class Var(
    val index: Idx,
    override val type: Core,
  ) : Core()
}
