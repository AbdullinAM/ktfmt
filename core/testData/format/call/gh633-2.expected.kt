fun quux() = listOf(
    "a",
    "b",
    listOf(
        "a",
        "b",
    ),
    listOf(
        "a",
        "b",
    )
        .baz()
        .bar(1, 2)
        .fold { it.boom() }
)
    .baz()

fun quux() {
  listOf(
      "a",
      "b",
  )

  listOf(
      "a",
      "b",
      listOf(
          "a",
          "b",
      ),
      listOf(
          "a",
          "b",
      )
          .baz()
          .bar(1, 2)
          .fold { it.boom() }
  )
      .baz()
}
