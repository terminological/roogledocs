# RoogleDocs

Programmatically substitute images, data and tables into a google doc.

Version: 0.6.0

Generated: 2026-03-17T09:08:33.903260011

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

R library to perform limited interactions with google docs (and maybe
one day slides) in R via the Java API library. The purpose being to
support google docs as a platform for interactive development and
documentation of data analysis in R for scientific publication, although
it is not limited to this purpose. The workflow supported is a parallel
documentation and analysis where a team of people are working
collaboratively on documentation, whilst at the same time analysis is
being performed and results updated repeatedly as a result of new data.
In this environment updating numeric results, tabular data and figures
in word documents manually becomes annoying. With roogledocs you can
automate this a bit like a RMarkdown document, but with the added
benefit that the content can be updated independently of the analysis,
by the wider team.

## Methods

### Constructors

- [`J$RoogleDocs$new(tokenDirectory, disabled)`](#method-new)

### Class methods

- [`instance$enable()`](#method-enable)

- [`instance$disable()`](#method-disable)

- [`instance$getName(suffix)`](#method-getName)

- [`instance$tagsDefined()`](#method-tagsDefined)

- [`instance$updateTaggedText(text, tagName)`](#method-updateTaggedText)

- [`instance$updateTaggedImage(absoluteFilePath, tagName, dpi, keepUpload)`](#method-updateTaggedImage)

- [`instance$updateTaggedTable(longFormatTable, tagName, colWidths, tableWidthInches)`](#method-updateTaggedTable)

- [`instance$revertTags()`](#method-revertTags)

- [`instance$removeTags(confirm)`](#method-removeTags)

- [`instance$updateTable(longFormatTable, tableIndex, colWidths, tableWidthInches)`](#method-updateTable)

- [`instance$updateFigure(absoluteFilePath, figureIndex, dpi, keepUpload)`](#method-updateFigure)

- [`instance$saveAsPdf(absoluteFilePath, uploadCopy)`](#method-saveAsPdf)

- [`instance$makeCopy(newName)`](#method-makeCopy)

- [`instance$delete(areYouSure)`](#method-delete)

- [`instance$uploadSupplementaryFiles(absoluteFilePath, overwrite, duplicate)`](#method-uploadSupplementaryFiles)

- [`instance$appendText(text, style)`](#method-appendText)

- [`instance$appendFormattedParagraph(formattedTextDf)`](#method-appendFormattedParagraph)

- [`instance$updateCitations(bibTexPath, citationStyle)`](#method-updateCitations)

- `instance$clone()`

- `instance$print()`

------------------------------------------------------------------------

### Constructor `new()`

Create a Roogledocs object for managing the interaction.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleDocs$new(tokenDirectory, disabled);
            

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

the new R6 RoogleDocs object

------------------------------------------------------------------------

### Method `enable()`

Enables roogledocs method calls for this document.

It is likely one of \`withDocument()\`, \`findOrCreateDocument()\` or
\`findOrCloneTemplate()\` methods will be needed to specify the
document.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleDocs$new(tokenDirectory, disabled);
    instance$enable()
            

#### Arguments

- none

#### Returns

R6 RoogleDocs object: itself - a fluent method

------------------------------------------------------------------------

### Method `disable()`

Disables roogledocs temporarily for this document.

While disabled all calls to roogledocs will silently abort.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleDocs$new(tokenDirectory, disabled);
    instance$disable()
            

#### Arguments

- none

#### Returns

R6 RoogleDocs object: itself - a fluent method

------------------------------------------------------------------------

### Method `getName()`

Return the name of the document

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleDocs$new(tokenDirectory, disabled);
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
    instance = J$RoogleDocs$new(tokenDirectory, disabled);
    instance$tagsDefined()
            

#### Arguments

- none

#### Returns

RDataframe: a dataframe containing tag and count columns

------------------------------------------------------------------------

### Method `updateTaggedText()`

Replace tags for text

Substitutes all occurrences of {{tag-name}} with the text parameter. If
the tag name is not found in the document it is inserted at the end in a
section labelled "Unmatched tags:". From there it can be cut and pasted
into the right place.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleDocs$new(tokenDirectory, disabled);
    instance$updateTaggedText(text, tagName)
            

#### Arguments

- text the value to replace the tag with (e.g. a result from analysis)
  (cannot be empty):

  \- (java expects a RCharacter)

- tagName the tag name - (defaulting to \`deparse(substitute(text))\`):

  \- (java expects a RCharacter)

#### Returns

R6 RoogleDocs object: itself - a fluent method

------------------------------------------------------------------------

### Method `updateTaggedImage()`

Replace a tag with an image.

Substitutes all occurrences of {{tag-name}} with an image from the local
storage.

The image is uploaded to your google drive as a temporary file, and
briefly made publicly readable. From there it is inserted into the
google doc, and one completed the temporary file deleted from your
google drive. Insertion is done using the dimensions of the existing
image (if present) or the PNG dimensions if not. Creating the image with
the correct dimensions (and providing the dpi if not 300) is important.

If the tag is missing from the document the image is inserted at the end
(and tagged). From there it can be cut and pasted to the correct place.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleDocs$new(tokenDirectory, disabled);
    instance$updateTaggedImage(absoluteFilePath, tagName, dpi, keepUpload)
            

#### Arguments

- absoluteFilePath a file path to an png image file.:

  \- (java expects a RCharacter)

- tagName the tag name - (defaulting to
  \`deparse(substitute(absoluteFilePath))\`):

  \- (java expects a RCharacter)

- dpi the dots per inch of the image in the document (defaults to 300) -
  (defaulting to \`300\`):

  \- (java expects a RNumeric)

- keepUpload keep the uploaded image as a supplementary file in the same
  directory as the google doc - (defaulting to \`FALSE\`):

  \- (java expects a RLogical)

#### Returns

R6 RoogleDocs object: itself - a fluent method

------------------------------------------------------------------------

### Method `updateTaggedTable()`

Replace a tag with a table.

Substitutes a unique occurrences of {{tag-name}} with a table. The tag
should either be in the text of the document or as the first entry in
the first cell of a table. Once inserted the table is tagged using a
zero width character as the very first item in the first cell. This will
be removed if \`removeTags()\` is called.

If the tag is missing the table is inserted at the end of the document
where it can be cut and pasted to the correct place.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleDocs$new(tokenDirectory, disabled);
    instance$updateTaggedTable(longFormatTable, tagName, colWidths, tableWidthInches)
            

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
  can be left out if longFormatTable comes from
  \`as.long_format_table\` - (defaulting to
  \`attr(longFormatTable,'colWidths')\`):

  \- (java expects a RNumericVector)

- tableWidthInches The final width of the table in inches (defaults to a
  size that fits in A4 page with margins) but you may want to make this
  wider for landscape tables - (defaulting to \`6.2\`):

  \- (java expects a RNumeric)

#### Returns

R6 RoogleDocs object: itself - a fluent method

------------------------------------------------------------------------

### Method `revertTags()`

Revert tagged text and images.

Remove all tagged text and images inserted by roogledocs and returns the
bare document the tags in place. This does not affect figures and tables
inserted by index (i.e. without tags) This is needed if content is being
moved around as cut and paste of tagged content unfortunately removes
the internal named range of the tag.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleDocs$new(tokenDirectory, disabled);
    instance$revertTags()
            

#### Arguments

- none

#### Returns

R6 RoogleDocs object: itself - a fluent method

------------------------------------------------------------------------

### Method `removeTags()`

Remove all tags

Finds tags defined in the current document and removes them. This cannot
be undone, except by rolling back to a previous version.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleDocs$new(tokenDirectory, disabled);
    instance$removeTags(confirm)
            

#### Arguments

- confirm - This action must be confirmed by passing \`true\` as cannot
  be undone. - (defaulting to \`(menu(c('Yes','No'), title = 'Are
  you...\`):

  \- (java expects a RLogical)

#### Returns

R6 RoogleDocs object: itself - a fluent method

------------------------------------------------------------------------

### Method `updateTable()`

Update or insert a formatted table into the document.

This function counts the number of tables from the start of the
document. Inserting tables by index works only if your document does not
change much or you are creating one from scratch. You can overwrite
tables using this function but if the order of tables has been changed
by your collaborators this will be generally cause probelms. Use of this
function is generally discouraged and we now prefer
\`updateTaggedTable()\`.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleDocs$new(tokenDirectory, disabled);
    instance$updateTable(longFormatTable, tableIndex, colWidths, tableWidthInches)
            

#### Arguments

- longFormatTable A dataframe consisting of the table content and
  formatting indexed by row and column. at a minimum this should have
  columns label,row,col, but may also include
  rowSpan,colSpan,fillColour, leftBorderWeight, rightBorderWeight,
  topBorderWeight, bottomBorderWeight, alignment (START,CENTER,END),
  valignment (TOP,MIDDLE,BOTTOM), fontName, fontFace, fontSize.:

  \- (java expects a RDataframe)

- tableIndex what is the table index in the document? This can be left
  out for a new table at the end of the document. - (defaulting to
  \`-1\`):

  \- (java expects a RInteger)

- colWidths A vector including the relative length of each column. This
  can be left out if longFormatTable comes from as.long_format_table -
  (defaulting to \`attr(longFormatTable,'colWidths')\`):

  \- (java expects a RNumericVector)

- tableWidthInches The final width of the table in inches (defaults to a
  size that fits in A4 page with margins) - (defaulting to \`6.2\`):

  \- (java expects a RNumeric)

#### Returns

R6 RoogleDocs object: itself - a fluent method

------------------------------------------------------------------------

### Method `updateFigure()`

Update or insert a figure in the document from a locally stored PNG by
index.

This function counts the number of inline images (i.e. not absolutely
positioned ones) from the start of the document. Inserting images by
index works only if your document does not change much or you are
creating one from scratch. You can overwrite images using this function
but if the order of images has been changed by your collaborators this
will generally cause problems. This function uploads the image into a
temporary file onto your Google Drive, and makes it briefly publicly
readable. From there inserts it into the google document. Once this is
complete the temporary google drive copy of the image is deleted.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleDocs$new(tokenDirectory, disabled);
    instance$updateFigure(absoluteFilePath, figureIndex, dpi, keepUpload)
            

#### Arguments

- absoluteFilePath a file path to an png image file (only png is
  supported at this point).:

  \- (java expects a RCharacter)

- figureIndex what is the figure index in the document? (This only
  counts inline images - and ignores absolutely positioned ones). leave
  out for a new image at the end of the document. - (defaulting to
  \`-1\`):

  \- (java expects a RInteger)

- dpi the dots per inch of the image in the document (defaults to 300).
  the final size of the image in the doc will be determined by the image
  file dimensions and the dpi. - (defaulting to \`300\`):

  \- (java expects a RNumeric)

- keepUpload keep the uploaded image as a supplementary file in the same
  directory as the google doc - (defaulting to \`FALSE\`):

  \- (java expects a RLogical)

#### Returns

R6 RoogleDocs object: itself - a fluent method

------------------------------------------------------------------------

### Method `saveAsPdf()`

Save the document as a PDF

Saves a snapshot of the current google doc with \`roogledocs\` links
removed as a pdf to a local drive. This is mainly intended for
snapshotting the current state of the document. For final export once
all analysis is complete it may be preferable to call
\`doc\$removeTags()\` and manually export the output but after this no
further updating is possible.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleDocs$new(tokenDirectory, disabled);
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

R6 RoogleDocs object: itself - a fluent method

------------------------------------------------------------------------

### Method `makeCopy()`

Make a copy of the current document

This makes a exact copy of the document under a new name. This name can
already exist as googledocs can have multiple files with the same file
name but this will certainly lead to confusion later. It is up to the
user to create a naming strategy that does not cause issues.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleDocs$new(tokenDirectory, disabled);
    instance$makeCopy(newName)
            

#### Arguments

- newName - The new document name.:

  \- (java expects a RCharacter)

#### Returns

R6 RoogleDocs object: a \`roogledocs\` object pointing to the new
document.

------------------------------------------------------------------------

### Method `delete()`

Delete the current document

Deleted documents can still be retrieved via the Google Drive website
but this is otherwise a final operation. After this any operations on
this document will fail with a null pointer exception.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleDocs$new(tokenDirectory, disabled);
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
directory as the document you are editing. This is handy for organising
all the files for a journal submission in one place. Any kind of file
can be loaded, and the mimetype will be detected. Normal Google Drive
rules for uploads will be triggered at this point. As google drive can
have multiple files with the same name the behaviour if the file already
exists is slightly complex, with \`overwrite\` and \`duplicate\`
options.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleDocs$new(tokenDirectory, disabled);
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

R6 RoogleDocs object: itself - a fluent method

------------------------------------------------------------------------

### Method `appendText()`

Append text to the document with optional paragraph styling. If you run
text blocks into each other without newlines the whole resulting
paragraph will be styled. You would normally not want this so it is up
to you to end paragraphs with a new line character, before changing
styles.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleDocs$new(tokenDirectory, disabled);
    instance$appendText(text, style)
            

#### Arguments

- text - a single string with the text to append which may include
  newlines:

  \- (java expects a RCharacter)

- style - one of NORMAL_TEXT, TITLE, SUBTITLE, HEADING_1, ...
  HEADING_6 - (defaulting to \`'NORMAL_TEXT'\`):

  \- (java expects a RCharacter)

#### Returns

R6 RoogleDocs object: itself - a fluent method

------------------------------------------------------------------------

### Method `appendFormattedParagraph()`

Append a new paragraph, with text from the 'label' column with optional
formating in the other columns.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleDocs$new(tokenDirectory, disabled);
    instance$appendFormattedParagraph(formattedTextDf)
            

#### Arguments

- formattedTextDf - a data frame containing the columns label, and
  optionally: link (as a URL), fontName, fontFace, fontSize.:

  \- (java expects a RDataframe)

#### Returns

R6 RoogleDocs object: itself - a fluent method

------------------------------------------------------------------------

### Method `updateCitations()`

Update citation tags in the document.

A citation tag is like this \`{{cite:challen2020;danon2021}}\`. The ids
are matched against the provided bibtex, and the tags are replaced with
an appropriate citation string. The bibliography itself is added to a
specific slide for references which can be decided with the
\`{{references}}\` tag.

If references do not already exist and there if no \`{{references}}\`
tag the references will be added to the end of the document. Where it
can be cut and pasted to the right place. N.B. Do not split up the
references when you move them.

#### Usage

    J = roogledocs::JavaApi$get()
    instance = J$RoogleDocs$new(tokenDirectory, disabled);
    instance$updateCitations(bibTexPath, citationStyle)
            

#### Arguments

- bibTexPath - the full file path to the file containing the bibtex:

  \- (java expects a RCharacter)

- citationStyle - the CSL specification - (defaulting to
  \`'ieee-with-url'\`):

  \- (java expects a RCharacter)

#### Returns

R6 RoogleDocs object: itself - a fluent method

## Examples

``` r
## -----------------------------------
## Construct new instance of RoogleDocs
## -----------------------------------
if (FALSE) { # \dontrun{
J = roogledocs::JavaApi$get()
# appropriate parameter values must be provided
instance = J$RoogleDocs$new(tokenDirectory, disabled)
} # }

## -----------------------------------
## Method `RoogleDocs$enable()`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$enable()
} # }

## -----------------------------------
## Method `RoogleDocs$disable()`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$disable()
} # }

## -----------------------------------
## Method `RoogleDocs$getName(suffix)`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$getName(suffix)
} # }

## -----------------------------------
## Method `RoogleDocs$tagsDefined()`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$tagsDefined()
} # }

## -----------------------------------
## Method `RoogleDocs$updateTaggedText(text, tagName)`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$updateTaggedText(text, tagName)
} # }

## -----------------------------------
## Method `RoogleDocs$updateTaggedImage(absoluteFilePath, tagName, dpi, keepUpload)`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$updateTaggedImage(absoluteFilePath, tagName, dpi, keepUpload)
} # }

## -----------------------------------
## Method `RoogleDocs$updateTaggedTable(longFormatTable, tagName, colWidths, tableWidthInches)`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$updateTaggedTable(longFormatTable, tagName, colWidths, tableWidthInches)
} # }

## -----------------------------------
## Method `RoogleDocs$revertTags()`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$revertTags()
} # }

## -----------------------------------
## Method `RoogleDocs$removeTags(confirm)`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$removeTags(confirm)
} # }

## -----------------------------------
## Method `RoogleDocs$updateTable(longFormatTable, tableIndex, colWidths, tableWidthInches)`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$updateTable(longFormatTable, tableIndex, colWidths, tableWidthInches)
} # }

## -----------------------------------
## Method `RoogleDocs$updateFigure(absoluteFilePath, figureIndex, dpi, keepUpload)`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$updateFigure(absoluteFilePath, figureIndex, dpi, keepUpload)
} # }

## -----------------------------------
## Method `RoogleDocs$saveAsPdf(absoluteFilePath, uploadCopy)`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$saveAsPdf(absoluteFilePath, uploadCopy)
} # }

## -----------------------------------
## Method `RoogleDocs$makeCopy(newName)`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$makeCopy(newName)
} # }

## -----------------------------------
## Method `RoogleDocs$delete(areYouSure)`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$delete(areYouSure)
} # }

## -----------------------------------
## Method `RoogleDocs$uploadSupplementaryFiles(absoluteFilePath, overwrite, duplicate)`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$uploadSupplementaryFiles(absoluteFilePath, overwrite, duplicate)
} # }

## -----------------------------------
## Method `RoogleDocs$appendText(text, style)`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$appendText(text, style)
} # }

## -----------------------------------
## Method `RoogleDocs$appendFormattedParagraph(formattedTextDf)`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$appendFormattedParagraph(formattedTextDf)
} # }

## -----------------------------------
## Method `RoogleDocs$updateCitations(bibTexPath, citationStyle)`
## -----------------------------------
if (FALSE) { # \dontrun{
# appropriate parameter values must be provided
instance$updateCitations(bibTexPath, citationStyle)
} # }
```
