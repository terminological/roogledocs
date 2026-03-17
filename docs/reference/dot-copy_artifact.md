# Copy an artifact from a repository to a local directory

This essentially runs a \`maven-dependency-plugin:copy\` goal to copy a
JAR file from (usually) a remote repository to a local directory. The
directory is under the users control but defaults to the \`.m2\`
repository.

## Usage

``` r
.copy_artifact(
  groupId = NULL,
  artifactId = NULL,
  version = NULL,
  ...,
  coordinates = NULL,
  artifact = NULL,
  outputDirectory = .working_dir(artifact),
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

  optional, coordinates as a coordinates object,

- artifact:

  optional, coordinates as an artifact string
  \`groupId:artifactId:version\[:packaging\[:classifier\]\]\` string

- outputDirectory:

  optional path, defaults to the \`rmaven\` cache directory

- repoUrl:

  the URLs of the repositories to check (defaults to maven central,
  \`Sonatype snaphots\` and \`jitpack\`)

- nocache:

  normally artifacts are only fetched if required, \`nocache\` forces
  fetching

- verbose:

  how much output from maven, one of "normal", "quiet", "debug"

## Value

the output of the system2 call. 0 on success.
