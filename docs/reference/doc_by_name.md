# Get a document by name or create a blank document if missing.

no description

## Usage

``` r
doc_by_name(
  title,
  tokenDirectory,
  disabled
)
```

## Arguments

- title:

  title a document title. If there is an exact match in google drive
  then that document will be used - (java expects a RCharacter)

- tokenDirectory:

  tokenDirectory the place to store authentication tokens. This should
  not be checked into version control. - (defaulting to
  \`.tokenDirectory()\`) - (java expects a RCharacter)

- disabled:

  disabled a flag to switch roogledocs off (on a document by document
  basis, for testing or development. This can be set globally with
  \`options('roogledocs.disabled'=TRUE)\` - (defaulting to
  \`getOption('roogledocs.disabled',FALSE)\`) - (java expects a
  RLogical)

## Value

R6 RoogleDocs object: itself - a fluent method
