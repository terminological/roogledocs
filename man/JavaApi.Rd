% Generated by r6-generator-maven-plugin: do not edit by hand

\name{JavaApi}

\alias{JavaApi}

\title{R Wrapper For Googledocs Java Library}

\section{Usage}{
	\if{html}{\out{<div class="r">}}
	\preformatted{
 J = roogledocs::JavaApi$get(logLevel)
 	}
  \if{html}{\out{</div>}}
}

\arguments{
	\if{html}{\out{<div class="arguments">}}
	\item{logLevel}{optional - the slf4j log level as a string - one of OFF (most specific, no logging), 
	FATAL (most specific, little data), ERROR, WARN, INFO, DEBUG, 
	TRACE (least specific, a lot of data), ALL (least specific, all data)}
	\if{html}{\out{</div>}}
}

\description{
Programmatically substitute images, data and tables into a google doc or presentation. R library to perform limited interactions with google docs and slides in R via the Java API library. The purpose being to support google docs as a platform for interactive development and documentation of data analysis in R for scientific publication, although it is not limited to this purpose. The workflow supported is a parallel documentation and analysis where a team of people are working collaboratively on documentation, whilst at the same time analysis is being performed and results updated repeatedly as a result of new data. In this environment updating numeric results, tabular data and figures in word documents manually becomes annoying. With roogledocs you can automate this a bit like a RMarkdown document, but with the added benefit that the content can be updated independently of the analysis, by the wider team.

Version: 0.5.0

Generated: 2024-04-27T13:56:10.506316288
}

\author{\email{rob.challen@bristol.ac.uk}}

\examples{
## -----------------------------------
## Check library dependencies for roogledocs
## -----------------------------------
roogledocs::JavaApi$installDependencies()

## -----------------------------------
## Construct a roogledocs Java API instance
## -----------------------------------

J = roogledocs::JavaApi$get()
# or a more verbose configuration
# J = roogledocs::JavaApi$get("DEBUG")


## -----------------------------------
## Method `J$RoogleDocs$new(...)`
## -----------------------------------
\dontrun{
# no example given - appropriate parameter values must be provided:
J$RoogleDocs$new(tokenDirectory, disabled)
# or alternatively:
roogledocs::new(tokenDirectory, disabled)
}

## -----------------------------------
## Method `J$RoogleDocs$reauth(...)`
## Aliased as `roogledocs::reauth(...)`
## -----------------------------------
\dontrun{
# no example given - appropriate parameter values must be provided:
J$RoogleDocs$reauth(tokenDirectory)
# or alternatively:
roogledocs::reauth(tokenDirectory)
}

## -----------------------------------
## Method `J$RoogleDocs$docById(...)`
## Aliased as `roogledocs::doc_by_id(...)`
## -----------------------------------
\dontrun{
# no example given - appropriate parameter values must be provided:
J$RoogleDocs$docById(shareUrlOrDocId, tokenDirectory, disabled)
# or alternatively:
roogledocs::doc_by_id(shareUrlOrDocId, tokenDirectory, disabled)
}

## -----------------------------------
## Method `J$RoogleDocs$docByName(...)`
## Aliased as `roogledocs::doc_by_name(...)`
## -----------------------------------
\dontrun{
# no example given - appropriate parameter values must be provided:
J$RoogleDocs$docByName(title, tokenDirectory, disabled)
# or alternatively:
roogledocs::doc_by_name(title, tokenDirectory, disabled)
}

## -----------------------------------
## Method `J$RoogleDocs$docFromTemplate(...)`
## Aliased as `roogledocs::doc_from_template(...)`
## -----------------------------------
\dontrun{
# no example given - appropriate parameter values must be provided:
J$RoogleDocs$docFromTemplate(title, templateUri, tokenDirectory, disabled)
# or alternatively:
roogledocs::doc_from_template(title, templateUri, tokenDirectory, disabled)
}

## -----------------------------------
## Method `J$RoogleDocs$searchForDocuments(...)`
## Aliased as `roogledocs::search_for_documents(...)`
## -----------------------------------
\dontrun{
# no example given - appropriate parameter values must be provided:
J$RoogleDocs$searchForDocuments(titleMatch, tokenDirectory)
# or alternatively:
roogledocs::search_for_documents(titleMatch, tokenDirectory)
}

## -----------------------------------
## Method `J$RoogleDocs$deleteDocument(...)`
## Aliased as `roogledocs::delete_document(...)`
## -----------------------------------
\dontrun{
# no example given - appropriate parameter values must be provided:
J$RoogleDocs$deleteDocument(docName, areYouSure, tokenDirectory, disabled)
# or alternatively:
roogledocs::delete_document(docName, areYouSure, tokenDirectory, disabled)
}

## -----------------------------------
## Method `J$RoogleDocs$citationStyles(...)`
## Aliased as `roogledocs::citation_styles(...)`
## -----------------------------------
\dontrun{
# no example given - appropriate parameter values must be provided:
J$RoogleDocs$citationStyles()
# or alternatively:
roogledocs::citation_styles()
}

## -----------------------------------
## Method `J$RoogleSlides$new(...)`
## -----------------------------------
\dontrun{
# no example given - appropriate parameter values must be provided:
J$RoogleSlides$new(tokenDirectory, disabled)
# or alternatively:
roogledocs::new(tokenDirectory, disabled)
}

## -----------------------------------
## Method `J$RoogleSlides$slidesById(...)`
## Aliased as `roogledocs::slides_by_id(...)`
## -----------------------------------
\dontrun{
# no example given - appropriate parameter values must be provided:
J$RoogleSlides$slidesById(shareUrlOrDocId, tokenDirectory, disabled)
# or alternatively:
roogledocs::slides_by_id(shareUrlOrDocId, tokenDirectory, disabled)
}

## -----------------------------------
## Method `J$RoogleSlides$slidesByName(...)`
## Aliased as `roogledocs::slides_by_name(...)`
## -----------------------------------
\dontrun{
# no example given - appropriate parameter values must be provided:
J$RoogleSlides$slidesByName(title, tokenDirectory, disabled)
# or alternatively:
roogledocs::slides_by_name(title, tokenDirectory, disabled)
}

## -----------------------------------
## Method `J$RoogleSlides$slidesFromTemplate(...)`
## Aliased as `roogledocs::slides_from_template(...)`
## -----------------------------------
\dontrun{
# no example given - appropriate parameter values must be provided:
J$RoogleSlides$slidesFromTemplate(title, templateUri, tokenDirectory, disabled)
# or alternatively:
roogledocs::slides_from_template(title, templateUri, tokenDirectory, disabled)
}

## -----------------------------------
## Method `J$RoogleSlides$searchForSlides(...)`
## Aliased as `roogledocs::search_for_slides(...)`
## -----------------------------------
\dontrun{
# no example given - appropriate parameter values must be provided:
J$RoogleSlides$searchForSlides(titleMatch, tokenDirectory)
# or alternatively:
roogledocs::search_for_slides(titleMatch, tokenDirectory)
}

## -----------------------------------
## Method `J$RoogleSlides$deleteSlides(...)`
## Aliased as `roogledocs::delete_slides(...)`
## -----------------------------------
\dontrun{
# no example given - appropriate parameter values must be provided:
J$RoogleSlides$deleteSlides(docName, areYouSure, tokenDirectory, disabled)
# or alternatively:
roogledocs::delete_slides(docName, areYouSure, tokenDirectory, disabled)
}
}

\keyword{java api}

\section{Package initialisation and control}{
	\itemize{
		\item \href{#method-api-installDependencies}{\code{JavaApi$installDependencies()}}
		\item \href{#method-api-rebuildDependencies}{\code{JavaApi$rebuildDependencies()}}
		\item \href{#method-api-versionInformation}{\code{JavaApi$versionInformation()}}
		\item \href{#method-api-get}{\code{J = JavaApi$get(logLevel)}}
		\item \href{#method-api-changeLogLevel}{\code{J$changeLogLevel(logLevel)}}
		\item \href{#method-api-reconfigureLog}{\code{J$reconfigureLog(log4jproperties)}}
		\item \href{#method-api-printMessages}{\code{J$printMessages()}}
	}
}

\section{Package classes and static methods}{
	\if{html}{\out{<hr>}}
	\itemize{
		\item \href{#method-RoogleDocs-new}{\code{J$RoogleDocs$new(tokenDirectory, disabled)}}
		\item \href{#method-RoogleDocs-reauth}{\code{J$RoogleDocs$reauth(tokenDirectory)}}
		\item \href{#method-RoogleDocs-docById}{\code{J$RoogleDocs$docById(shareUrlOrDocId, tokenDirectory, disabled)}}
		\item \href{#method-RoogleDocs-docByName}{\code{J$RoogleDocs$docByName(title, tokenDirectory, disabled)}}
		\item \href{#method-RoogleDocs-docFromTemplate}{\code{J$RoogleDocs$docFromTemplate(title, templateUri, tokenDirectory, disabled)}}
		\item \href{#method-RoogleDocs-searchForDocuments}{\code{J$RoogleDocs$searchForDocuments(titleMatch, tokenDirectory)}}
		\item \href{#method-RoogleDocs-deleteDocument}{\code{J$RoogleDocs$deleteDocument(docName, areYouSure, tokenDirectory, disabled)}}
		\item \href{#method-RoogleDocs-citationStyles}{\code{J$RoogleDocs$citationStyles()}}
	}
	\if{html}{\out{<hr>}}
	\itemize{
		\item \href{#method-RoogleSlides-new}{\code{J$RoogleSlides$new(tokenDirectory, disabled)}}
		\item \href{#method-RoogleSlides-slidesById}{\code{J$RoogleSlides$slidesById(shareUrlOrDocId, tokenDirectory, disabled)}}
		\item \href{#method-RoogleSlides-slidesByName}{\code{J$RoogleSlides$slidesByName(title, tokenDirectory, disabled)}}
		\item \href{#method-RoogleSlides-slidesFromTemplate}{\code{J$RoogleSlides$slidesFromTemplate(title, templateUri, tokenDirectory, disabled)}}
		\item \href{#method-RoogleSlides-searchForSlides}{\code{J$RoogleSlides$searchForSlides(titleMatch, tokenDirectory)}}
		\item \href{#method-RoogleSlides-deleteSlides}{\code{J$RoogleSlides$deleteSlides(docName, areYouSure, tokenDirectory, disabled)}}
	}
}

\section{Package initialisation and control}{
	\if{html}{\out{<a id="method-api-installDependencies"></a>}}
	\subsection{Package method \code{JavaApi$installDependencies()}}{
		This package level method checks for, and installs any dependencies needed for the running of the package.
		It is called automatically on first package load and so in general does not need to be used directly.
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
roogledocs::JavaApi$installDependencies()
			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item none
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
nothing. called for side effects.
		}
	}
	
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-api-rebuildDependencies"></a>}}
	\subsection{Package method \code{JavaApi$rebuildDependencies()}}{
		This package level method removes existing dependencies and re-installs dependencies needed for the running of the package.
		It is called automatically on first package load and so in general does not need to be called.
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
roogledocs::JavaApi$rebuildDependencies()
			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item none
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
nothing. called for side effects.
		}
	}
	
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-api-versionInformation"></a>}}
	\subsection{Package method \code{JavaApi$versionInformation()}}{
		This package level method returns debugging version information for the package
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
roogledocs::JavaApi$versionInformation()
			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item none
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
A list containing a set of versioning information about this package.
		}
	}
	
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-api-get"></a>}}
	\subsection{Package method \code{JavaApi$get()}}{
		This is the main entry point for the package and the root of the Java API in this package. All classes defined in the package
		are made available as items under this root. The JavaApi object manages the communication between R and Java.
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
# package classes and functions are nested under the `J` api object.
			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\describe{
				\item{logLevel}{ The desired verbosity of the package. One of "OFF", "FATAL", "ERROR", "WARN", "INFO", "DEBUG", "TRACE", "ALL".}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
A R6 roogledocs::JavaApi object containing the access point to the objects and functions defined in this package 
		}
	}
	
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-api-changeLogLevel"></a>}}
	\subsection{Api method \code{J$changeLogLevel(logLevel)}}{
		Once the package is initialised the log level can be changed to increase the level of messages from the api.
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
J$changeLogLevel("DEBUG")
			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\describe{
				\item{logLevel}{ The desired verbosity of the package. One of "OFF", "FATAL", "ERROR", "WARN", "INFO", "DEBUG", "TRACE", "ALL".}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
nothing. used for side effects.
		}
	}
	
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-api-reconfigreLog"></a>}}
	\subsection{Api method \code{J$reconfigureLog(log4jproperties)}}{
		Experimental / Advanced use: Once the package is initialised the log configureation can be changed to log to 
		an external file for example.
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
prp = fs::path(getwd(),"log4j.properties")
if (fs::file_exists(prp)) {
	J$changeLogLevel(prp)
}
			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\describe{
				\item{log4jproperties}{ a full path to a log4jproperies file}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
nothing. used for side effects.
		}
	}
	
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-api-printMessages"></a>}}
	\subsection{Api method \code{J$printMessages()}}{
		Experimental / Internal use: Messages from Java to R are queued and
		printed after each function call. It is unlikely that any will be not printed
		so in normal circumstances this function should do nothing.
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
J$printMessages()
			}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item none
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
nothing. used for side effects.
		}
	}
	
}


\section{Static methods and constructors}{
	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-RoogleDocs-new"></a>}}
	
	\subsection{Method \code{RoogleDocs$new()}}{
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
			\describe{
				\item{tokenDirectory the place to store authentication tokens. This should not be checked into version control. - (defaulting to `.tokenDirectory()`)}{ - (java expects a RCharacter)}
				\item{disabled a flag to switch roogledocs off (on a document by document basis, for testing or development. This can be set globally with `options('roogledocs.disabled'=TRUE)` - (defaulting to `getOption('roogledocs.disabled',FALSE)`)}{ - (java expects a RLogical)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			R6 RoogleDocs object: 
		}
	}


	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-RoogleDocs-reauth"></a>}}
	
	\subsection{Method \code{RoogleDocs$reauth()}}{
		Re-authenticate roogledocs library

Re-authenticate the service deleting the existing OAuth tokens may be helpful if there is some problem. 

Generally this is only be needed if  
application permission updates are needed in which case the directory can be manually deleted anyway,
or if you want to switch google user without using a different tokenDirectory.
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
J$RoogleDocs$reauth(tokenDirectory)
# this method is also exposed as a package function:
roogledocs::reauth(tokenDirectory)
	  		}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\describe{
				\item{tokenDirectory the place to store authentication tokens. This should not be checked into version control. - (defaulting to `.tokenDirectory()`)}{ - (java expects a RCharacter)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			R6 RoogleDocs object: a new RoogleDocs instance without an active document
		}
	}


	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-RoogleDocs-docById"></a>}}
	
	\subsection{Method \code{RoogleDocs$docById()}}{
		Get a document by id or sharing link.
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
J$RoogleDocs$docById(shareUrlOrDocId, tokenDirectory, disabled)
# this method is also exposed as a package function:
roogledocs::doc_by_id(shareUrlOrDocId, tokenDirectory, disabled)
	  		}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\describe{
				\item{shareUrlOrDocId the url from clicking a share button in google docs or an id from searchForDocuments() method}{ - (java expects a RCharacter)}
				\item{tokenDirectory the place to store authentication tokens. This should not be checked into version control. - (defaulting to `.tokenDirectory()`)}{ - (java expects a RCharacter)}
				\item{disabled a flag to switch roogledocs off (on a document by document basis, for testing or development. This can be set globally with `options('roogledocs.disabled'=TRUE)` - (defaulting to `getOption('roogledocs.disabled',FALSE)`)}{ - (java expects a RLogical)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			R6 RoogleDocs object: itself - a fluent method
		}
	}


	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-RoogleDocs-docByName"></a>}}
	
	\subsection{Method \code{RoogleDocs$docByName()}}{
		Get a document by name or create a blank document if missing.
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
J$RoogleDocs$docByName(title, tokenDirectory, disabled)
# this method is also exposed as a package function:
roogledocs::doc_by_name(title, tokenDirectory, disabled)
	  		}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\describe{
				\item{title a document title. If there is an exact match in google drive then that document will be used}{ - (java expects a RCharacter)}
				\item{tokenDirectory the place to store authentication tokens. This should not be checked into version control. - (defaulting to `.tokenDirectory()`)}{ - (java expects a RCharacter)}
				\item{disabled a flag to switch roogledocs off (on a document by document basis, for testing or development. This can be set globally with `options('roogledocs.disabled'=TRUE)` - (defaulting to `getOption('roogledocs.disabled',FALSE)`)}{ - (java expects a RLogical)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			R6 RoogleDocs object: itself - a fluent method
		}
	}


	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-RoogleDocs-docFromTemplate"></a>}}
	
	\subsection{Method \code{RoogleDocs$docFromTemplate()}}{
		Get a document by name or create one from a template if missing.
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
J$RoogleDocs$docFromTemplate(title, templateUri, tokenDirectory, disabled)
# this method is also exposed as a package function:
roogledocs::doc_from_template(title, templateUri, tokenDirectory, disabled)
	  		}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\describe{
				\item{title a document title. If there is an exact match in google drive then that document will be used
otherwise a new one will be created.}{ - (java expects a RCharacter)}
				\item{templateUri the share link (or document id) of a template google document}{ - (java expects a RCharacter)}
				\item{tokenDirectory the place to store authentication tokens. This should not be checked into version control. - (defaulting to `.tokenDirectory()`)}{ - (java expects a RCharacter)}
				\item{disabled a flag to switch roogledocs off (on a document by document basis, for testing or development. This can be set globally with `options('roogledocs.disabled'=TRUE)` - (defaulting to `getOption('roogledocs.disabled',FALSE)`)}{ - (java expects a RLogical)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			R6 RoogleDocs object: itself - a fluent method
		}
	}


	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-RoogleDocs-searchForDocuments"></a>}}
	
	\subsection{Method \code{RoogleDocs$searchForDocuments()}}{
		Search for documents with the given title
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
J$RoogleDocs$searchForDocuments(titleMatch, tokenDirectory)
# this method is also exposed as a package function:
roogledocs::search_for_documents(titleMatch, tokenDirectory)
	  		}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\describe{
				\item{titleMatch a string to be searched for as an approximate match. All results will be retrieved with document ids.}{ - (java expects a RCharacter)}
				\item{tokenDirectory the place to store authentication tokens. This should not be checked into version control. - (defaulting to `.tokenDirectory()`)}{ - (java expects a RCharacter)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			RDataframe: a dataframe containing id and name columns
		}
	}


	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-RoogleDocs-deleteDocument"></a>}}
	
	\subsection{Method \code{RoogleDocs$deleteDocument()}}{
		Deletes a google document by name.
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
J$RoogleDocs$deleteDocument(docName, areYouSure, tokenDirectory, disabled)
# this method is also exposed as a package function:
roogledocs::delete_document(docName, areYouSure, tokenDirectory, disabled)
	  		}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\describe{
				\item{docName - the name of a document to delete. must be an exact and unique match.}{ - (java expects a RCharacter)}
				\item{areYouSure - a boolean check. - (defaulting to `utils::askYesNo(paste0('Are you sure ...`)}{ - (java expects a RLogical)}
				\item{tokenDirectory - (defaulting to `.tokenDirectory()`)}{ - (java expects a RCharacter)}
				\item{disabled - (defaulting to `getOption('roogledocs.disabled',FALSE)`)}{ - (java expects a RLogical)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			void: nothing, called for side efffects
		}
	}


	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-RoogleDocs-citationStyles"></a>}}
	
	\subsection{Method \code{RoogleDocs$citationStyles()}}{
		List the supported citation styles
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
J$RoogleDocs$citationStyles()
# this method is also exposed as a package function:
roogledocs::citation_styles()
	  		}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\itemize{
				\item none
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			RCharacterVector: a vector of csl style names
		}
	}


	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-RoogleSlides-new"></a>}}
	
	\subsection{Method \code{RoogleSlides$new()}}{
		Create a RoogleSlides object for managing the interaction.
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
J$RoogleSlides$new(tokenDirectory, disabled)
	  		}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\describe{
				\item{tokenDirectory the place to store authentication tokens. This should not be checked into version control. - (defaulting to `.tokenDirectory()`)}{ - (java expects a RCharacter)}
				\item{disabled a flag to switch roogledocs off (on a document by document basis, for testing or development. This can be set globally with `options('roogledocs.disabled'=TRUE)` - (defaulting to `getOption('roogledocs.disabled',FALSE)`)}{ - (java expects a RLogical)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			R6 RoogleSlides object: 
		}
	}


	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-RoogleSlides-slidesById"></a>}}
	
	\subsection{Method \code{RoogleSlides$slidesById()}}{
		Get a document by id or sharing link.
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
J$RoogleSlides$slidesById(shareUrlOrDocId, tokenDirectory, disabled)
# this method is also exposed as a package function:
roogledocs::slides_by_id(shareUrlOrDocId, tokenDirectory, disabled)
	  		}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\describe{
				\item{shareUrlOrDocId the url from clicking a share button in google slides or an id from searchForDocuments() method}{ - (java expects a RCharacter)}
				\item{tokenDirectory the place to store authentication tokens. This should not be checked into version control. - (defaulting to `.tokenDirectory()`)}{ - (java expects a RCharacter)}
				\item{disabled a flag to switch roogledocs off (on a document by document basis, for testing or development. This can be set globally with `options('roogledocs.disabled'=TRUE)` - (defaulting to `getOption('roogledocs.disabled',FALSE)`)}{ - (java expects a RLogical)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			R6 RoogleSlides object: itself - a fluent method
		}
	}


	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-RoogleSlides-slidesByName"></a>}}
	
	\subsection{Method \code{RoogleSlides$slidesByName()}}{
		Get a document by name or create a blank document if missing.
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
J$RoogleSlides$slidesByName(title, tokenDirectory, disabled)
# this method is also exposed as a package function:
roogledocs::slides_by_name(title, tokenDirectory, disabled)
	  		}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\describe{
				\item{title a document title. If there is an exact match in google drive then that document will be used}{ - (java expects a RCharacter)}
				\item{tokenDirectory the place to store authentication tokens. This should not be checked into version control. - (defaulting to `.tokenDirectory()`)}{ - (java expects a RCharacter)}
				\item{disabled a flag to switch roogledocs off (on a document by document basis, for testing or development. This can be set globally with `options('roogledocs.disabled'=TRUE)` - (defaulting to `getOption('roogledocs.disabled',FALSE)`)}{ - (java expects a RLogical)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			R6 RoogleSlides object: itself - a fluent method
		}
	}


	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-RoogleSlides-slidesFromTemplate"></a>}}
	
	\subsection{Method \code{RoogleSlides$slidesFromTemplate()}}{
		Get a document by name or create one from a template if missing.
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
J$RoogleSlides$slidesFromTemplate(title, templateUri, tokenDirectory, disabled)
# this method is also exposed as a package function:
roogledocs::slides_from_template(title, templateUri, tokenDirectory, disabled)
	  		}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\describe{
				\item{title a document title. If there is an exact match in google drive then that document will be used
otherwise a new one will be created.}{ - (java expects a RCharacter)}
				\item{templateUri the share link (or document id) of a template google document}{ - (java expects a RCharacter)}
				\item{tokenDirectory the place to store authentication tokens. This should not be checked into version control. - (defaulting to `.tokenDirectory()`)}{ - (java expects a RCharacter)}
				\item{disabled a flag to switch roogledocs off (on a document by document basis, for testing or development. This can be set globally with `options('roogledocs.disabled'=TRUE)` - (defaulting to `getOption('roogledocs.disabled',FALSE)`)}{ - (java expects a RLogical)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			R6 RoogleSlides object: itself - a fluent method
		}
	}


	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-RoogleSlides-searchForSlides"></a>}}
	
	\subsection{Method \code{RoogleSlides$searchForSlides()}}{
		Search for documents with the given title
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
J$RoogleSlides$searchForSlides(titleMatch, tokenDirectory)
# this method is also exposed as a package function:
roogledocs::search_for_slides(titleMatch, tokenDirectory)
	  		}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\describe{
				\item{titleMatch a string to be searched for as an approximate match. All results will be retrieved with document ids.}{ - (java expects a RCharacter)}
				\item{tokenDirectory the place to store authentication tokens. This should not be checked into version control. - (defaulting to `.tokenDirectory()`)}{ - (java expects a RCharacter)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			RDataframe: a dataframe containing id and name columns
		}
	}


	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-RoogleSlides-deleteSlides"></a>}}
	
	\subsection{Method \code{RoogleSlides$deleteSlides()}}{
		Deletes a google slides by name.
	
		\subsection{Usage}{
			\if{html}{\out{<div class="r">}}
			\preformatted{
J = roogledocs::JavaApi$get()
J$RoogleSlides$deleteSlides(docName, areYouSure, tokenDirectory, disabled)
# this method is also exposed as a package function:
roogledocs::delete_slides(docName, areYouSure, tokenDirectory, disabled)
	  		}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\describe{
				\item{docName - the name of a document to delete. must be an exact and unique match.}{ - (java expects a RCharacter)}
				\item{areYouSure - a boolean check. - (defaulting to `utils::askYesNo(paste0('Are you sure ...`)}{ - (java expects a RLogical)}
				\item{tokenDirectory - (defaulting to `.tokenDirectory()`)}{ - (java expects a RCharacter)}
				\item{disabled - (defaulting to `getOption('roogledocs.disabled',FALSE)`)}{ - (java expects a RLogical)}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			void: nothing, called for side efffects
		}
	}



}