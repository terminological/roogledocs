# Get a document by name or create one from a template if missing.

no description

## Usage

``` r
slides_from_template(
  title,
  templateUri,
  tokenDirectory,
  disabled
)
```

## Arguments

- title:

  title a document title. If there is an exact match in google drive
  then that document will be used otherwise a new one will be created. -
  (java expects a RCharacter)

- templateUri:

  templateUri the share link (or document id) of a template google
  document - (java expects a RCharacter)

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

R6 RoogleSlides object: itself - a fluent method
