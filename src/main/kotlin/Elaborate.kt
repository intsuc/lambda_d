import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.plus
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import Core as C
import Surface as S
import Value as V

/**
 * A context for elaboration.
 */
data class Ctx(
  val entries: PersistentList<Entry>,
  val env: Env,
) {
  data class Entry(
    val name: String,
    val type: V.Term,
    val term: V.Term,
  )
}

/**
 * Creates an empty context.
 */
fun emptyCtx(): Ctx {
  return Ctx(persistentListOf(), emptyEnv())
}

/**
 * Returns the next level under [this] [Ctx] context.
 */
fun Ctx.next(): Level {
  return Level(env.size)
}

/**
 * Returns the next variable of [type] under [this] [Ctx] context.
 */
fun Ctx.nextVar(
  type: Lazy<V.Term>,
): Lazy<V.Term> {
  return lazyOf(V.Term.Var(next(), type))
}

/**
 * An elaboration result.
 */
data class Result(
  val term: C.Term,
  val type: V.Term,
)

infix fun C.Term.of(type: V.Term): Result {
  return Result(this, type)
}

inline fun Ctx.resultOf(type: V.Term, build: (C.Term) -> C.Term): Result {
  return build(next().quote(type)) of type
}

@OptIn(ExperimentalContracts::class)
fun synth(type: V.Term?): Boolean {
  contract {
    returns(true) implies (type == null)
  }
  return type == null
}

@OptIn(ExperimentalContracts::class)
inline fun <reified T : V.Term> check(type: V.Term?): Boolean {
  contract {
    returns(true) implies (type is T)
  }
  return type is T
}

@OptIn(ExperimentalContracts::class)
inline fun <reified T : V.Term> match(type: V.Term?): Boolean {
  contract {
    returns(true) implies (type is T?)
  }
  return type is T?
}

/**
 * Elaborates [term] under [this] [Ctx] context.
 * - If [type] is `null`, synthesizes the type of [term].
 * - If [type] is not `null`, checks that [term] against [type].
 */
@Suppress("NAME_SHADOWING")
fun Ctx.elaborate(
  term: S.Term,
  type: V.Term?,
): Result {
  return when {
    /**
     * ---------------
     * Γ ⊢ Type ⇒ Type
     */
    term is S.Term.Type && synth(type)                -> {
      C.Term.Type of V.Term.Type
    }

    /**
     * Γ ⊢ A ⇐ Type
     * Γ ⊢ P ⇐ A ⊣ Γ'
     * Γ' ⊢ B ⇐ Type
     * ----------------------
     * Γ ⊢ Π(P : A). B ⇒ Type
     */
    term is S.Term.Func && synth(type)                -> {
      val param = elaborate(term.param, V.Term.Type)
      val vParam = env.eval(param.term)
      val result = extend(term.binder, vParam, nextVar(lazyOf(vParam))).elaborate(term.result, V.Term.Type)
      C.Term.Func(param.term, result.term) of V.Term.Type
    }

    term is S.Term.FuncOf && synth(type)              -> {
      error("failed to synthesize: $term")
    }

    /**
     * Γ ⊢ A ⇐ Type
     * Γ ⊢ p ⇐ A ⊣ Γ'
     * Γ' ⊢ b ⇐ B(p)
     * -----------------------
     * Γ ⊢ λp. b ⇐ Π(P : A). B
     */
    term is S.Term.FuncOf && check<V.Term.Func>(type) -> {
      val param = type.param.value
      val next = nextVar(lazyOf(param))
      val body = extend(term.binder, param, next).elaborate(term.result, type.result(next))
      resultOf(type) { C.Term.FuncOf(body.term, it) }
    }

    term is S.Term.FuncOf && check<V.Term>(type)      -> {
      error("expected: func, actual: $type")
    }

    /**
     * Γ ⊢ f ⇒ Π(P : A). B
     * Γ ⊢ a ⇐ A
     * -------------------
     * Γ ⊢ f a ⇒ B(a)
     */
    term is S.Term.Apply && synth(type)               -> {
      val func = elaborate(term.func, null)
      when (val funcType = func.type) {
        is V.Term.Func -> {
          val arg = elaborate(term.arg, funcType.param.value)
          val vArg = lazy { env.eval(arg.term) }
          val type = funcType.result(vArg)
          resultOf(type) { C.Term.Apply(func.term, arg.term, it) }
        }
        else           -> {
          error("expected: func, actual: $funcType")
        }
      }
    }

    /**
     * ---------------
     * Γ ⊢ Unit ⇒ Type
     */
    term is S.Term.Unit && synth(type)                -> {
      C.Term.Unit of V.Term.Type
    }

    /**
     * -------------
     * Γ ⊢ () ⇒ Unit
     */
    term is S.Term.UnitOf && synth(type)              -> {
      C.Term.UnitOf of V.Term.Unit
    }

    /**
     * Γ ⊢ A ⇐ Type
     * Γ ⊢ P ⇐ A ⊣ Γ'
     * Γ' ⊢ B ⇐ Type
     * ----------------------
     * Γ ⊢ Σ(P : A). B ⇒ Type
     */
    term is S.Term.Pair && synth(type)                -> {
      val first = elaborate(term.first, V.Term.Type)
      val vFirst = env.eval(first.term)
      val second = extend(term.binder, vFirst, nextVar(lazyOf(vFirst))).elaborate(term.second, V.Term.Type)
      C.Term.Pair(first.term, second.term) of V.Term.Type
    }

    /**
     * Γ ⊢ a ⇔ A
     * Γ ⊢ b ⇔ B(a)
     * ---------------------------
     * Γ ⊢ (a, b) ⇔ Σ(P : A). B(a)
     */
    term is S.Term.PairOf && match<V.Term.Pair>(type) -> {
      val first = elaborate(term.first, type?.first?.value)
      val vFirst = lazy { env.eval(first.term) }
      val second = elaborate(term.second, type?.second(vFirst))

      // We need to recreate the type here to store and return the pair type whose second component is applied to the first component of the pair.
      val type = V.Term.Pair(lazyOf(first.type), Closure(env, next().quote(second.type)))

      resultOf(type) { C.Term.PairOf(first.term, second.term, it) }
    }

    /**
     * Γ ⊢ t ⇒ Σ(P : A). B
     * -------------------
     * Γ ⊢ t.1 ⇒ A
     */
    term is S.Term.First && synth(type)               -> {
      val pair = elaborate(term.pair, null)
      when (val pairType = pair.type) {
        is V.Term.Pair -> {
          val type = pairType.first.value
          resultOf(type) { C.Term.First(pair.term, it) }
        }
        else           -> {
          error("expected: pair, actual: $pairType")
        }
      }
    }

    /**
     * Γ ⊢ t ⇒ Σ(P : A). B
     * -------------------
     * Γ ⊢ t.2 ⇒ B(t.1)
     */
    term is S.Term.Second && synth(type)              -> {
      val pair = elaborate(term.pair, null)
      when (val pairType = pair.type) {
        is V.Term.Pair -> {
          // We inject the first projection into the pair to represent the first component of the pair, whatever form the pair is in.
          val vFirst = lazy { env.eval(C.Term.First(pair.term, next().quote(pairType.first.value))) }

          val type = pairType.second(vFirst)
          resultOf(type) { C.Term.Second(pair.term, it) }
        }
        else           -> {
          error("expected: pair, actual: $pairType")
        }
      }
    }

    /**
     * Γ ⊢ a ⇒ A
     * Γ ⊢ p ⇐ A ⊣ Γ'
     * Γ' ⊢ b ⇔ B
     * --------------------
     * Γ ⊢ let p = a; b ⇔ B
     */
    term is S.Term.Let && match<V.Term>(type)         -> {
      val init = elaborate(term.init, null)
      val vInit = lazy { env.eval(init.term) }
      val body = extend(term.binder, init.type, vInit).elaborate(term.body, type)
      C.Term.Let(init.term, body.term) of (type ?: body.type)
    }

    /**
     * ----------------
     * Γ, x : A ⊢ x ⇒ A
     */
    term is S.Term.Var && synth(type)                 -> {
      val entry = entries.lastOrNull { it.name == term.name } ?: error("var not found: ${term.name}")
      val term = next().quote(entry.term)
      val type = entry.type
      term of type
    }

    /**
     * Γ ⊢ a ⇐ A
     * -------------
     * Γ ⊢ a : A ⇒ A
     */
    term is S.Term.Anno && synth(type)                -> {
      val type = elaborate(term.type, V.Term.Type)
      val vType = env.eval(type.term)
      elaborate(term.target, vType)
    }

    /**
     * Γ ⊢ t ⇒ A
     * A ≡ B
     * ---------
     * Γ ⊢ t ⇐ B
     */
    check<V.Term>(type)                               -> {
      val actual = elaborate(term, null)
      if (next().conv(actual.type, type)) {
        actual
      } else {
        error("expected: $type, actual: ${actual.type}")
      }
    }

    else                                              -> {
      error("unreachable")
    }
  }
}

private fun Ctx.extend(
  pattern: S.Pattern,
  type: V.Term,
  value: Lazy<V.Term>,
): Ctx {
  val entries = mutableListOf<Ctx.Entry>()

  fun bind(
    pattern: S.Pattern,
    type: V.Term,
    value: V.Term,
  ) {
    when {
      pattern is S.Pattern.PairOf && check<V.Term.Pair>(type) -> {
        val first = V.Term.First(value, type.first)
        bind(pattern.first, type.first.value, first)
        val secondType = type.second(lazyOf(first))
        bind(pattern.second, secondType, V.Term.Second(value, lazyOf(secondType)))
      }

      pattern is S.Pattern.Var && check<V.Term>(type)         -> {
        entries += Ctx.Entry(pattern.name, type, value)
      }

      pattern is S.Pattern.Drop -> {
      }

      else                      -> {
        TODO()
      }
    }
  }
  bind(pattern, type, V.Term.Var(next(), lazyOf(type)))

  return Ctx(this.entries + entries, env + value)
}
