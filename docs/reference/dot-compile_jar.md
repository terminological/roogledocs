# Compile and package Java code

Compilation will package the Java source code in to a Jar file for
further use. It will resolve dependencies and optionally package them
into a single \`uber jar\` (using maven assembly).

## Usage

``` r
.compile_jar(
  path,
  nocache = FALSE,
  verbose = c("normal", "quiet", "debug"),
  with_dependencies = FALSE,
  ...
)
```

## Arguments

- path:

  the path to - either a java source code directory containing a
  \`pom.xml\` file, the \`pom.xml\` file itself, or a \`...-src.jar\`
  assembled by the maven assembly plugin,

- nocache:

  normally compilation is only performed if the input has changed.
  \`nocache\` forces recompilation

- verbose:

  how much output from maven, one of "normal", "quiet", "debug"

- with_dependencies:

  compile the Java code to a '...-jar-with-dependencies.jar' including
  transitive dependencies which may be easier to embed into R code as
  does not need a class path (however may be large if there are a lot of
  dependencies)

- ...:

  passed to \`execute_maven(...)\`, e.g. could include \`settings\`
  parameter

## Value

the path to the compiled 'jar' file. If this is a fat jar this can be
passed straight to \`rJava\`, otherwise an additional
\`resolve_dependencies(...)\` call is required
