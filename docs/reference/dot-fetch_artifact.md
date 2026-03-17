# Fetch an artifact, and dependencies, into the local \`.m2\` repository

This can be used to get a JAR file from the maven repositories into a
local \`.m2\` repository. The local path is made available for importing
it into the \`rJava\` classpath for example.

## Usage

``` r
.fetch_artifact(
  groupId = NULL,
  artifactId = NULL,
  version = NULL,
  ...,
  coordinates = NULL,
  artifact = NULL,
  repoUrl = .default_repos(),
  nocache = FALSE,
  verbose = c("normal", "quiet", "debug")
)
```

## Arguments

- groupId:

  optional, the maven \`groupId\`,

- artifactId:

  optional, the maven \`artifactId\`,

- version:

  optional, the maven version,

- ...:

  other maven coordinates such as classifier or packaging

- coordinates:

  optional, but if not supplied \`groupId\` and \`artifactId\` must be,
  coordinates as a coordinates object (see \`as.coordinates()\`)

- artifact:

  optional, coordinates as an artifact string
  \`groupId:artifactId:version\[:packaging\[:classifier\]\]\` string

- repoUrl:

  the URLs of the repositories to check (defaults to Maven central,
  'Sonatype' snapshots and 'jitpack', defined in
  \`options("rmaven.default_repos"))\`

- nocache:

  normally artifacts are only fetched if required, \`nocache\` forces
  fetching

- verbose:

  how much output from maven, one of "normal", "quiet", "debug"

## Value

the path of the artifact within the local maven cache
