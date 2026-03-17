# Search for documents with the given title

no description

## Usage

``` r
search_for_documents(
  titleMatch,
  tokenDirectory
)
```

## Arguments

- titleMatch:

  titleMatch a string to be searched for as an approximate match. All
  results will be retrieved with document ids. - (java expects a
  RCharacter)

- tokenDirectory:

  tokenDirectory the place to store authentication tokens. This should
  not be checked into version control. - (defaulting to
  \`.tokenDirectory()\`) - (java expects a RCharacter)

## Value

RDataframe: a dataframe containing id and name columns
