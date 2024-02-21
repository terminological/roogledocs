# ---
# repo: terminological/roogledocs
# file: standalone-roogledocs.R
# last-updated: 2024-02-20
# license: https://unlicense.org
# imports: 
#   - rlang (>= 1.1.1)
#   - fs
# ---
  
# This is a shim to allow a consistent interface to roogledocs that does not
# depend on the package actually being installed.

# It is imported into the any project using a 
# pkgtools::use_standalone("terminological/roogledocs", "roogledocs")
# This will expose some project level functions that can then be used with
# optional `roogledocs` dependency (if it installed then it will be used)

## Google document functions ----

#' Get a named `roogledoc` document
#' 
#' This function connects to a `roogledoc` document by name defaulting to
#' `getOption("roogledoc.doc_name")`. If `roogledocs` is not installed it
#' returns a shim that silently ignores `roogledocs` calls. This is setup so a
#' script with `roogledocs` calls in it can be executed on a machine without
#' `roogledocs` installed.
#'
#'  @param name a googledocs document name (will be created if it does not exist)
#'
#' @return a `RoogleDocs` R6 object or a shim that ignores `roogledocs` calls
#' @noRd
roogledoc = function(name = getOption("roogledoc.doc_name",NULL)) {
  if (is.null(name)) stop("roogledocs name must be given (or `option(roogledoc.doc_name = '...')` must be set)")
  .roogledoc_impl(name)()
}

# cached doc
.roogledoc_impl = function(name) {
  docs = list()
  function() {
    
    if (!rlang::is_installed("roogledocs")) {
      rlang::warn("`roogledocs` is not installed. All `roogledocs` calls will be ignored.",.frequency = "once", .frequency_id = "roogledocs installed")
      return(structure(list(), class="roogledocs_shim"))
    }
    
    if (is.null(docs[[name]])) {
      doc = roogledocs::doc_by_name(name)
      docs[[name]] <<- doc
    }
    
    return(docs[[name]])
  }
}


#' Get a named `roogledoc` presentation
#' 
#' This function connects to a `roogledoc` presentation instance by name which
#' defaults to `getOption("roogledoc.slides_name")`. If `roogledocs` is not
#' installed it returns a shim that silently ignores `roogledocs` calls. This is
#' setup so a script with `roogledocs` calls in it can be executed on a machine
#' without `roogledocs` installed.
#'
#' @param name a googledocs presentation name (will be created if it does not exist)
#'
#' @return a `RoogleSlides` R6 object or a shim that ignores `roogledocs` calls
#' @noRd
roogleslides = function(name = getOption("roogledoc.slides_name",NULL)) {
  if (is.null(name)) stop("roogledocs name must be given (or `option(roogledoc.doc_name = '...')` must be set)")
  .roogleslides_impl(name)()
}

# cached slides
.roogleslides_impl = function(name) {
  docs = list()
  function() {
    
    if (!rlang::is_installed("roogledocs")) {
      rlang::warn("`roogledocs` is not installed. All `roogledocs` calls will be ignored.",.frequency = "once", .frequency_id = "roogledocs installed")
      return(structure(list(), class="roogledocs_shim"))
    }
    
    if (is.null(docs[[name]])) {
      doc = roogledocs::slides_by_name(name)
      docs[[name]] <<- doc
    }
    
    return(docs[[name]])
  }
}


# a list accessor that intercepts any function calls and returns the first argument
# or NULL if none.
`$.roogledocs_shim` = function(x, y) {
  return(function(...) {
    tmp = rlang::list2(...)
    if (length(tmp) == 0) return(invisible(NULL))
    return(tmp[[1]])
  })
}


