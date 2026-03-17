# Start an \`rJava\` \`JVM\` with or without debugging options

This does not do anything if the \`JVM\` has already been started.
Otherwise starts the JVM via \`rJava\` with a set of options Additional
JVM options (beyond debugging) can be set with the
\`options("java.parameters"=c("-Xprof","-Xrunhprof"))\`

## Usage

``` r
.start_jvm(
  debug = FALSE,
  quiet = getOption("rmaven.quiet", TRUE),
  max_heap = NULL,
  thread_stack = NULL,
  ...
)
```

## Arguments

- debug:

  turn on debugging

- quiet:

  don't report messages (defaults to \`getOption("rmaven.quiet")\` or
  TRUE)

- max_heap:

  optional. if a string like \`"2048m"\` the \`-Xmx\` option value to
  start the \`JVM\` - if a string like \`"75 if a numeric - number of
  megabytes.

- thread_stack:

  optional. sensible values range from '1m' to '128m' (max is '1g'). Can
  be important with deeply nested structures.

- ...:

  any other named parameters are passed as \`-name=value\`

## Value

nothing - called for side effects
