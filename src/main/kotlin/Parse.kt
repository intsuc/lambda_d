import Surface.Pattern
import Surface.Term

class Parse private constructor(
  private val text: String,
) {
  private var cursor: Int = 0

  private fun parse(): Term {
    val term = parseTerm()
    skipWhitespace()
    check(!canRead())
    return term
  }

  private fun parseTerm(): Term {
    val terms = mutableListOf<Term>()
    skipWhitespace()
    while (canRead() && when (peek()) {
        ')', ';', ':', ',', '.' -> false
        else                    -> true
      }
    ) {
      terms += parseTerm0()
      skipWhitespace()
    }
    return terms.reduce { acc, term ->
      Term.Apply(acc, term)
    }
  }

  private fun parseTerm0(): Term {
    skipWhitespace()
    return when (peek()) {
      'Π'  -> {
        skip()
        when (peek()) {
          '('  -> {
            skip()
            val binder = parsePattern()
            expect(':')
            val param = parseTerm()
            expect(')')
            expect('.')
            val result = parseTerm()
            Term.Func(binder, param, result)
          }
          else -> {
            val param = parseTerm()
            expect('.')
            val result = parseTerm()
            Term.Func(Pattern.Drop, param, result)
          }
        }
      }

      'λ'  -> {
        skip()
        val binder = parsePattern()
        expect('.')
        val body = parseTerm()
        Term.FuncOf(binder, body)
      }

      'Σ'  -> {
        skip()
        when (peek()) {
          '('  -> {
            skip()
            val binder = parsePattern()
            expect(':')
            val param = parseTerm()
            expect(')')
            expect('.')
            val result = parseTerm()
            Term.Pair(binder, param, result)
          }
          else -> {
            val param = parseTerm()
            expect('.')
            val result = parseTerm()
            Term.Pair(Pattern.Drop, param, result)
          }
        }
      }

      '('  -> {
        skip()
        when (peek()) {
          ')'  -> {
            skip()
            Term.UnitOf
          }

          else -> {
            val term = parseTerm()
            skipWhitespace()
            when (peek()) {
              ')'  -> {
                skip()
                term
              }

              ','  -> {
                skip()
                val second = parseTerm()
                expect(')')
                Term.PairOf(term, second)
              }

              '.'  -> {
                skip()
                skipWhitespace()
                when (peek()) {
                  '1'  -> {
                    skip()
                    expect(')')
                    Term.First(term)
                  }

                  '2'  -> {
                    skip()
                    expect(')')
                    Term.Second(term)
                  }

                  else -> {
                    error("unexpected '${peek()}'")
                  }
                }
              }

              ':'  -> {
                skip()
                val type = parseTerm()
                expect(')')
                Term.Anno(term, type)
              }

              else -> {
                error("unexpected '${peek()}'")
              }
            }
          }
        }
      }

      else -> {
        when (val word = parseWord()) {
          "Type" -> {
            Term.Type
          }

          "Unit" -> {
            Term.Unit
          }

          "let"  -> {
            val binder = parsePattern()
            expect('=')
            val init = parseTerm()
            expect(';')
            val body = parseTerm()
            Term.Let(binder, init, body)
          }

          else   -> {
            Term.Var(word)
          }
        }
      }
    }
  }

  private fun parsePattern(): Pattern {
    skipWhitespace()
    return when (peek()) {
      '_'  -> {
        skip()
        Pattern.Drop
      }

      else -> {
        val name = parseWord()
        Pattern.Var(name)
      }
    }
  }

  private fun parseWord(): String {
    skipWhitespace()
    val start = cursor
    while (canRead() && peek().isLetterOrDigit()) {
      skip()
    }
    check(start < cursor) {
      println(text.substring(cursor))
    }
    return text.substring(start, cursor)
  }

  private fun expect(expected: Char) {
    skipWhitespace()
    return if (canRead() && peek() == expected) {
      skip()
    } else {
      error("expected '$expected', got '${peek()}'")
    }
  }

  private fun skipWhitespace() {
    while (canRead() && peek().isWhitespace()) {
      skip()
    }
  }

  private fun skip() {
    ++cursor
  }

  private fun peek(): Char {
    return text[cursor]
  }

  private fun canRead(): Boolean {
    return cursor < text.length
  }

  companion object {
    operator fun invoke(
      text: String,
    ): Term {
      return Parse(text).parse()
    }
  }
}
