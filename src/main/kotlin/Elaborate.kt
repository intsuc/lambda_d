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
  /**
   * An entry for pattern desugaring.
   */
  data class Entry(
    val name: String,
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
fun Ctx.next(): Lvl {
  return Lvl(env.size)
}

/**
 * Returns the next variable of [type] under [this] [Ctx] context.
 */
fun Ctx.nextVar(
  type: Lazy<V.Term>,
): Lazy<V.Term> {
  return next().nextVar(type)
}

/**
 * An elaboration result.
 */
data class Result<E>(
  val element: E,
  val type: V.Term,
)

infix fun <E> E.of(type: V.Term): Result<E> {
  return Result(this, type)
}

inline fun <E> Ctx.resultOf(type: V.Term, build: (C.Term) -> E): Result<E> {
  return build(next().quoteTerm(type)) of type
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
fun Ctx.elaborateTerm(
  term: S.Term,
  type: V.Term?,
): Result<C.Term> {
  return when {
    /**
     * ───────────────
     * Γ ⊢ Type ⇒ Type
     */
    term is S.Term.Type && synth(type)                -> {
      C.Term.Type of V.Term.Type
    }

    /**
     * Γ ⊢ A ⇐ Type
     * Γ ⊢ P ⇐ A ⊣ Γ'
     * Γ' ⊢ B ⇐ Type
     * ──────────────────────
     * Γ ⊢ Π(P : A). B ⇒ Type
     */
    term is S.Term.Func && synth(type)                -> {
      val param = elaborateTerm(term.param, V.Term.Type)
      val vParam = env.evalTerm(param.element)
      val (ctx, _) = extend(term.binder, vParam, nextVar(lazyOf(vParam)))
      val result = ctx.elaborateTerm(term.result, V.Term.Type)
      C.Term.Func(param.element, result.element) of V.Term.Type
    }

    term is S.Term.FuncOf && synth(type)              -> {
      error("failed to synthesize: $term")
    }

    /**
     * Γ ⊢ A ⇐ Type
     * Γ ⊢ p ⇐ A ⊣ Γ'
     * Γ' ⊢ b ⇐ B(p)
     * ───────────────────────
     * Γ ⊢ λp. b ⇐ Π(P : A). B
     */
    term is S.Term.FuncOf && check<V.Term.Func>(type) -> {
      val param = type.param.value
      val next = nextVar(lazyOf(param))
      val (ctx, binder) = extend(term.binder, param, next)
      val body = ctx.elaborateTerm(term.result, type.result(next))
      resultOf(type) { C.Term.FuncOf(binder, body.element, it) }
    }

    term is S.Term.FuncOf && check<V.Term>(type)      -> {
      error("expected: func, actual: $type")
    }

    /**
     * Γ ⊢ f ⇒ Π(P : A). B
     * Γ ⊢ a ⇐ A
     * ───────────────────
     * Γ ⊢ f a ⇒ B(a)
     */
    term is S.Term.Apply && synth(type)               -> {
      val func = elaborateTerm(term.func, null)
      when (val funcType = func.type) {
        is V.Term.Func -> {
          val arg = elaborateTerm(term.arg, funcType.param.value)
          val vArg = lazy { env.evalTerm(arg.element) }
          val type = funcType.result(vArg)
          resultOf(type) { C.Term.Apply(func.element, arg.element, it) }
        }
        else           -> {
          error("expected: func, actual: $funcType")
        }
      }
    }

    /**
     * ───────────────
     * Γ ⊢ Unit ⇒ Type
     */
    term is S.Term.Unit && synth(type)                -> {
      C.Term.Unit of V.Term.Type
    }

    /**
     * ─────────────
     * Γ ⊢ () ⇒ Unit
     */
    term is S.Term.UnitOf && synth(type)              -> {
      C.Term.UnitOf of V.Term.Unit
    }

    /**
     * Γ ⊢ A ⇐ Type
     * Γ ⊢ P ⇐ A ⊣ Γ'
     * Γ' ⊢ B ⇐ Type
     * ──────────────────────
     * Γ ⊢ Σ(P : A). B ⇒ Type
     */
    term is S.Term.Pair && synth(type)                -> {
      val first = elaborateTerm(term.first, V.Term.Type)
      val vFirst = env.evalTerm(first.element)
      val (ctx, _) = extend(term.binder, vFirst, nextVar(lazyOf(vFirst)))
      val second = ctx.elaborateTerm(term.second, V.Term.Type)
      C.Term.Pair(first.element, second.element) of V.Term.Type
    }

    /**
     * Γ ⊢ a ⇔ A
     * Γ ⊢ b ⇔ B(a)
     * ───────────────────────────
     * Γ ⊢ (a, b) ⇔ Σ(P : A). B(a)
     */
    term is S.Term.PairOf && match<V.Term.Pair>(type) -> {
      val first = elaborateTerm(term.first, type?.first?.value)
      val vFirst = lazy { env.evalTerm(first.element) }
      val second = elaborateTerm(term.second, type?.second(vFirst))

      // We need to recreate the type here to store and return the pair type whose second component is applied to the first component of the pair.
      val type = V.Term.Pair(lazyOf(first.type), Closure(env, next().quoteTerm(second.type)))

      resultOf(type) { C.Term.PairOf(first.element, second.element, it) }
    }

    /**
     * Γ ⊢ t ⇒ Σ(P : A). B
     * ───────────────────
     * Γ ⊢ t.1 ⇒ A
     */
    term is S.Term.First && synth(type)               -> {
      val pair = elaborateTerm(term.pair, null)
      when (val pairType = pair.type) {
        is V.Term.Pair -> {
          val type = pairType.first.value
          resultOf(type) { C.Term.First(pair.element, it) }
        }
        else           -> {
          error("expected: pair, actual: $pairType")
        }
      }
    }

    /**
     * Γ ⊢ t ⇒ Σ(P : A). B
     * ───────────────────
     * Γ ⊢ t.2 ⇒ B(t.1)
     */
    term is S.Term.Second && synth(type)              -> {
      val pair = elaborateTerm(term.pair, null)
      when (val pairType = pair.type) {
        is V.Term.Pair -> {
          // We inject the first projection into the pair to represent the first component of the pair, whatever form the pair is in.
          val vFirst = lazy { env.evalTerm(C.Term.First(pair.element, next().quoteTerm(pairType.first.value))) }

          val type = pairType.second(vFirst)
          resultOf(type) { C.Term.Second(pair.element, it) }
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
     * ────────────────────
     * Γ ⊢ let p = a; b ⇔ B
     */
    term is S.Term.Let && match<V.Term>(type)         -> {
      val init = elaborateTerm(term.init, null)
      val vInit = lazy { env.evalTerm(init.element) }
      val (ctx, binder) = extend(term.binder, init.type, vInit)
      val body = ctx.elaborateTerm(term.body, type)
      C.Term.Let(binder, init.element, body.element) of (type ?: body.type)
    }

    /**
     * ────────────────
     * Γ, x : A ⊢ x ⇒ A
     */
    term is S.Term.Var && synth(type)                 -> {
      val entry = entries.lastOrNull { it.name == term.name } ?: error("var not found: ${term.name}")
      val term = next().quoteTerm(entry.term)
      val type = entry.term.type.value
      term of type
    }

    /**
     * Γ ⊢ a ⇐ A
     * ─────────────
     * Γ ⊢ a : A ⇒ A
     */
    term is S.Term.Anno && synth(type)                -> {
      val type = elaborateTerm(term.type, V.Term.Type)
      val vType = env.evalTerm(type.element)
      elaborateTerm(term.target, vType)
    }

    /**
     * Γ ⊢ t ⇒ A
     * A ≡ B
     * ─────────
     * Γ ⊢ t ⇐ B
     */
    check<V.Term>(type)                               -> {
      val actual = elaborateTerm(term, null)
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

/**
 * Extends [this] [Ctx] context with [value] of [type], while desugaring [pattern] to the nested projections.
 */
@Suppress("NAME_SHADOWING")
private fun Ctx.extend(
  pattern: S.Pattern,
  type: V.Term,
  value: Lazy<V.Term>,
): Pair<Ctx, C.Pattern> {
  val entries = mutableListOf<Ctx.Entry>()

  fun bind(
    pattern: S.Pattern,
    value: V.Term,
  ): C.Pattern {
    @Suppress("NAME_SHADOWING")
    val type = value.type.value
    return when {
      pattern is S.Pattern.UnitOf && check<V.Term.Unit>(type) -> {
        C.Pattern.UnitOf
      }

      pattern is S.Pattern.PairOf && check<V.Term.Pair>(type) -> {
        val proj = V.Term.First(value, type.first)
        val first = bind(pattern.first, proj)
        val second = bind(pattern.second, V.Term.Second(value, lazy { type.second(lazyOf(proj)) }))
        C.Pattern.PairOf(first, second)
      }

      pattern is S.Pattern.Var                                -> {
        entries += Ctx.Entry(pattern.name, value)
        C.Pattern.Var(pattern.name)
      }

      pattern is S.Pattern.Drop                               -> {
        C.Pattern.Drop
      }

      else                                                    -> {
        TODO()
      }
    }
  }

  val pattern = bind(pattern, V.Term.Var(next(), lazyOf(type)))

  return Ctx(this.entries + entries, env + value) to pattern
}
