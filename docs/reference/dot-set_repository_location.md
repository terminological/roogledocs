# Sets the local maven repository location

This writes a maven repository location to a temporary \`settings.xml\`
file which persists only for the R session. The location of the maven
repository is either specified here, or can be defined by the
\`options("rmaven.m2.repository"=...)\` option. If neither of these is
provided, the location will revert to a default location within the
\`rmaven\` cache. (Approved by CRAN for a local cache location) e.g. on
'Linux' this will default to \`~/.cache/rmaven/.m2/repository/\`

## Usage

``` r
.set_repository_location(
  repository_location = getOption("rmaven.m2.repository", default = .working_dir(subpath
    = ".m2/repository/")),
  settings_path = .settings_path()
)
```

## Arguments

- repository_location:

  a file path (which will be expanded to a full path) where the
  repository should be based, e.g. \`~/.m2/repository/\`. Defaults to a
  sub-directory of the \`rmaven\` cache.

- settings_path:

  the file path of the settings.xml to update (generally the supplied
  default is what you want to use)

## Value

the expanded path of the new repository location
