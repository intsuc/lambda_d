sealed interface Surface {
  data object Type : Surface

  data class Func(
    val name: String,
    val param: Surface,
    val result: Surface,
  ) : Surface

  data class FuncOf(
    val name: String,
    val body: Surface,
  ) : Surface

  data class App(
    val func: Surface,
    val arg: Surface,
  ) : Surface

  data class Let(
    val name: String,
    val init: Surface,
    val body: Surface,
  ) : Surface

  data class Var(
    val name: String,
  ) : Surface

  data class Anno(
    val term: Surface,
    val type: Surface,
  ) : Surface
}
