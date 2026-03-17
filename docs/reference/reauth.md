# Re-authenticate roogledocs library

Re-authenticate the service deleting the existing OAuth tokens may be
helpful if there is some problem.

Generally this is only be needed if application permission updates are
needed in which case the directory can be manually deleted anyway, or if
you want to switch google user without using a different tokenDirectory.

## Usage

``` r
reauth(
  tokenDirectory
)
```

## Arguments

- tokenDirectory:

  tokenDirectory the place to store authentication tokens. This should
  not be checked into version control. - (defaulting to
  \`.tokenDirectory()\`) - (java expects a RCharacter)

## Value

R6 RoogleDocs object: a new RoogleDocs instance without an active
document
