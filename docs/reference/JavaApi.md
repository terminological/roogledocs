# R Wrapper For Googledocs Java Library

Programmatically substitute images, data and tables into a google doc or
presentation. R library to perform limited interactions with google docs
and slides in R via the Java API library. The purpose being to support
google docs as a platform for interactive development and documentation
of data analysis in R for scientific publication, although it is not
limited to this purpose. The workflow supported is a parallel
documentation and analysis where a team of people are working
collaboratively on documentation, whilst at the same time analysis is
being performed and results updated repeatedly as a result of new data.
In this environment updating numeric results, tabular data and figures
in word documents manually becomes annoying. With roogledocs you can
automate this a bit like a RMarkdown document, but with the added
benefit that the content can be updated independently of the analysis,
by the wider team.

Version: 0.6.0

Generated: 2026-03-17T09:08:33.723432981

## Usage

     J = roogledocs::JavaApi$get(logLevel)
       

## Arguments

- logLevel:

  optional - the slf4j log level as a string - one of OFF (most
  specific, no logging), FATAL (most specific, little data), ERROR,
  WARN, INFO, DEBUG, TRACE (least specific, a lot of data), ALL (least
  specific, all data)

## Author

<rob.challen@bristol.ac.uk>

## Package initialisation and control

- [`JavaApi$installDependencies()`](#method-api-installDependencies)

- [`JavaApi$rebuildDependencies()`](#method-api-rebuildDependencies)

- [`JavaApi$versionInformation()`](#method-api-versionInformation)

- [`J = JavaApi$get(logLevel)`](#method-api-get)

- [`J$changeLogLevel(logLevel)`](#method-api-changeLogLevel)

- [`J$reconfigureLog(log4jproperties)`](#method-api-reconfigureLog)

- [`J$printMessages()`](#method-api-printMessages)

## Package classes and static methods

------------------------------------------------------------------------

- [`J$RoogleSlides$new(tokenDirectory, disabled)`](#method-RoogleSlides-new)

- [`J$RoogleSlides$slidesById(shareUrlOrDocId, tokenDirectory, disabled)`](#method-RoogleSlides-slidesById)

- [`J$RoogleSlides$slidesByName(title, tokenDirectory, disabled)`](#method-RoogleSlides-slidesByName)

- [`J$RoogleSlides$slidesFromTemplate(title, templateUri, tokenDirectory, disabled)`](#method-RoogleSlides-slidesFromTemplate)

- [`J$RoogleSlides$searchForSlides(titleMatch, tokenDirectory)`](#method-RoogleSlides-searchForSlides)

- [`J$RoogleSlides$deleteSlides(docName, areYouSure, tokenDirectory, disabled)`](#method-RoogleSlides-deleteSlides)

------------------------------------------------------------------------

- [`J$RoogleDocs$new(tokenDirectory, disabled)`](#method-RoogleDocs-new)

- [`J$RoogleDocs$reauth(tokenDirectory)`](#method-RoogleDocs-reauth)

- [`J$RoogleDocs$docById(shareUrlOrDocId, tokenDirectory, disabled)`](#method-RoogleDocs-docById)

- [`J$RoogleDocs$docByName(title, tokenDirectory, disabled)`](#method-RoogleDocs-docByName)

- [`J$RoogleDocs$docFromTemplate(title, templateUri, tokenDirectory, disabled)`](#method-RoogleDocs-docFromTemplate)

- [`J$RoogleDocs$searchForDocuments(titleMatch, tokenDirectory)`](#method-RoogleDocs-searchForDocuments)

- [`J$RoogleDocs$deleteDocument(docName, areYouSure, tokenDirectory, disabled)`](#method-RoogleDocs-deleteDocument)

- [`J$RoogleDocs$citationStyles()`](#method-RoogleDocs-citationStyles)

## Package initialisation and control

### Package method `JavaApi$installDependencies()`

This package level method checks for, and installs any dependencies
needed for the running of the package. It is called automatically on
first package load and so in general does not need to be used directly.

#### Usage

    roogledocs::JavaApi$installDependencies()
          

#### Arguments

- none

#### Returns

nothing. called for side effects.

------------------------------------------------------------------------

### Package method `JavaApi$rebuildDependencies()`

This package level method removes existing dependencies and re-installs
dependencies needed for the running of the package. It is called
automatically on first package load and so in general does not need to
be called.

#### Usage

    roogledocs::JavaApi$rebuildDependencies()
          

#### Arguments

- none

#### Returns

nothing. called for side effects.

------------------------------------------------------------------------

### Package method `JavaApi$versionInformation()`

This package level method returns debugging version information for the
package

#### Usage

    roogledocs::JavaApi$versionInformation()
          

#### Arguments

- none

#### Returns

A list containing a set of versioning information about this package.

------------------------------------------------------------------------

### Package method `JavaApi$get()`

This is the main entry point for the package and the root of the Java
API in this package. All classes defined in the package are made
available as items under this root. The JavaApi object manages the
communication between R and Java.

#### Usage

    J = roogledocs::JavaApi$get()
    # package classes and functions are nested under the `J` api object.
          

#### Arguments

- logLevel:

  The desired verbosity of the package. One of "OFF", "FATAL", "ERROR",
  "WARN", "INFO", "DEBUG", "TRACE", "ALL".

#### Returns

A R6 roogledocs::JavaApi object containing the access point to the
objects and functions defined in this package

------------------------------------------------------------------------

### Api method `J$changeLogLevel(logLevel)`

Once the package is initialised the log level can be changed to increase
the level of messages from the api.

#### Usage

    J = roogledocs::JavaApi$get()
    J$changeLogLevel("DEBUG")
          

#### Arguments

- logLevel:

  The desired verbosity of the package. One of "OFF", "FATAL", "ERROR",
  "WARN", "INFO", "DEBUG", "TRACE", "ALL".

#### Returns

nothing. used for side effects.

------------------------------------------------------------------------

### Api method `J$reconfigureLog(log4jproperties)`

Experimental / Advanced use: Once the package is initialised the log
configureation can be changed to log to an external file for example.

#### Usage

    J = roogledocs::JavaApi$get()
    prp = fs::path(getwd(),"log4j.properties")
    if (fs::file_exists(prp)) {
      J$changeLogLevel(prp)
    }
          

#### Arguments

- log4jproperties:

  a full path to a log4jproperies file

#### Returns

nothing. used for side effects.

------------------------------------------------------------------------

### Api method `J$printMessages()`

Experimental / Internal use: Messages from Java to R are queued and
printed after each function call. It is unlikely that any will be not
printed so in normal circumstances this function should do nothing.

#### Usage

    J = roogledocs::JavaApi$get()
    J$printMessages()
          

#### Arguments

- none

#### Returns

nothing. used for side effects.

## Static methods and constructors

------------------------------------------------------------------------

### Method `RoogleSlides$new()`

Create a RoogleSlides object for managing the interaction.

#### Usage

    J = roogledocs::JavaApi$get()
    J$RoogleSlides$new(tokenDirectory, disabled)
            

#### Arguments

- tokenDirectory the place to store authentication tokens. This should
  not be checked into version control. - (defaulting to
  \`.tokenDirectory()\`):

  \- (java expects a RCharacter)

- disabled a flag to switch roogledocs off (on a document by document
  basis, for testing or development. This can be set globally with
  \`options('roogledocs.disabled'=TRUE)\` - (defaulting to
  \`getOption('roogledocs.disabled',FALSE)\`):

  \- (java expects a RLogical)

#### Returns

R6 RoogleSlides object:

------------------------------------------------------------------------

### Method `RoogleSlides$slidesById()`

Get a document by id or sharing link.

#### Usage

    J = roogledocs::JavaApi$get()
    J$RoogleSlides$slidesById(shareUrlOrDocId, tokenDirectory, disabled)
    # this method is also exposed as a package function:
    roogledocs::slides_by_id(shareUrlOrDocId, tokenDirectory, disabled)
            

#### Arguments

- shareUrlOrDocId the url from clicking a share button in google slides
  or an id from searchForDocuments() method:

  \- (java expects a RCharacter)

- tokenDirectory the place to store authentication tokens. This should
  not be checked into version control. - (defaulting to
  \`.tokenDirectory()\`):

  \- (java expects a RCharacter)

- disabled a flag to switch roogledocs off (on a document by document
  basis, for testing or development. This can be set globally with
  \`options('roogledocs.disabled'=TRUE)\` - (defaulting to
  \`getOption('roogledocs.disabled',FALSE)\`):

  \- (java expects a RLogical)

#### Returns

R6 RoogleSlides object: itself - a fluent method

------------------------------------------------------------------------

### Method `RoogleSlides$slidesByName()`

Get a document by name or create a blank document if missing.

#### Usage

    J = roogledocs::JavaApi$get()
    J$RoogleSlides$slidesByName(title, tokenDirectory, disabled)
    # this method is also exposed as a package function:
    roogledocs::slides_by_name(title, tokenDirectory, disabled)
            

#### Arguments

- title a document title. If there is an exact match in google drive
  then that document will be used:

  \- (java expects a RCharacter)

- tokenDirectory the place to store authentication tokens. This should
  not be checked into version control. - (defaulting to
  \`.tokenDirectory()\`):

  \- (java expects a RCharacter)

- disabled a flag to switch roogledocs off (on a document by document
  basis, for testing or development. This can be set globally with
  \`options('roogledocs.disabled'=TRUE)\` - (defaulting to
  \`getOption('roogledocs.disabled',FALSE)\`):

  \- (java expects a RLogical)

#### Returns

R6 RoogleSlides object: itself - a fluent method

------------------------------------------------------------------------

### Method `RoogleSlides$slidesFromTemplate()`

Get a document by name or create one from a template if missing.

#### Usage

    J = roogledocs::JavaApi$get()
    J$RoogleSlides$slidesFromTemplate(title, templateUri, tokenDirectory, disabled)
    # this method is also exposed as a package function:
    roogledocs::slides_from_template(title, templateUri, tokenDirectory, disabled)
            

#### Arguments

- title a document title. If there is an exact match in google drive
  then that document will be used otherwise a new one will be created.:

  \- (java expects a RCharacter)

- templateUri the share link (or document id) of a template google
  document:

  \- (java expects a RCharacter)

- tokenDirectory the place to store authentication tokens. This should
  not be checked into version control. - (defaulting to
  \`.tokenDirectory()\`):

  \- (java expects a RCharacter)

- disabled a flag to switch roogledocs off (on a document by document
  basis, for testing or development. This can be set globally with
  \`options('roogledocs.disabled'=TRUE)\` - (defaulting to
  \`getOption('roogledocs.disabled',FALSE)\`):

  \- (java expects a RLogical)

#### Returns

R6 RoogleSlides object: itself - a fluent method

------------------------------------------------------------------------

### Method `RoogleSlides$searchForSlides()`

Search for documents with the given title

#### Usage

    J = roogledocs::JavaApi$get()
    J$RoogleSlides$searchForSlides(titleMatch, tokenDirectory)
    # this method is also exposed as a package function:
    roogledocs::search_for_slides(titleMatch, tokenDirectory)
            

#### Arguments

- titleMatch a string to be searched for as an approximate match. All
  results will be retrieved with document ids.:

  \- (java expects a RCharacter)

- tokenDirectory the place to store authentication tokens. This should
  not be checked into version control. - (defaulting to
  \`.tokenDirectory()\`):

  \- (java expects a RCharacter)

#### Returns

RDataframe: a dataframe containing id and name columns

------------------------------------------------------------------------

### Method `RoogleSlides$deleteSlides()`

Deletes a google slides by name.

#### Usage

    J = roogledocs::JavaApi$get()
    J$RoogleSlides$deleteSlides(docName, areYouSure, tokenDirectory, disabled)
    # this method is also exposed as a package function:
    roogledocs::delete_slides(docName, areYouSure, tokenDirectory, disabled)
            

#### Arguments

- docName - the name of a document to delete. must be an exact and
  unique match.:

  \- (java expects a RCharacter)

- areYouSure - a boolean check. - (defaulting to
  \`utils::askYesNo(paste0('Are you sure ...\`):

  \- (java expects a RLogical)

- tokenDirectory - (defaulting to \`.tokenDirectory()\`):

  \- (java expects a RCharacter)

- disabled - (defaulting to \`getOption('roogledocs.disabled',FALSE)\`):

  \- (java expects a RLogical)

#### Returns

void: nothing, called for side efffects

------------------------------------------------------------------------

### Method `RoogleDocs$new()`

Create a Roogledocs object for managing the interaction.

#### Usage

    J = roogledocs::JavaApi$get()
    J$RoogleDocs$new(tokenDirectory, disabled)
            

#### Arguments

- tokenDirectory the place to store authentication tokens. This should
  not be checked into version control. - (defaulting to
  \`.tokenDirectory()\`):

  \- (java expects a RCharacter)

- disabled a flag to switch roogledocs off (on a document by document
  basis, for testing or development. This can be set globally with
  \`options('roogledocs.disabled'=TRUE)\` - (defaulting to
  \`getOption('roogledocs.disabled',FALSE)\`):

  \- (java expects a RLogical)

#### Returns

R6 RoogleDocs object:

------------------------------------------------------------------------

### Method `RoogleDocs$reauth()`

Re-authenticate roogledocs library

Re-authenticate the service deleting the existing OAuth tokens may be
helpful if there is some problem.

Generally this is only be needed if application permission updates are
needed in which case the directory can be manually deleted anyway, or if
you want to switch google user without using a different tokenDirectory.

#### Usage

    J = roogledocs::JavaApi$get()
    J$RoogleDocs$reauth(tokenDirectory)
    # this method is also exposed as a package function:
    roogledocs::reauth(tokenDirectory)
            

#### Arguments

- tokenDirectory the place to store authentication tokens. This should
  not be checked into version control. - (defaulting to
  \`.tokenDirectory()\`):

  \- (java expects a RCharacter)

#### Returns

R6 RoogleDocs object: a new RoogleDocs instance without an active
document

------------------------------------------------------------------------

### Method `RoogleDocs$docById()`

Get a document by id or sharing link.

#### Usage

    J = roogledocs::JavaApi$get()
    J$RoogleDocs$docById(shareUrlOrDocId, tokenDirectory, disabled)
    # this method is also exposed as a package function:
    roogledocs::doc_by_id(shareUrlOrDocId, tokenDirectory, disabled)
            

#### Arguments

- shareUrlOrDocId the url from clicking a share button in google docs or
  an id from searchForDocuments() method:

  \- (java expects a RCharacter)

- tokenDirectory the place to store authentication tokens. This should
  not be checked into version control. - (defaulting to
  \`.tokenDirectory()\`):

  \- (java expects a RCharacter)

- disabled a flag to switch roogledocs off (on a document by document
  basis, for testing or development. This can be set globally with
  \`options('roogledocs.disabled'=TRUE)\` - (defaulting to
  \`getOption('roogledocs.disabled',FALSE)\`):

  \- (java expects a RLogical)

#### Returns

R6 RoogleDocs object: itself - a fluent method

------------------------------------------------------------------------

### Method `RoogleDocs$docByName()`

Get a document by name or create a blank document if missing.

#### Usage

    J = roogledocs::JavaApi$get()
    J$RoogleDocs$docByName(title, tokenDirectory, disabled)
    # this method is also exposed as a package function:
    roogledocs::doc_by_name(title, tokenDirectory, disabled)
            

#### Arguments

- title a document title. If there is an exact match in google drive
  then that document will be used:

  \- (java expects a RCharacter)

- tokenDirectory the place to store authentication tokens. This should
  not be checked into version control. - (defaulting to
  \`.tokenDirectory()\`):

  \- (java expects a RCharacter)

- disabled a flag to switch roogledocs off (on a document by document
  basis, for testing or development. This can be set globally with
  \`options('roogledocs.disabled'=TRUE)\` - (defaulting to
  \`getOption('roogledocs.disabled',FALSE)\`):

  \- (java expects a RLogical)

#### Returns

R6 RoogleDocs object: itself - a fluent method

------------------------------------------------------------------------

### Method `RoogleDocs$docFromTemplate()`

Get a document by name or create one from a template if missing.

#### Usage

    J = roogledocs::JavaApi$get()
    J$RoogleDocs$docFromTemplate(title, templateUri, tokenDirectory, disabled)
    # this method is also exposed as a package function:
    roogledocs::doc_from_template(title, templateUri, tokenDirectory, disabled)
            

#### Arguments

- title a document title. If there is an exact match in google drive
  then that document will be used otherwise a new one will be created.:

  \- (java expects a RCharacter)

- templateUri the share link (or document id) of a template google
  document:

  \- (java expects a RCharacter)

- tokenDirectory the place to store authentication tokens. This should
  not be checked into version control. - (defaulting to
  \`.tokenDirectory()\`):

  \- (java expects a RCharacter)

- disabled a flag to switch roogledocs off (on a document by document
  basis, for testing or development. This can be set globally with
  \`options('roogledocs.disabled'=TRUE)\` - (defaulting to
  \`getOption('roogledocs.disabled',FALSE)\`):

  \- (java expects a RLogical)

#### Returns

R6 RoogleDocs object: itself - a fluent method

------------------------------------------------------------------------

### Method `RoogleDocs$searchForDocuments()`

Search for documents with the given title

#### Usage

    J = roogledocs::JavaApi$get()
    J$RoogleDocs$searchForDocuments(titleMatch, tokenDirectory)
    # this method is also exposed as a package function:
    roogledocs::search_for_documents(titleMatch, tokenDirectory)
            

#### Arguments

- titleMatch a string to be searched for as an approximate match. All
  results will be retrieved with document ids.:

  \- (java expects a RCharacter)

- tokenDirectory the place to store authentication tokens. This should
  not be checked into version control. - (defaulting to
  \`.tokenDirectory()\`):

  \- (java expects a RCharacter)

#### Returns

RDataframe: a dataframe containing id and name columns

------------------------------------------------------------------------

### Method `RoogleDocs$deleteDocument()`

Deletes a google document by name.

#### Usage

    J = roogledocs::JavaApi$get()
    J$RoogleDocs$deleteDocument(docName, areYouSure, tokenDirectory, disabled)
    # this method is also exposed as a package function:
    roogledocs::delete_document(docName, areYouSure, tokenDirectory, disabled)
            

#### Arguments

- docName - the name of a document to delete. must be an exact and
  unique match.:

  \- (java expects a RCharacter)

- areYouSure - a boolean check. - (defaulting to
  \`utils::askYesNo(paste0('Are you sure ...\`):

  \- (java expects a RLogical)

- tokenDirectory - (defaulting to \`.tokenDirectory()\`):

  \- (java expects a RCharacter)

- disabled - (defaulting to \`getOption('roogledocs.disabled',FALSE)\`):

  \- (java expects a RLogical)

#### Returns

void: nothing, called for side efffects

------------------------------------------------------------------------

### Method `RoogleDocs$citationStyles()`

List the supported citation styles

#### Usage

    J = roogledocs::JavaApi$get()
    J$RoogleDocs$citationStyles()
    # this method is also exposed as a package function:
    roogledocs::citation_styles()
            

#### Arguments

- none

#### Returns

RCharacterVector: a vector of csl style names

## Examples

``` r
## -----------------------------------
## Check library dependencies for roogledocs
## -----------------------------------
roogledocs::JavaApi$installDependencies()

## -----------------------------------
## Construct a roogledocs Java API instance
## -----------------------------------

J = roogledocs::JavaApi$get()
#> Initialised roogledocs
# or a more verbose configuration
# J = roogledocs::JavaApi$get("DEBUG")


## -----------------------------------
## Method `J$RoogleSlides$new(...)`
## -----------------------------------
if (FALSE) { # \dontrun{
# no example given - appropriate parameter values must be provided:
J$RoogleSlides$new(tokenDirectory, disabled)
# or alternatively:
roogledocs::new(tokenDirectory, disabled)
} # }

## -----------------------------------
## Method `J$RoogleSlides$slidesById(...)`
## Aliased as `roogledocs::slides_by_id(...)`
## -----------------------------------
if (FALSE) { # \dontrun{
# no example given - appropriate parameter values must be provided:
J$RoogleSlides$slidesById(shareUrlOrDocId, tokenDirectory, disabled)
# or alternatively:
roogledocs::slides_by_id(shareUrlOrDocId, tokenDirectory, disabled)
} # }

## -----------------------------------
## Method `J$RoogleSlides$slidesByName(...)`
## Aliased as `roogledocs::slides_by_name(...)`
## -----------------------------------
if (FALSE) { # \dontrun{
# no example given - appropriate parameter values must be provided:
J$RoogleSlides$slidesByName(title, tokenDirectory, disabled)
# or alternatively:
roogledocs::slides_by_name(title, tokenDirectory, disabled)
} # }

## -----------------------------------
## Method `J$RoogleSlides$slidesFromTemplate(...)`
## Aliased as `roogledocs::slides_from_template(...)`
## -----------------------------------
if (FALSE) { # \dontrun{
# no example given - appropriate parameter values must be provided:
J$RoogleSlides$slidesFromTemplate(title, templateUri, tokenDirectory, disabled)
# or alternatively:
roogledocs::slides_from_template(title, templateUri, tokenDirectory, disabled)
} # }

## -----------------------------------
## Method `J$RoogleSlides$searchForSlides(...)`
## Aliased as `roogledocs::search_for_slides(...)`
## -----------------------------------
if (FALSE) { # \dontrun{
# no example given - appropriate parameter values must be provided:
J$RoogleSlides$searchForSlides(titleMatch, tokenDirectory)
# or alternatively:
roogledocs::search_for_slides(titleMatch, tokenDirectory)
} # }

## -----------------------------------
## Method `J$RoogleSlides$deleteSlides(...)`
## Aliased as `roogledocs::delete_slides(...)`
## -----------------------------------
if (FALSE) { # \dontrun{
# no example given - appropriate parameter values must be provided:
J$RoogleSlides$deleteSlides(docName, areYouSure, tokenDirectory, disabled)
# or alternatively:
roogledocs::delete_slides(docName, areYouSure, tokenDirectory, disabled)
} # }

## -----------------------------------
## Method `J$RoogleDocs$new(...)`
## -----------------------------------
if (FALSE) { # \dontrun{
# no example given - appropriate parameter values must be provided:
J$RoogleDocs$new(tokenDirectory, disabled)
# or alternatively:
roogledocs::new(tokenDirectory, disabled)
} # }

## -----------------------------------
## Method `J$RoogleDocs$reauth(...)`
## Aliased as `roogledocs::reauth(...)`
## -----------------------------------
if (FALSE) { # \dontrun{
# no example given - appropriate parameter values must be provided:
J$RoogleDocs$reauth(tokenDirectory)
# or alternatively:
roogledocs::reauth(tokenDirectory)
} # }

## -----------------------------------
## Method `J$RoogleDocs$docById(...)`
## Aliased as `roogledocs::doc_by_id(...)`
## -----------------------------------
if (FALSE) { # \dontrun{
# no example given - appropriate parameter values must be provided:
J$RoogleDocs$docById(shareUrlOrDocId, tokenDirectory, disabled)
# or alternatively:
roogledocs::doc_by_id(shareUrlOrDocId, tokenDirectory, disabled)
} # }

## -----------------------------------
## Method `J$RoogleDocs$docByName(...)`
## Aliased as `roogledocs::doc_by_name(...)`
## -----------------------------------
if (FALSE) { # \dontrun{
# no example given - appropriate parameter values must be provided:
J$RoogleDocs$docByName(title, tokenDirectory, disabled)
# or alternatively:
roogledocs::doc_by_name(title, tokenDirectory, disabled)
} # }

## -----------------------------------
## Method `J$RoogleDocs$docFromTemplate(...)`
## Aliased as `roogledocs::doc_from_template(...)`
## -----------------------------------
if (FALSE) { # \dontrun{
# no example given - appropriate parameter values must be provided:
J$RoogleDocs$docFromTemplate(title, templateUri, tokenDirectory, disabled)
# or alternatively:
roogledocs::doc_from_template(title, templateUri, tokenDirectory, disabled)
} # }

## -----------------------------------
## Method `J$RoogleDocs$searchForDocuments(...)`
## Aliased as `roogledocs::search_for_documents(...)`
## -----------------------------------
if (FALSE) { # \dontrun{
# no example given - appropriate parameter values must be provided:
J$RoogleDocs$searchForDocuments(titleMatch, tokenDirectory)
# or alternatively:
roogledocs::search_for_documents(titleMatch, tokenDirectory)
} # }

## -----------------------------------
## Method `J$RoogleDocs$deleteDocument(...)`
## Aliased as `roogledocs::delete_document(...)`
## -----------------------------------
if (FALSE) { # \dontrun{
# no example given - appropriate parameter values must be provided:
J$RoogleDocs$deleteDocument(docName, areYouSure, tokenDirectory, disabled)
# or alternatively:
roogledocs::delete_document(docName, areYouSure, tokenDirectory, disabled)
} # }

## -----------------------------------
## Method `J$RoogleDocs$citationStyles(...)`
## Aliased as `roogledocs::citation_styles(...)`
## -----------------------------------
if (FALSE) { # \dontrun{
# no example given - appropriate parameter values must be provided:
J$RoogleDocs$citationStyles()
# or alternatively:
roogledocs::citation_styles()
} # }
```
