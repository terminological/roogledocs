# RoogleSlides

Programmatically substitute images, data and tables into a google
presentation.

Version: 0.6.0

Generated: 2026-03-17T09:08:33.845523818

## Arguments

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

## Details

The purpose being to support google slides as a platform for interactive
development and documentation of data analysis in R. The workflow
supported is a parallel documentation and analysis where a team of
people are working collaboratively on documentation, whilst at the same
time analysis is being performed and results updated repeatedly as a
result of new data. In this environment updating numeric results,
tabular data and figures in word documents manually becomes annoying.
With roogledocs you can automate this a bit like a RMarkdown document,
but with the added benefit that the content can be updated independently
of the analysis, by the wider team.

## Methods

### Constructors

- [`J$RoogleSlides$new(tokenDirectory, disabled)`](#method-new)

### Class methods

- [`instance$enable()`](#method-enable)

- [`instance$disable()`](#method-disable)

- [`instance$getName(suffix)`](#method-getName)

- [`instance$tagsDefined()`](#method-tagsDefined)

- [`instance$updateTaggedText(text, tagName)`](#method-updateTaggedText)

- [`instance$updateTaggedImage(absoluteFilePath, tagName, keepUpload)`](#method-updateTaggedImage)

- [`instance$updateTaggedTable(longFormatTable, tagName, colWidths)`](#method-updateTaggedTable)

- [`instance$removeTags(confirm)`](#method-removeTags)

- [`instance$saveAsPdf(absoluteFilePath, uploadCopy)`](#method-saveAsPdf)

- [`instance$makeCopy(newName)`](#method-makeCopy)

- [`instance$delete(areYouSure)`](#method-delete)

- [`instance$uploadSupplementaryFiles(absoluteFilePath, overwrite, duplicate)`](#method-uploadSupplementaryFiles)

- [`instance$appendFormattedSlide(title, formattedTextDf)`](#method-appendFormattedSlide)

- [`instance$slideDimensions()`](#method-slideDimensions)

- [`instance$slideLayouts()`](#method-slideLayouts)

- [`instance$setDefaultLayout(layout)`](#method-setDefaultLayout)

- [`instance$updateCitations(bibTexPath, citationStyle)`](#method-updateCitations)

- `instance$clone()`

- `instance$print()`

------------------------------------------------------------------------

### Constructor `new()`

Create a RoogleSlides object for managing the interaction.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleSlides$new(tokenDirectory, disabled);
            

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

the new R6 RoogleSlides object

------------------------------------------------------------------------

### Method `enable()`

Enables roogledocs method calls for this document.

It is likely one of \`withDocument()\`, \`findOrCreateDocument()\` or
\`findOrCloneTemplate()\` methods will be needed to specify the
document.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleSlides$new(tokenDirectory, disabled);
    instance$enable()
            

#### Arguments

- none

#### Returns

R6 RoogleSlides object: itself - a fluent method

------------------------------------------------------------------------

### Method `disable()`

Disables roogledocs temporarily for this document.

While disabled all calls to roogledocs will silently abort.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleSlides$new(tokenDirectory, disabled);
    instance$disable()
            

#### Arguments

- none

#### Returns

R6 RoogleSlides object: itself - a fluent method

------------------------------------------------------------------------

### Method `getName()`

Return the name of the presentation

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleSlides$new(tokenDirectory, disabled);
    instance$getName(suffix)
            

#### Arguments

- suffix an additional suffix to add to the name - (defaulting to
  \`”\`):

  \- (java expects a RCharacter)

#### Returns

String:

------------------------------------------------------------------------

### Method `tagsDefined()`

List all tags

Finds tags defined in the current document

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleSlides$new(tokenDirectory, disabled);
    instance$tagsDefined()
            

#### Arguments

- none

#### Returns

RDataframe: a dataframe containing tag and count columns

------------------------------------------------------------------------

### Method `updateTaggedText()`

Replace tags for text

Substitutes all occurrences of {{tag-name}} with the text parameter. If
the tag is not found then a new slide is inserted at the end in a
section titled "Unmatched tags:". From there they can be cut and pasted
into the right place.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleSlides$new(tokenDirectory, disabled);
    instance$updateTaggedText(text, tagName)
            

#### Arguments

- text the value to replace the tag with (e.g. a result from analysis)
  (cannot be empty):

  \- (java expects a RCharacter)

- tagName the tag name - (defaulting to \`deparse(substitute(text))\`):

  \- (java expects a RCharacter)

#### Returns

R6 RoogleSlides object: itself - a fluent method

------------------------------------------------------------------------

### Method `updateTaggedImage()`

Replace a tag with an image.

Substitutes all occurrences of {{tag-name}} with an image from the local
storage.

The image is uploaded to your google drive as a temporary file, and made
publicly readable. From there it is inserted into the google slides, and
once completed the temporary file deleted from your google drive, unless
\`keepUpload\` is true. Insertion is done in the dimensions of the
containing box of the image if it already exists or a default slide body
box if not.

If the tag is not found in the document a new slide will be created at
the end of the presentation with the image and an uninformative title
which can be changed.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleSlides$new(tokenDirectory, disabled);
    instance$updateTaggedImage(absoluteFilePath, tagName, keepUpload)
            

#### Arguments

- absoluteFilePath a file path to an png image file.:

  \- (java expects a RCharacter)

- tagName the tag name - (defaulting to
  \`deparse(substitute(absoluteFilePath))\`):

  \- (java expects a RCharacter)

- keepUpload keep the uploaded image as a supplementary file in the same
  directory as the google doc. N.B. the result will be publicly
  readable. - (defaulting to \`FALSE\`):

  \- (java expects a RLogical)

#### Returns

R6 RoogleSlides object: itself - a fluent method

------------------------------------------------------------------------

### Method `updateTaggedTable()`

Replace a tag with a table.

Substitutes a unique occurrence of {{tag-name}} with a table. The tag
must either be in a text box shape or as the first entry in a table.
Once inserted the table is tagged using a zero width character as the
very first item in the first cell. This will be removed if
\`removeTags()\` is called.

If the tag is not found in the document a new slide will be created at
the end with the table.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleSlides$new(tokenDirectory, disabled);
    instance$updateTaggedTable(longFormatTable, tagName, colWidths)
            

#### Arguments

- longFormatTable A dataframe consisting of the table content and
  formatting indexed by row and column. at a minimum this should have
  columns label,row,col, but may also include
  rowSpan,colSpan,fillColour, leftBorderWeight, rightBorderWeight,
  topBorderWeight, bottomBorderWeight, alignment (START,CENTER,END),
  valignment (TOP,MIDDLE,BOTTOM), fontName, fontFace, fontSize.:

  \- (java expects a RDataframe)

- tagName the tag name - (defaulting to
  \`deparse(substitute(longFormatTable))\`):

  \- (java expects a RCharacter)

- colWidths A vector including the relative length of each column. This
  can be left out if longFormatTable comes from as.long_format_table -
  (defaulting to \`attr(longFormatTable,'colWidths')\`):

  \- (java expects a RNumericVector)

#### Returns

R6 RoogleSlides object: itself - a fluent method

------------------------------------------------------------------------

### Method `removeTags()`

Remove all tags

Finds tags defined in the current document and removes them. This cannot
be undone, except by rolling back to a previous version.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleSlides$new(tokenDirectory, disabled);
    instance$removeTags(confirm)
            

#### Arguments

- confirm - This action must be confirmed by passing \`true\` as cannot
  be undone. - (defaulting to \`(menu(c('Yes','No'), title = 'Are
  you...\`):

  \- (java expects a RLogical)

#### Returns

R6 RoogleSlides object: itself - a fluent method

------------------------------------------------------------------------

### Method `saveAsPdf()`

Save the document as a PDF

Saves a snapshot of the current google slides with \`roogledocs\` links
removed as a pdf to a local drive. This is mainly intended for
snap-shotting the current state of the document. For final export once
all analysis is complete it may be preferable to call
\`doc\$removeTags()\` and manually export the output but after this no
further updating is possible.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleSlides$new(tokenDirectory, disabled);
    instance$saveAsPdf(absoluteFilePath, uploadCopy)
            

#### Arguments

- absoluteFilePath - a file path to save the pdf.:

  \- (java expects a RFile)

- uploadCopy place a copy of the downloaded pdf back onto google drive
  in the same folder as the document for example for keeping submitted
  versions of a updated document. This will overwrite files of the same
  name in the google drive directory. - (defaulting to \`FALSE\`):

  \- (java expects a RLogical)

#### Returns

R6 RoogleSlides object: itself - a fluent method

------------------------------------------------------------------------

### Method `makeCopy()`

Make a copy of the current document

This makes a exact copy of the document under a new name. This name can
already exist as googledocs can have multiple files with the same file
name but this will certainly lead to confusion later. It is up to the
user to create a naming strategy that does not cause issues.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleSlides$new(tokenDirectory, disabled);
    instance$makeCopy(newName)
            

#### Arguments

- newName - The new document name.:

  \- (java expects a RCharacter)

#### Returns

R6 RoogleSlides object: a \`roogledocs\` object pointing to the new
document.

------------------------------------------------------------------------

### Method `delete()`

Delete the current presentation

Deleted presentations can still be retrieved via the Google Drive
website but this is otherwise a final operation. After this any
operations on this presentation will fail with a null pointer exception.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleSlides$new(tokenDirectory, disabled);
    instance$delete(areYouSure)
            

#### Arguments

- areYouSure - confirm the delete - (defaulting to
  \`utils::askYesNo('Are you sure you wan...\`):

  \- (java expects a RLogical)

#### Returns

void:

------------------------------------------------------------------------

### Method `uploadSupplementaryFiles()`

Upload a file into the same directory as the document.

This allow you to load e.g. a supplementary file, or the pdf of an image
file or a docx/html version of a table into google drive into the same
directory as the slides you are editing. This is handy for organising
all the files for a journal submission in one place. Any kind of file
can be loaded, and the mimetype will be detected. Normal Google Drive
rules for uploads will be triggered at this point. As google drive can
have multiple files with the same name the behaviour if the file already
exists is slightly complex, with \`overwrite\` and \`duplicate\`
options.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleSlides$new(tokenDirectory, disabled);
    instance$uploadSupplementaryFiles(absoluteFilePath, overwrite, duplicate)
            

#### Arguments

- absoluteFilePath - a file path to upload.:

  \- (java expects a RFile)

- overwrite - if matching file(s) are found in the target, delete them
  before uploading the new one. - (defaulting to \`FALSE\`):

  \- (java expects a RLogical)

- duplicate - if matching file(s) are found in the target, upload this
  new file anyway, creating duplicate names in the folder. - (defaulting
  to \`FALSE\`):

  \- (java expects a RLogical)

#### Returns

R6 RoogleSlides object: itself - a fluent method

------------------------------------------------------------------------

### Method `appendFormattedSlide()`

Append a new "TITLE_AND_BODY" slide, with formatted text from the
'label' column with optional formating in the other columns.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleSlides$new(tokenDirectory, disabled);
    instance$appendFormattedSlide(title, formattedTextDf)
            

#### Arguments

- title - A plain text title:

  \- (java expects a RCharacter)

- formattedTextDf - a data frame containing the columns label, and
  optionally: link (as a URL), fontName, fontFace, fontSize.:

  \- (java expects a RDataframe)

#### Returns

R6 RoogleSlides object: itself - a fluent method

------------------------------------------------------------------------

### Method `slideDimensions()`

The default dimensions of the body of a new slide.

A new slide will be created using a default layout which is usually the
slide layout with the biggest text box on it. This can be set manually
with the \`setDefaultLayout()\` function.

The body is the largest text box on the slide. This is the place that
images or tables will be placed by default. If you want to fit in with
the theme then images will need to be to be sized to these dimensions to
fill the slide.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleSlides$new(tokenDirectory, disabled);
    instance$slideDimensions()
            

#### Arguments

- none

#### Returns

RNamedList: a named list with width and height in inches, and the name
of the layout being used as the default.

------------------------------------------------------------------------

### Method `slideLayouts()`

The layouts available in the slides templates

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleSlides$new(tokenDirectory, disabled);
    instance$slideLayouts()
            

#### Arguments

- none

#### Returns

RCharacterVector: a list of available slide layouts

------------------------------------------------------------------------

### Method `setDefaultLayout()`

Set the default layout

The default layout for the slide is used when inserting new slides at
the end of the document for images. A default layout will have 2 text
boxes, one for the title and one for the content. The second text box
will be large. The layouts in a presentation can be listed with
\`slideLayouts()\` or seen on the google slides \`Apply Layout...\` menu
option.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleSlides$new(tokenDirectory, disabled);
    instance$setDefaultLayout(layout)
            

#### Arguments

- layout a string representing the layout:

  \- (java expects a RCharacter)

#### Returns

R6 RoogleSlides object: itself - a fluent method

------------------------------------------------------------------------

### Method `updateCitations()`

Update citation tags in the document.

A citation tag is like this \`{{cite:challen2020;danon2021}}\`. The ids
are matched against the provided bibtex, and the tags are replaced with
an appropriate citation string. The bibliography itself is added to a
specific slide for references which can be decided with the
\`{{references}}\` tag.

If references do not already exist and there if no \`{{references}}\`
tag a new slide will be created at the end of the presentation.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleSlides$new(tokenDirectory, disabled);
    instance$updateCitations(bibTexPath, citationStyle)
            

#### Arguments

- bibTexPath - the full file path to the file containing the bibtex:

  \- (java expects a RCharacter)

- citationStyle - the CSL specification - (defaulting to \`'ieee'\`):

  \- (java expects a RCharacter)

#### Returns

R6 RoogleSlides object: itself - a fluent method

## Examples

``` r
## -----------------------------------
## Construct new instance of RoogleSlides
## -----------------------------------
if (FALSE) { # \dontrun{
J = roogledocs::JavaApi$get()
# appropriate parameter values must be provided
instance = J$RoogleSlides$new(tokenDirectory, disabled)
} # }

## -----------------------------------
## Method `RoogleSlides$enable()`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$enable()
} # }

## -----------------------------------
## Method `RoogleSlides$disable()`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$disable()
} # }

## -----------------------------------
## Method `RoogleSlides$getName(suffix)`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$getName(suffix)
} # }

## -----------------------------------
## Method `RoogleSlides$tagsDefined()`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$tagsDefined()
} # }

## -----------------------------------
## Method `RoogleSlides$updateTaggedText(text, tagName)`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$updateTaggedText(text, tagName)
} # }

## -----------------------------------
## Method `RoogleSlides$updateTaggedImage(absoluteFilePath, tagName, keepUpload)`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$updateTaggedImage(absoluteFilePath, tagName, keepUpload)
} # }

## -----------------------------------
## Method `RoogleSlides$updateTaggedTable(longFormatTable, tagName, colWidths)`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$updateTaggedTable(longFormatTable, tagName, colWidths)
} # }

## -----------------------------------
## Method `RoogleSlides$removeTags(confirm)`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$removeTags(confirm)
} # }

## -----------------------------------
## Method `RoogleSlides$saveAsPdf(absoluteFilePath, uploadCopy)`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$saveAsPdf(absoluteFilePath, uploadCopy)
} # }

## -----------------------------------
## Method `RoogleSlides$makeCopy(newName)`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$makeCopy(newName)
} # }

## -----------------------------------
## Method `RoogleSlides$delete(areYouSure)`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$delete(areYouSure)
} # }

## -----------------------------------
## Method `RoogleSlides$uploadSupplementaryFiles(absoluteFilePath, overwrite, duplicate)`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$uploadSupplementaryFiles(absoluteFilePath, overwrite, duplicate)
} # }

## -----------------------------------
## Method `RoogleSlides$appendFormattedSlide(title, formattedTextDf)`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$appendFormattedSlide(title, formattedTextDf)
} # }

## -----------------------------------
## Method `RoogleSlides$slideDimensions()`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$slideDimensions()
} # }

## -----------------------------------
## Method `RoogleSlides$slideLayouts()`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$slideLayouts()
} # }

## -----------------------------------
## Method `RoogleSlides$setDefaultLayout(layout)`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$setDefaultLayout(layout)
} # }

## -----------------------------------
## Method `RoogleSlides$updateCitations(bibTexPath, citationStyle)`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$updateCitations(bibTexPath, citationStyle)
} # }
```
