# Executes a maven goal

Maven goals are defined either as life-cycle goals (e.g. "clean",
"compile") or as plugin goals (e.g. "help:system"). Some Maven goals may
be executed without a \`pom.xml\` file, others require one. Some maven
goals (e.g. compilation) require the use of a \`JDK\`.

## Usage

``` r
.execute_maven(
  goal,
  opts = c(),
  pom_path = NULL,
  quiet = .quietly(verbose),
  debug = .debug(verbose),
  verbose = c("normal", "debug", "quiet"),
  require_jdk = FALSE,
  settings = .settings_path(),
  ...
)
```

## Arguments

- goal:

  the goal of the \`mvn\` command ( can be multiple ) e.g.
  \`c("clean","compile")\`

- opts:

  provided options in the form
  \`c("-Doption1=value2","-Doption2=value2")\`

- pom_path:

  optional. the path to a \`pom.xml\` file for goals that need one.

- quiet:

  should output from maven be suppressed? (\`-q\` flag)

- debug:

  should output from maven be verbose? (\`-X\` flag)

- verbose:

  how much output from maven, one of "normal", "quiet", "debug"

- require_jdk:

  does the goal you are executing require a \`JDK\` (e.g. compilation
  does, fetching artifacts and calculating class path does not)

- settings:

  the path to a \`settings.xml\` file controlling Maven. The default is
  a configuration with a local repository in the \`rmaven\` cache
  directory (and not the Java maven repository).

- ...:

  non-empty named parameters are passed to maven as options in the form
  \`-Dname=value\`

## Value

nothing, invisibly
