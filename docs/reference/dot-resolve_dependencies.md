# Resolve dependencies and calculate the \`classpath\` for an artifact.

This function makes sure the transitive dependencies for a maven
artifact are available locally in the \`.m2\` maven cache and calculates
a local classpath which can be provided to \`rJava\`. The artifact may
be specified either as a set of maven coordinates (in which case the
artifact itself is also downloaded, and included in the \`classpath\`)
or as a path to a jar file containing a pom.xml (e.g. a compiled jar
file, a compiled \`...-jar-with-dependencies\`, or a assembled
\`...-src.jar\`).

## Usage

``` r
.resolve_dependencies(
  groupId = NULL,
  artifactId = NULL,
  version = NULL,
  ...,
  coordinates = NULL,
  artifact = NULL,
  path = NULL,
  include_self = NULL,
  nocache = FALSE,
  verbose = c("normal", "quiet", "debug")
)
```

## Arguments

- groupId:

  the maven \`groupId\`, optional

- artifactId:

  the maven \`artifactId\`, optional

- version:

  the maven version, optional

- ...:

  passed on to as.coordinates()

- coordinates:

  the maven coordinates, optional (either \`groupId\`,\`artifactId\` and
  'version' must be specified, or 'coordinates', or 'artifact')

- artifact:

  optional, coordinates as an artifact string
  \`groupId:artifactId:version\[:packaging\[:classifier\]\]\` string

- path:

  the path to the source directory, pom file or jar file. if not given
  \`rmaven\` will get the artifact from the maven central repositories

- include_self:

  do you want include this path in the \`classpath\`. optional, if
  missing the path will be included if it is a regular jar, or a fat
  jar, otherwise not.

- nocache:

  do not used cached version, by default we use a cached version of the
  \`classpath\` unless the \`pom.xml\` is newer that the cached
  \`classpath\`.

- verbose:

  how much output from maven, one of "normal", "quiet", "debug"

## Value

a character vector of the \`classpath\` jar files (including the current
one if appropriate)
