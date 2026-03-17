# Deletes a google document by name.

no description

## Usage

``` r
delete_document(
  docName,
  areYouSure,
  tokenDirectory,
  disabled
)
```

## Arguments

- docName:

  docName - the name of a document to delete. must be an exact and
  unique match. - (java expects a RCharacter)

- areYouSure:

  areYouSure - a boolean check. - (defaulting to
  \`utils::askYesNo(paste0('Are you sure ...\`) - (java expects a
  RLogical)

- tokenDirectory:

  tokenDirectory - (defaulting to \`.tokenDirectory()\`) - (java expects
  a RCharacter)

- disabled:

  disabled - (defaulting to
  \`getOption('roogledocs.disabled',FALSE)\`) - (java expects a
  RLogical)

## Value

void: nothing, called for side efffects
