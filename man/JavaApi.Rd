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
Programmatically substitute images, data and tables into a google doc. R library to perform limited interactions with google docs (and maybe one day slides) in R via the Java API library. The purpose being to support google docs as a platform for interactive development and documentation of data analysis in R for scientific publication, although it is not limited to this purpose. The workflow supported is a parallel documentation and analysis where a team of people are working collaboratively on documentation, whilst at the same time analysis is being performed and results updated repeatedly as a result of new data. In this environment updating numeric results, tabular data and figures in word documents manually becomes annoying. With roogledocs you can automate this a bit like a RMarkdown document, but with the added benefit that the content can be updated independently of the analysis, by the wider team.

Version: 0.0.3.9999

Generated: 2022-07-08T22:54:10.676
}

\author{\email{rob.challen@bristol.ac.uk}}

\keyword{java api}

\section{Static methods and constructors}{
	\itemize{
		\item \code{JavaApi$get()}
		\item \href{#method-RoogleDocs-new}{\code{J$RoogleDocs$new(tokenDirectory, disabled)}}
		\item \href{#method-RoogleDocs-docById}{\code{J$RoogleDocs$docById(shareUrlOrDocId, tokenDirectory, disabled)}}
		\item \href{#method-RoogleDocs-docByName}{\code{J$RoogleDocs$docByName(title, tokenDirectory, disabled)}}
		\item \href{#method-RoogleDocs-docFromTemplate}{\code{J$RoogleDocs$docFromTemplate(title, templateUri, tokenDirectory, disabled)}}
		\item \href{#method-RoogleDocs-searchForDocuments}{\code{J$RoogleDocs$searchForDocuments(titleMatch, tokenDirectory)}}
		\item \href{#method-RoogleDocs-deleteDocument}{\code{J$RoogleDocs$deleteDocument(docName, areYouSure, tokenDirectory, disabled)}}
	}
	

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
				\itemize{
					\item{tokenDirectory the place to store authentication tokens. This should not be checked into version control. - (defaulting to .tokenDirectory())}{ - (java expects a String)}
					\item{disabled a flag to switch roogledocs off (on a document by document basis, for testing or development. This can be set globally with `options('roogledocs.disabled'=TRUE)` - (defaulting to getOption('roogledocs.disabled',FALSE))}{ - (java expects a boolean)}
				}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			R6 RoogleDocs object: 
		}
	}

	\if{html}{\out{<hr>}}
	\if{html}{\out{<a id="method-RoogleDocs-docById"></a>}}
	
	\subsection{Method \code{RoogleDocs$docById()}}{
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
			\describe{
				\itemize{
					\item{shareUrlOrDocId the url from clicking a share button in google docs or an id from searchForDocuments() method}{ - (java expects a String)}
					\item{tokenDirectory the place to store authentication tokens. This should not be checked into version control. - (defaulting to .tokenDirectory())}{ - (java expects a String)}
					\item{disabled a flag to switch roogledocs off (on a document by document basis, for testing or development. This can be set globally with `options('roogledocs.disabled'=TRUE)` - (defaulting to getOption('roogledocs.disabled',FALSE))}{ - (java expects a boolean)}
				}
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
			\describe{
				\itemize{
					\item{title a document title. If there is an exact match in google drive then that document will be used}{ - (java expects a String)}
					\item{tokenDirectory the place to store authentication tokens. This should not be checked into version control. - (defaulting to .tokenDirectory())}{ - (java expects a String)}
					\item{disabled a flag to switch roogledocs off (on a document by document basis, for testing or development. This can be set globally with `options('roogledocs.disabled'=TRUE)` - (defaulting to getOption('roogledocs.disabled',FALSE))}{ - (java expects a boolean)}
				}
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
			\describe{
				\itemize{
					\item{title a document title. If there is an exact match in google drive then that document will be used
otherwise a new one will be created.}{ - (java expects a String)}
					\item{templateUri the share link (or document id) of a template google document}{ - (java expects a String)}
					\item{tokenDirectory the place to store authentication tokens. This should not be checked into version control. - (defaulting to .tokenDirectory())}{ - (java expects a String)}
					\item{disabled a flag to switch roogledocs off (on a document by document basis, for testing or development. This can be set globally with `options('roogledocs.disabled'=TRUE)` - (defaulting to getOption('roogledocs.disabled',FALSE))}{ - (java expects a boolean)}
				}
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
	  		}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\describe{
				\itemize{
					\item{titleMatch a string to be searched for as an approximate match. All results will be retrieved with document ids.}{ - (java expects a String)}
					\item{tokenDirectory the place to store authentication tokens. This should not be checked into version control. - (defaulting to .tokenDirectory())}{ - (java expects a String)}
				}
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
	  		}
			\if{html}{\out{</div>}}
		}
	
		\subsection{Arguments}{
			\if{html}{\out{<div class="arguments">}}
			\describe{
				\itemize{
					\item{docName - the name of a document to delete. must be an exact and unique match.}{ - (java expects a String)}
					\item{areYouSure - a boolean check. - (defaulting to utils::askYesNo(paste0('Are you sure you want to delete ',docName),FALSE))}{ - (java expects a boolean)}
					\item{tokenDirectory - (defaulting to .tokenDirectory())}{ - (java expects a String)}
					\item{disabled - (defaulting to getOption('roogledocs.disabled',FALSE))}{ - (java expects a boolean)}
				}
			}
			\if{html}{\out{</div>}}
		}

		\subsection{Returns}{
			void: nothing, called for side efffects
		}
	}

}