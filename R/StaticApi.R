# Generated by r6-generator-maven-plugin: do not edit by hand
# This is a collection of the static methods described in the Java API
# and serves as an alternative R centric entry point of the roogledocs generated R library.

# Version: 0.5.0
# Generated: 2024-04-27T13:56:10.582362960
# Contact: rob.challen@bristol.ac.uk

# RoogleDocs class static methods ----


#' reauth: Re-authenticate roogledocs library
#' 
#' Re-authenticate the service deleting the existing OAuth tokens may be
#'   helpful if there is some problem. 
#'   
#'   Generally this is only be needed
#'   if  
#'   application permission updates are needed in which case the
#'   directory can be manually deleted anyway,
#'   or if you want to switch
#'   google user without using a different tokenDirectory.
#' @param tokenDirectory the place to store authentication tokens. This should
#'   not be checked into version control. - (defaulting to
#'   `.tokenDirectory()`) - (java expects a RCharacter)
#' @return R6 RoogleDocs object: 
#' a new RoogleDocs instance without an active document
#' @export
reauth = function(tokenDirectory=.tokenDirectory()) {
	# get the API singleton
	J = JavaApi$get()
	# execute the R6 function call with the same parameters
	out = J$RoogleDocs$reauth(tokenDirectory)
	if(is.null(out)) return(invisible(out))
	return(out)
}


#' docById: Get a document by id or sharing link.
#' 
#' no description
#' @param shareUrlOrDocId the url from clicking a share button in google docs or
#'   an id from searchForDocuments() method - (java expects a RCharacter)
#' @param tokenDirectory the place to store authentication tokens. This should
#'   not be checked into version control. - (defaulting to
#'   `.tokenDirectory()`) - (java expects a RCharacter)
#' @param disabled a flag to switch roogledocs off (on a document by document
#'   basis, for testing or development. This can be set globally with
#'   `options('roogledocs.disabled'=TRUE)` - (defaulting to
#'   `getOption('roogledocs.disabled',FALSE)`) - (java expects a RLogical)
#' @return R6 RoogleDocs object: 
#' itself - a fluent method
#' @export
doc_by_id = function(shareUrlOrDocId, tokenDirectory=.tokenDirectory(), disabled=getOption('roogledocs.disabled',FALSE)) {
	# get the API singleton
	J = JavaApi$get()
	# execute the R6 function call with the same parameters
	out = J$RoogleDocs$docById(shareUrlOrDocId, tokenDirectory, disabled)
	if(is.null(out)) return(invisible(out))
	return(out)
}


#' docByName: Get a document by name or create a blank document if missing.
#' 
#' no description
#' @param title a document title. If there is an exact match in google drive
#'   then that document will be used - (java expects a RCharacter)
#' @param tokenDirectory the place to store authentication tokens. This should
#'   not be checked into version control. - (defaulting to
#'   `.tokenDirectory()`) - (java expects a RCharacter)
#' @param disabled a flag to switch roogledocs off (on a document by document
#'   basis, for testing or development. This can be set globally with
#'   `options('roogledocs.disabled'=TRUE)` - (defaulting to
#'   `getOption('roogledocs.disabled',FALSE)`) - (java expects a RLogical)
#' @return R6 RoogleDocs object: 
#' itself - a fluent method
#' @export
doc_by_name = function(title, tokenDirectory=.tokenDirectory(), disabled=getOption('roogledocs.disabled',FALSE)) {
	# get the API singleton
	J = JavaApi$get()
	# execute the R6 function call with the same parameters
	out = J$RoogleDocs$docByName(title, tokenDirectory, disabled)
	if(is.null(out)) return(invisible(out))
	return(out)
}


#' docFromTemplate: Get a document by name or create one from a template if missing.
#' 
#' no description
#' @param title a document title. If there is an exact match in google drive
#'   then that document will be used
#'   otherwise a new one will be created. - (java expects a RCharacter)
#' @param templateUri the share link (or document id) of a template google
#'   document - (java expects a RCharacter)
#' @param tokenDirectory the place to store authentication tokens. This should
#'   not be checked into version control. - (defaulting to
#'   `.tokenDirectory()`) - (java expects a RCharacter)
#' @param disabled a flag to switch roogledocs off (on a document by document
#'   basis, for testing or development. This can be set globally with
#'   `options('roogledocs.disabled'=TRUE)` - (defaulting to
#'   `getOption('roogledocs.disabled',FALSE)`) - (java expects a RLogical)
#' @return R6 RoogleDocs object: 
#' itself - a fluent method
#' @export
doc_from_template = function(title, templateUri, tokenDirectory=.tokenDirectory(), disabled=getOption('roogledocs.disabled',FALSE)) {
	# get the API singleton
	J = JavaApi$get()
	# execute the R6 function call with the same parameters
	out = J$RoogleDocs$docFromTemplate(title, templateUri, tokenDirectory, disabled)
	if(is.null(out)) return(invisible(out))
	return(out)
}


#' searchForDocuments: Search for documents with the given title
#' 
#' no description
#' @param titleMatch a string to be searched for as an approximate match. All
#'   results will be retrieved with document ids. - (java expects a RCharacter)
#' @param tokenDirectory the place to store authentication tokens. This should
#'   not be checked into version control. - (defaulting to
#'   `.tokenDirectory()`) - (java expects a RCharacter)
#' @return RDataframe: 
#' a dataframe containing id and name columns
#' @export
search_for_documents = function(titleMatch, tokenDirectory=.tokenDirectory()) {
	# get the API singleton
	J = JavaApi$get()
	# execute the R6 function call with the same parameters
	out = J$RoogleDocs$searchForDocuments(titleMatch, tokenDirectory)
	if(is.null(out)) return(invisible(out))
	return(out)
}


#' deleteDocument: Deletes a google document by name.
#' 
#' no description
#' @param docName - the name of a document to delete. must be an exact and
#'   unique match. - (java expects a RCharacter)
#' @param areYouSure - a boolean check. - (defaulting to
#'   `utils::askYesNo(paste0('Are you sure ...`) - (java expects a RLogical)
#' @param tokenDirectory - (defaulting to `.tokenDirectory()`) - (java expects a RCharacter)
#' @param disabled - (defaulting to `getOption('roogledocs.disabled',FALSE)`) - (java expects a RLogical)
#' @return void: 
#' nothing, called for side efffects
#' @export
delete_document = function(docName, areYouSure=utils::askYesNo(paste0('Are you sure you want to delete ',docName),FALSE), tokenDirectory=.tokenDirectory(), disabled=getOption('roogledocs.disabled',FALSE)) {
	# get the API singleton
	J = JavaApi$get()
	# execute the R6 function call with the same parameters
	out = J$RoogleDocs$deleteDocument(docName, areYouSure, tokenDirectory, disabled)
	if(is.null(out)) return(invisible(out))
	return(out)
}


#' citationStyles: List the supported citation styles
#' 
#' no description
#' @return RCharacterVector: 
#' a vector of csl style names
#' @export
citation_styles = function() {
	# get the API singleton
	J = JavaApi$get()
	# execute the R6 function call with the same parameters
	out = J$RoogleDocs$citationStyles()
	if(is.null(out)) return(invisible(out))
	return(out)
}


# RoogleSlides class static methods ----


#' slidesById: Get a document by id or sharing link.
#' 
#' no description
#' @param shareUrlOrDocId the url from clicking a share button in google slides
#'   or an id from searchForDocuments() method - (java expects a RCharacter)
#' @param tokenDirectory the place to store authentication tokens. This should
#'   not be checked into version control. - (defaulting to
#'   `.tokenDirectory()`) - (java expects a RCharacter)
#' @param disabled a flag to switch roogledocs off (on a document by document
#'   basis, for testing or development. This can be set globally with
#'   `options('roogledocs.disabled'=TRUE)` - (defaulting to
#'   `getOption('roogledocs.disabled',FALSE)`) - (java expects a RLogical)
#' @return R6 RoogleSlides object: 
#' itself - a fluent method
#' @export
slides_by_id = function(shareUrlOrDocId, tokenDirectory=.tokenDirectory(), disabled=getOption('roogledocs.disabled',FALSE)) {
	# get the API singleton
	J = JavaApi$get()
	# execute the R6 function call with the same parameters
	out = J$RoogleSlides$slidesById(shareUrlOrDocId, tokenDirectory, disabled)
	if(is.null(out)) return(invisible(out))
	return(out)
}


#' slidesByName: Get a document by name or create a blank document if missing.
#' 
#' no description
#' @param title a document title. If there is an exact match in google drive
#'   then that document will be used - (java expects a RCharacter)
#' @param tokenDirectory the place to store authentication tokens. This should
#'   not be checked into version control. - (defaulting to
#'   `.tokenDirectory()`) - (java expects a RCharacter)
#' @param disabled a flag to switch roogledocs off (on a document by document
#'   basis, for testing or development. This can be set globally with
#'   `options('roogledocs.disabled'=TRUE)` - (defaulting to
#'   `getOption('roogledocs.disabled',FALSE)`) - (java expects a RLogical)
#' @return R6 RoogleSlides object: 
#' itself - a fluent method
#' @export
slides_by_name = function(title, tokenDirectory=.tokenDirectory(), disabled=getOption('roogledocs.disabled',FALSE)) {
	# get the API singleton
	J = JavaApi$get()
	# execute the R6 function call with the same parameters
	out = J$RoogleSlides$slidesByName(title, tokenDirectory, disabled)
	if(is.null(out)) return(invisible(out))
	return(out)
}


#' slidesFromTemplate: Get a document by name or create one from a template if missing.
#' 
#' no description
#' @param title a document title. If there is an exact match in google drive
#'   then that document will be used
#'   otherwise a new one will be created. - (java expects a RCharacter)
#' @param templateUri the share link (or document id) of a template google
#'   document - (java expects a RCharacter)
#' @param tokenDirectory the place to store authentication tokens. This should
#'   not be checked into version control. - (defaulting to
#'   `.tokenDirectory()`) - (java expects a RCharacter)
#' @param disabled a flag to switch roogledocs off (on a document by document
#'   basis, for testing or development. This can be set globally with
#'   `options('roogledocs.disabled'=TRUE)` - (defaulting to
#'   `getOption('roogledocs.disabled',FALSE)`) - (java expects a RLogical)
#' @return R6 RoogleSlides object: 
#' itself - a fluent method
#' @export
slides_from_template = function(title, templateUri, tokenDirectory=.tokenDirectory(), disabled=getOption('roogledocs.disabled',FALSE)) {
	# get the API singleton
	J = JavaApi$get()
	# execute the R6 function call with the same parameters
	out = J$RoogleSlides$slidesFromTemplate(title, templateUri, tokenDirectory, disabled)
	if(is.null(out)) return(invisible(out))
	return(out)
}


#' searchForSlides: Search for documents with the given title
#' 
#' no description
#' @param titleMatch a string to be searched for as an approximate match. All
#'   results will be retrieved with document ids. - (java expects a RCharacter)
#' @param tokenDirectory the place to store authentication tokens. This should
#'   not be checked into version control. - (defaulting to
#'   `.tokenDirectory()`) - (java expects a RCharacter)
#' @return RDataframe: 
#' a dataframe containing id and name columns
#' @export
search_for_slides = function(titleMatch, tokenDirectory=.tokenDirectory()) {
	# get the API singleton
	J = JavaApi$get()
	# execute the R6 function call with the same parameters
	out = J$RoogleSlides$searchForSlides(titleMatch, tokenDirectory)
	if(is.null(out)) return(invisible(out))
	return(out)
}


#' deleteSlides: Deletes a google slides by name.
#' 
#' no description
#' @param docName - the name of a document to delete. must be an exact and
#'   unique match. - (java expects a RCharacter)
#' @param areYouSure - a boolean check. - (defaulting to
#'   `utils::askYesNo(paste0('Are you sure ...`) - (java expects a RLogical)
#' @param tokenDirectory - (defaulting to `.tokenDirectory()`) - (java expects a RCharacter)
#' @param disabled - (defaulting to `getOption('roogledocs.disabled',FALSE)`) - (java expects a RLogical)
#' @return void: 
#' nothing, called for side efffects
#' @export
delete_slides = function(docName, areYouSure=utils::askYesNo(paste0('Are you sure you want to delete ',docName),FALSE), tokenDirectory=.tokenDirectory(), disabled=getOption('roogledocs.disabled',FALSE)) {
	# get the API singleton
	J = JavaApi$get()
	# execute the R6 function call with the same parameters
	out = J$RoogleSlides$deleteSlides(docName, areYouSure, tokenDirectory, disabled)
	if(is.null(out)) return(invisible(out))
	return(out)
}





