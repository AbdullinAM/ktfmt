@Foo(a = [1, 2])
fun doIt(o: Object) {
  //
}

@Anno(
    array =
        [
            someItem,
            andAnother,
            noTrailingComma])
class Host

@Anno(
    array =
        [
            someItem,
            andAnother,
            withTrailingComma,
        ])
class Host

@Anno(
    array =
        [
            // Comment
            someItem,
            // Comment
            andAnother,
            // Comment
            withTrailingComment
            // Comment
            // Comment
            ])
class Host
