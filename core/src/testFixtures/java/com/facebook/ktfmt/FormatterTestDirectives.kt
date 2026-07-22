package com.facebook.ktfmt

data class FormatterTestOptions(
    val allowTrailingWhitespace: Boolean,
    val checkIdempotency: Boolean,
)

class FormatterTestDirectives : DirectiveContainer<FormatterTestOptions> {
  override val result
    get() =
        FormatterTestOptions(
            allowTrailingWhitespace = allowTrailingWhitespace,
            checkIdempotency = checkIdempotency,
        )

  var allowTrailingWhitespace: Boolean = false
  var checkIdempotency: Boolean = true

  override val directives =
      listOf(
          FormatterTestOptionsDirective("ALLOW_TRAILING_WHITESPACE") {
            allowTrailingWhitespace = true
          },
          FormatterTestOptionsDirective("CHECK_IDEMPOTENCY") {
              checkIdempotency = it.toBooleanStrict()
          },
      )

  data class FormatterTestOptionsDirective(
      override val name: String,
      override val callback: (String) -> Unit,
  ) : Directive
}
