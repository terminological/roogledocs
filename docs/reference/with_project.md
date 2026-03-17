# Set the main project name for a session.

A project consists of a main document and optional supplementary files
with possible presentation versions. The associated google docs and
slides will be named with a versioned title based on the name parameter,
e.g. \`"\<name\> \<version\>"\`, \`"\<name\> supplementary 1
\<version\>"\`, \`"\<name\> slides \<version\>"\`...

## Usage

``` r
with_project(name, version = "v0.01")
```

## Arguments

- name:

  The main project name. Forms part of the googledocs or slides
  filename.

- version:

  A version identifier. defaults to \`"v0.01"\`

## Value

Nothing. Called for side effects. Does not check that document exists.
If it does not exist it will be created on first use.

## Examples

``` r
if (FALSE) with_project("Test project")
```
