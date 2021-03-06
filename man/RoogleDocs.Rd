% Generated by r6-generator-maven-plugin: do not edit by hand

\name{RoogleDocs}

\alias{RoogleDocs}

\title{RoogleDocs}

\arguments{
	\if{html}{\out{<div class="arguments">}}
	\item{tokenDirectory}{tokenDirectory the place to store authentication tokens. This should not be checked into version control. - (defaulting to .tokenDirectory()) - (java expects a String)}
	\item{disabled}{disabled a flag to switch roogledocs off (on a document by document basis, for testing or development. This can be set globally with `options('roogledocs.disabled'=TRUE)` - (defaulting to getOption('roogledocs.disabled',FALSE)) - (java expects a boolean)}
	\if{html}{\out{</div>}}
}


\description{
Programmatically substitute images, data and tables into a google doc. 

Version: 0.0.3.9999

Generated: 2022-07-08T22:54:10.915
}

\details{
	
R library to perform limited interactions with google docs (and maybe one day slides)
in R via the Java API library. The purpose being to support google docs as a
platform for interactive development and documentation of data analysis in R for scientific
publication, although it is not limited to this purpose. The workflow supported is a parallel documentation and analysis
where a team of people are working collaboratively on documentation, whilst at the same time analysis
is being performed and results updated repeatedly as a result of new data. In this environment updating
numeric results, tabular data and figures in word documents manually becomes annoying. With roogledocs
you can automate this a bit like a RMarkdown document, but with the added benefit that the content
can be updated independently of the analysis, by the wider team.
}

\examples{\dontrun{
J = roogledocs::JavaApi$get();
instance = J$RoogleDocs$new(tokenDirectory, disabled)

## -----------------------------------
## Method `RoogleDocs$reauth`
## -----------------------------------

## -----------------------------------
## Method `RoogleDocs$enable`
## -----------------------------------

## -----------------------------------
## Method `RoogleDocs$disable`
## -----------------------------------

## -----------------------------------
## Method `RoogleDocs$withDocument`
## -----------------------------------

## -----------------------------------
## Method `RoogleDocs$docById`
## -----------------------------------

## -----------------------------------
## Method `RoogleDocs$findOrCreateDocument`
## -----------------------------------

## -----------------------------------
## Method `RoogleDocs$docByName`
## -----------------------------------

## -----------------------------------
## Method `RoogleDocs$findOrCloneTemplate`
## -----------------------------------

## -----------------------------------
## Method `RoogleDocs$docFromTemplate`
## -----------------------------------

## -----------------------------------
## Method `RoogleDocs$searchForDocuments`
## -----------------------------------

## -----------------------------------
## Method `RoogleDocs$tagsDefined`
## -----------------------------------

## -----------------------------------
## Method `RoogleDocs$updateTaggedText`
## -----------------------------------

## -----------------------------------
## Method `RoogleDocs$updateTaggedImage`
## -----------------------------------

## -----------------------------------
## Method `RoogleDocs$revertTags`
## -----------------------------------

## -----------------------------------
## Method `RoogleDocs$updateTable`
## -----------------------------------

## -----------------------------------
## Method `RoogleDocs$updateFigure`
## -----------------------------------

## -----------------------------------
## Method `RoogleDocs$saveAsPdf`
## -----------------------------------

## -----------------------------------
## Method `RoogleDocs$deleteDocument`
## -----------------------------------

## -----------------------------------
## Method `RoogleDocs$appendText`
## -----------------------------------

## -----------------------------------
## Method `RoogleDocs$appendFormattedParagraph`
## -----------------------------------

}}

\keyword{java api}

\section{Methods}{
	\subsection{Constructors}{
		\itemize{
			\item \href{#method-new}{\code{J$RoogleDocs$new(tokenDirectory, disabled)}}
		}
	}
	\subsection{Static methods}{
		\itemize{
			\item \href{#method-docById}{\code{J$RoogleDocs$docById(shareUrlOrDocId, tokenDirectory, disabled)}}
			\item \href{#method-docByName}{\code{J$RoogleDocs$docByName(title, tokenDirectory, disabled)}}
			\item \href{#method-docFromTemplate}{\code{J$RoogleDocs$docFromTemplate(title, templateUri, tokenDirectory, disabled)}}
			\item \href{#method-searchForDocuments}{\code{J$RoogleDocs$searchForDocuments(titleMatch, tokenDirectory)}}
			\item \href{#method-deleteDocument}{\code{J$RoogleDocs$deleteDocument(docName, areYouSure, tokenDirectory, disabled)}}
		}
	}
	\subsection{Instance methods}{
		\itemize{
			\item \href{#method-reauth}{\code{instance$reauth()}}
			\item \href{#method-enable}{\code{instance$enable()}}
			\item \href{#method-disable}{\code{instance$disable()}}
			\item \href{#method-withDocument}{\code{instance$withDocument(shareUrlOrDocId)}}
			\item \href{#method-findOrCreateDocument}{\code{instance$findOrCreateDocument(title)}}
			\item \href{#method-findOrCloneTemplate}{\code{instance$findOrCloneTemplate(title, templateUri)}}
			\item \href{#method-tagsDefined}{\code{instance$tagsDefined()}}
			\item \href{#method-updateTaggedText}{\code{instance$updateTaggedText(text, tagName)}}
			\item \href{#method-updateTaggedImage}{\code{instance$updateTaggedImage(absoluteFilePath, tagName, dpi)}}
			\item \href{#method-revertTags}{\code{instance$revertTags()}}
			\item \href{#method-updateTable}{\code{instance$updateTable(longFormatTable, tableIndex, colWidths, tableWidthInches)}}
			\item \href{#method-updateFigure}{\code{instance$updateFigure(absoluteFilePath, figureIndex, dpi)}}
			\item \href{#method-saveAsPdf}{\code{instance$saveAsPdf(absoluteFilePath)}}
			\item \href{#method-appendText}{\code{instance$appendText(text, style)}}
			\item \href{#method-appendFormattedParagraph}{\code{instance$appendFormattedParagraph(formattedTextDf)}}
			\item \code{instance$clone()}
			\item \code{instance$print()}
		}
	}

	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-new"></a>}}
	
	\subsection{Method \code{new()}}{
		Create a Roogledocs object for managing the interaction.
	

		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
J$RoogleDocs$new(tokenDirectory, disabled)
  			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{tokenDirectory the place to store authentication tokens. This should not be checked into version control. - (defaulting to .tokenDirectory())}{ - (java expects a String)}
				\item{disabled a flag to switch roogledocs off (on a document by document basis, for testing or development. This can be set globally with `options('roogledocs.disabled'=TRUE)` - (defaulting to getOption('roogledocs.disabled',FALSE))}{ - (java expects a boolean)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			R6 RoogleDocs object: 
		}

		\subsection{Examples}{
			\if{html}{\out{<div class="r example copy">}}
			\preformatted{
not available
			}
			\if{html}{\out{</div>}}
		}
	}
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-reauth"></a>}}
	
	\subsection{Method \code{reauth()}}{
		Reauthenticate the service deleting the existing OAuth tokens. Generally this would only be needed if 
application permission updates are needed in which case the directory can be manually deleted anyway,
or if you want to switch google user without using a different tokenDirectory.
	

		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
instance = J$RoogleDocs$new(...);
instance$reauth()
  			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{none}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			R6 RoogleDocs object: a fluent method
		}

		\subsection{Examples}{
			\if{html}{\out{<div class="r example copy">}}
			\preformatted{
not available
			}
			\if{html}{\out{</div>}}
		}
	}
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-enable"></a>}}
	
	\subsection{Method \code{enable()}}{
		Enables roogledocs. It is likely one of `withDocument()`, `findOrCreateDocument()` or `findOrCloneTemplate()` methods will be needed to specify the document.
	

		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
instance = J$RoogleDocs$new(...);
instance$enable()
  			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{none}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			R6 RoogleDocs object: itself - a fluent method
		}

		\subsection{Examples}{
			\if{html}{\out{<div class="r example copy">}}
			\preformatted{
not available
			}
			\if{html}{\out{</div>}}
		}
	}
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-disable"></a>}}
	
	\subsection{Method \code{disable()}}{
		disables roogledocs temporarily only for this document. While disabled all calls to roogledocs will silently fail.
	

		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
instance = J$RoogleDocs$new(...);
instance$disable()
  			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{none}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			R6 RoogleDocs object: itself - a fluent method
		}

		\subsection{Examples}{
			\if{html}{\out{<div class="r example copy">}}
			\preformatted{
not available
			}
			\if{html}{\out{</div>}}
		}
	}
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-withDocument"></a>}}
	
	\subsection{Method \code{withDocument()}}{
		Select a document by its share url or id.
	

		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
instance = J$RoogleDocs$new(...);
instance$withDocument(shareUrlOrDocId)
  			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{shareUrlOrDocId the url from clicking a share button in google docs or an id from searchForDocuments() method}{ - (java expects a String)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			R6 RoogleDocs object: itself - a fluent method
		}

		\subsection{Examples}{
			\if{html}{\out{<div class="r example copy">}}
			\preformatted{
not available
			}
			\if{html}{\out{</div>}}
		}
	}
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-docById"></a>}}
	
	\subsection{Method \code{docById()}}{
		Select a document by its share url or id.
	

		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
J$RoogleDocs$docById(shareUrlOrDocId, tokenDirectory, disabled)
  			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{shareUrlOrDocId the url from clicking a share button in google docs or an id from searchForDocuments() method}{ - (java expects a String)}
				\item{tokenDirectory the place to store authentication tokens. This should not be checked into version control. - (defaulting to .tokenDirectory())}{ - (java expects a String)}
				\item{disabled a flag to switch roogledocs off (on a document by document basis, for testing or development. This can be set globally with `options('roogledocs.disabled'=TRUE)` - (defaulting to getOption('roogledocs.disabled',FALSE))}{ - (java expects a boolean)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			R6 RoogleDocs object: itself - a fluent method
		}

		\subsection{Examples}{
			\if{html}{\out{<div class="r example copy">}}
			\preformatted{
not available
			}
			\if{html}{\out{</div>}}
		}
	}
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-findOrCreateDocument"></a>}}
	
	\subsection{Method \code{findOrCreateDocument()}}{
		Search for a document by name or create one if missing.
	

		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
instance = J$RoogleDocs$new(...);
instance$findOrCreateDocument(title)
  			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{title a document title. If there is an exact match in google drive then that document will be used 
otherwise a new one will be created.}{ - (java expects a String)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			R6 RoogleDocs object: itself - a fluent method
		}

		\subsection{Examples}{
			\if{html}{\out{<div class="r example copy">}}
			\preformatted{
not available
			}
			\if{html}{\out{</div>}}
		}
	}
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-docByName"></a>}}
	
	\subsection{Method \code{docByName()}}{
		Search for a document by name or create one if missing.
	

		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
J$RoogleDocs$docByName(title, tokenDirectory, disabled)
  			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{title a document title. If there is an exact match in google drive then that document will be used}{ - (java expects a String)}
				\item{tokenDirectory the place to store authentication tokens. This should not be checked into version control. - (defaulting to .tokenDirectory())}{ - (java expects a String)}
				\item{disabled a flag to switch roogledocs off (on a document by document basis, for testing or development. This can be set globally with `options('roogledocs.disabled'=TRUE)` - (defaulting to getOption('roogledocs.disabled',FALSE))}{ - (java expects a boolean)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			R6 RoogleDocs object: itself - a fluent method
		}

		\subsection{Examples}{
			\if{html}{\out{<div class="r example copy">}}
			\preformatted{
not available
			}
			\if{html}{\out{</div>}}
		}
	}
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-findOrCloneTemplate"></a>}}
	
	\subsection{Method \code{findOrCloneTemplate()}}{
		Search for or create a document as a copy of a template document if it is missing.
	

		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
instance = J$RoogleDocs$new(...);
instance$findOrCloneTemplate(title, templateUri)
  			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{title a document title. If there is an exact match in google drive then that document will be used
otherwise a new one will be created.}{ - (java expects a String)}
				\item{templateUri the share link (or document id) of a template google document}{ - (java expects a String)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			R6 RoogleDocs object: itself - a fluent method
		}

		\subsection{Examples}{
			\if{html}{\out{<div class="r example copy">}}
			\preformatted{
not available
			}
			\if{html}{\out{</div>}}
		}
	}
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-docFromTemplate"></a>}}
	
	\subsection{Method \code{docFromTemplate()}}{
		Search for a document by name or create one if missing.
	

		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
J$RoogleDocs$docFromTemplate(title, templateUri, tokenDirectory, disabled)
  			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{title a document title. If there is an exact match in google drive then that document will be used
otherwise a new one will be created.}{ - (java expects a String)}
				\item{templateUri the share link (or document id) of a template google document}{ - (java expects a String)}
				\item{tokenDirectory the place to store authentication tokens. This should not be checked into version control. - (defaulting to .tokenDirectory())}{ - (java expects a String)}
				\item{disabled a flag to switch roogledocs off (on a document by document basis, for testing or development. This can be set globally with `options('roogledocs.disabled'=TRUE)` - (defaulting to getOption('roogledocs.disabled',FALSE))}{ - (java expects a boolean)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			R6 RoogleDocs object: itself - a fluent method
		}

		\subsection{Examples}{
			\if{html}{\out{<div class="r example copy">}}
			\preformatted{
not available
			}
			\if{html}{\out{</div>}}
		}
	}
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-searchForDocuments"></a>}}
	
	\subsection{Method \code{searchForDocuments()}}{
		Search for documents with the given title
	

		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
J$RoogleDocs$searchForDocuments(titleMatch, tokenDirectory)
  			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{titleMatch a string to be searched for as an approximate match. All results will be retrieved with document ids.}{ - (java expects a String)}
				\item{tokenDirectory the place to store authentication tokens. This should not be checked into version control. - (defaulting to .tokenDirectory())}{ - (java expects a String)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			RDataframe: a dataframe containing id and name columns
		}

		\subsection{Examples}{
			\if{html}{\out{<div class="r example copy">}}
			\preformatted{
not available
			}
			\if{html}{\out{</div>}}
		}
	}
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-tagsDefined"></a>}}
	
	\subsection{Method \code{tagsDefined()}}{
		Finds tags defined in the current document
	

		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
instance = J$RoogleDocs$new(...);
instance$tagsDefined()
  			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{none}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			RDataframe: a dataframe containing tag and count columns
		}

		\subsection{Examples}{
			\if{html}{\out{<div class="r example copy">}}
			\preformatted{
not available
			}
			\if{html}{\out{</div>}}
		}
	}
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-updateTaggedText"></a>}}
	
	\subsection{Method \code{updateTaggedText()}}{
		Substitutes all occurrences of {{tag-name}} with the text parameter.
	

		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
instance = J$RoogleDocs$new(...);
instance$updateTaggedText(text, tagName)
  			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{text the value to replace the tag with (e.g. a result from analysis) (cannot be empty)}{ - (java expects a String)}
				\item{tagName the tag name}{ - (java expects a String)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			R6 RoogleDocs object: itself - a fluent method
		}

		\subsection{Examples}{
			\if{html}{\out{<div class="r example copy">}}
			\preformatted{
not available
			}
			\if{html}{\out{</div>}}
		}
	}
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-updateTaggedImage"></a>}}
	
	\subsection{Method \code{updateTaggedImage()}}{
		Substitutes all occurrences of {{tag-name}} with an image from the local storage. There are limited circumstances
in which using this is a good idea. It will almost always be better to use `updateFigure()` to insert an image
by index. If you choose to ignore this warning, beware combining this with `updateFigure()` as potentially the figure indexes will
change dynamically. You have been warned.
	

		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
instance = J$RoogleDocs$new(...);
instance$updateTaggedImage(absoluteFilePath, tagName, dpi)
  			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{absoluteFilePath a file path to an png image file.}{ - (java expects a String)}
				\item{tagName the tag name}{ - (java expects a String)}
				\item{dpi the dots per inch of the image in the document (defaults to 300) - (defaulting to 300)}{ - (java expects a double)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			R6 RoogleDocs object: itself - a fluent method
		}

		\subsection{Examples}{
			\if{html}{\out{<div class="r example copy">}}
			\preformatted{
not available
			}
			\if{html}{\out{</div>}}
		}
	}
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-revertTags"></a>}}
	
	\subsection{Method \code{revertTags()}}{
		remove all tagged text and images inserted by roogledocs and returns the bare document. This does not affect figures and tables inserted by index (i.e. without tags) 
This is needed if content is being moved around as cut and paste of tagged content unfortunately removes the internal named range of the tag.
	

		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
instance = J$RoogleDocs$new(...);
instance$revertTags()
  			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{none}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			R6 RoogleDocs object: itself - a fluent method
		}

		\subsection{Examples}{
			\if{html}{\out{<div class="r example copy">}}
			\preformatted{
not available
			}
			\if{html}{\out{</div>}}
		}
	}
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-updateTable"></a>}}
	
	\subsection{Method \code{updateTable()}}{
		Update or insert a formatted table into the document. The table and formatting are described in a dataframe the format of which is documented in the as.long_format_table() method.
	

		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
instance = J$RoogleDocs$new(...);
instance$updateTable(longFormatTable, tableIndex, colWidths, tableWidthInches)
  			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{longFormatTable A dataframe consisting of the table content and formatting indexed by row and column. at a minimum this should have columns label,row,col, but may also include
rowSpan,colSpan,fillColour, leftBorderWeight, rightBorderWeight, topBorderWeight, bottomBorderWeight, alignment (START,CENTER,END), valignment (TOP,MIDDLE,BOTTOM), fontName, fontFace, fontSize.}{ - (java expects a RDataframe)}
				\item{tableIndex what is the table index in the document? This can be left out for a new table at the end of the document. - (defaulting to -1)}{ - (java expects a int)}
				\item{colWidths A vector including the relative length of each column. This can be left out if longFormatTable comes from as.long_format_table - (defaulting to attr(longFormatTable,'colWidths'))}{ - (java expects a RNumericVector)}
				\item{tableWidthInches The final width of the table in inches (defaults to a size that fits in A4 page with margins) - (defaulting to 6.2)}{ - (java expects a RNumeric)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			R6 RoogleDocs object: itself - a fluent method
		}

		\subsection{Examples}{
			\if{html}{\out{<div class="r example copy">}}
			\preformatted{
not available
			}
			\if{html}{\out{</div>}}
		}
	}
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-updateFigure"></a>}}
	
	\subsection{Method \code{updateFigure()}}{
		Update or insert a figure in the document.
	

		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
instance = J$RoogleDocs$new(...);
instance$updateFigure(absoluteFilePath, figureIndex, dpi)
  			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{absoluteFilePath a file path to an png image file (only png is supported at this point).}{ - (java expects a String)}
				\item{figureIndex what is the figure index in the document? (This only counts inline images - and ignores absolutely positioned ones). leave out for a new image at the end of the document. - (defaulting to -1)}{ - (java expects a int)}
				\item{dpi the dots per inch of the image in the document (defaults to 300). the final size of the image in the doc will be determined by the image file dimensions and the dpi. - (defaulting to 300)}{ - (java expects a double)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			R6 RoogleDocs object: itself - a fluent method
		}

		\subsection{Examples}{
			\if{html}{\out{<div class="r example copy">}}
			\preformatted{
not available
			}
			\if{html}{\out{</div>}}
		}
	}
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-saveAsPdf"></a>}}
	
	\subsection{Method \code{saveAsPdf()}}{
		Saves a snapshot of the current google doc as a pdf to a local drive. This is mainly intended for testing.
	

		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
instance = J$RoogleDocs$new(...);
instance$saveAsPdf(absoluteFilePath)
  			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{absoluteFilePath - a file path to save the pdf.}{ - (java expects a String)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			R6 RoogleDocs object: itself - a fluent method
		}

		\subsection{Examples}{
			\if{html}{\out{<div class="r example copy">}}
			\preformatted{
not available
			}
			\if{html}{\out{</div>}}
		}
	}
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-deleteDocument"></a>}}
	
	\subsection{Method \code{deleteDocument()}}{
		Deletes a google document by name.
	

		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
J$RoogleDocs$deleteDocument(docName, areYouSure, tokenDirectory, disabled)
  			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{docName - the name of a document to delete. must be an exact and unique match.}{ - (java expects a String)}
				\item{areYouSure - a boolean check. - (defaulting to utils::askYesNo(paste0('Are you sure you want to delete ',docName),FALSE))}{ - (java expects a boolean)}
				\item{tokenDirectory - (defaulting to .tokenDirectory())}{ - (java expects a String)}
				\item{disabled - (defaulting to getOption('roogledocs.disabled',FALSE))}{ - (java expects a boolean)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			void: nothing, called for side efffects
		}

		\subsection{Examples}{
			\if{html}{\out{<div class="r example copy">}}
			\preformatted{
not available
			}
			\if{html}{\out{</div>}}
		}
	}
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-appendText"></a>}}
	
	\subsection{Method \code{appendText()}}{
		Append text to the document with optional paragraph styling. If you run text blocks into each other without newlines the whole resulting paragraph will be styled. You 
would normally not want this so it is up to you to end paragraphs with a new line character, before changing styles.
	

		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
instance = J$RoogleDocs$new(...);
instance$appendText(text, style)
  			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{text - a single string with the text to append which may include newlines}{ - (java expects a RCharacter)}
				\item{style - one of NORMAL_TEXT, TITLE, SUBTITLE, HEADING_1, ... HEADING_6 - (defaulting to 'NORMAL_TEXT')}{ - (java expects a RCharacter)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			R6 RoogleDocs object: itself - a fluent method
		}

		\subsection{Examples}{
			\if{html}{\out{<div class="r example copy">}}
			\preformatted{
not available
			}
			\if{html}{\out{</div>}}
		}
	}
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-appendFormattedParagraph"></a>}}
	
	\subsection{Method \code{appendFormattedParagraph()}}{
		Append a new paragraph, with text from the 'label' column with optional formating in the other columns.
	

		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
instance = J$RoogleDocs$new(...);
instance$appendFormattedParagraph(formattedTextDf)
  			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item{formattedTextDf - a data frame containing the columns label, and optionally: link (as a URL), fontName, fontFace, fontSize.}{ - (java expects a RDataframe)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			R6 RoogleDocs object: itself - a fluent method
		}

		\subsection{Examples}{
			\if{html}{\out{<div class="r example copy">}}
			\preformatted{
not available
			}
			\if{html}{\out{</div>}}
		}
	}
}