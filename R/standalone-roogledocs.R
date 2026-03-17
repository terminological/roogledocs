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
roogledoc = function(name = getOption("roogledoc.doc_name", NULL)) {
  if (is.null(name)) {
    stop(
      "roogledocs name must be given (or `option(roogledoc.doc_name = '...')` must be set)"
    )
  }
  .roogledoc_impl(name)
}

# cached doc
.roogledoc_impl = (function() {
  docs = list()
  return(
    function(name) {
      if (!rlang::is_installed("roogledocs")) {
        rlang::warn(
          "`roogledocs` is not installed. All `roogledocs` calls will be ignored.",
          .frequency = "once",
          .frequency_id = "roogledocs installed"
        )
        return(structure(list(), class = "roogledocs_shim"))
      } else {
        if (is.null(docs[[name]])) {
          doc = roogledocs::doc_by_name(name)
          docs[[name]] <<- doc
        }
        return(docs[[name]])
      }
    }
  )
})()


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
roogleslides = function(name = getOption("roogledoc.slides_name", NULL)) {
  if (is.null(name)) {
    stop(
      "roogledocs name must be given (or `option(roogledoc.doc_name = '...')` must be set)"
    )
  }
  .roogleslides_impl(name)
}

# cached slides
.roogleslides_impl = (function() {
  docs = list()
  function(name) {
    if (!rlang::is_installed("roogledocs")) {
      rlang::warn(
        "`roogledocs` is not installed. All `roogledocs` calls will be ignored.",
        .frequency = "once",
        .frequency_id = "roogledocs installed"
      )
      return(structure(list(), class = "roogledocs_shim"))
    } else {
      if (is.null(docs[[name]])) {
        doc = roogledocs::slides_by_name(name)
        docs[[name]] <<- doc
      }

      return(docs[[name]])
    }
  }
})()


# a list accessor that intercepts any function calls and returns the first argument
# or NULL if none.
#' @export
`$.roogledocs_shim` = function(x, y) {
  return(function(...) {
    tmp = rlang::list2(...)
    if (length(tmp) == 0) {
      return(invisible(NULL))
    }
    return(tmp[[1]])
  })
}


# Utility functions for quickly writing to a single document ----
# Simply put this lets us do the following:
# 1) define a single google document as the main document "XXX"
# 2) refer to supplementary documents as "XXX - supplementary 1", etc.
# 3) refer to slides as "XXX - slides"
# 4) cache main working document.
# 5) update tagged text in documents or slides using variable name as default tag name
# 6) update tagged ggplot in documents or slides using variable name as default tag name
# 7) update tagged table in documents or slides using variable name as default tag name
# 8) update references using a bib file + style.

# Stateful object holding project / manuscript name and version, and
# cached docs and slides.
.currentdoc = (function() {
  .name = expression(stop("Document name has not been defined", call. = FALSE))
  .version = "v0.01"

  # getter and setter for name
  name = function(name) {
    if (!missing(name)) {
      .name <<- name
    }
    return(eval(.name))
  }

  # getter and setter for version
  version = function(version) {
    if (!missing(version)) {
      .version <<- version
    }
    return(eval(.version))
  }

  # create the document name from name version and addition items
  to_string = function(supp = NA) {
    if (!is.na(supp)) {
      qual = sprintf("supplementary %d", as.integer(supp))
      return(sprintf("%s %s %s", name(), qual, version()))
    }
    return(sprintf("%s %s", name(), version()))
  }

  # create a filename from name version and addition items
  filename = function(supp, name, extn) {
    return(sprintf("%s %s.%s", to_string(supp), name, extn))
  }

  # create a named roogleslides document
  slides = function() {
    return(roogleslides(to_string("slides")))
  }

  # create a named roogledocs (main or supplementary) or roogleslides object
  doc = function(supp = NA) {
    if (is.na(supp)) {
      return(roogledoc(to_string()))
    }
    if (supp == "slides") {
      return(slides())
    }
    return(roogledoc(to_string()))
  }

  # create the object
  return(environment())
})()


#' Set the main project name for a session.
#'
#' A project consists of a main document and optional supplementary files with
#' possible presentation versions. The associated google docs and slides will be
#' named with a versioned title based on the name parameter,
#' e.g. `"<name> <version>"`, `"<name> supplementary 1 <version>"`,
#' `"<name> slides <version>"`...
#'
#' @param name The main project name. Forms part of the googledocs or slides
#'   filename.
#' @param version A version identifier. defaults to `"v0.01"`
#'
#' @returns Nothing. Called for side effects. Does not check that document
#'   exists. If it does not exist it will be created on first use.
#' @keywords internal
#'
#' @examples
#' if (FALSE) with_project("Test project")
with_project = function(name, version = "v0.01") {
  .currentdoc$name(name)
  .currentdoc$version(version)
  .currentdoc$to_string()
}


#' Simpler interface to `roogledocs`
#'
#' A simple way to write to a document, supplementary or slides, without having to
#' manage individual document lifecycle, or learn a lot of . This is designed for the most usual
#' use case of writing a single manuscript with possibly associated slides and or
#' supplementary materials in the context of one R session. It allows a document
#' to be selected and written to iteratively.
#'
#' This set of functions can be used even if `roogledocs` is not installed. In
#' this case the functions will short circuit and do nothing. `roogledocs` may
#' also be globally disabled.
#'
#' These functions can be imported into a project with
#' `devtools::use_standalone("terminological/roogledocs")` to allow optional
#' simple `roogledocs` use.
#'
#' @seealso [with_project()]
#'
#' @param name the tag name to replace it should be in the google doc as `\{\{tag\}\}`
#' @param supp use a supplementary? either a number e.g. 1, 2, 3... or "slides"
#' @param size a `std_size` object with width and height in inches.
#'
#' @returns the written value invisibly, or the `roogledocs` document object
#'   for a project document.
#' @keywords internal
#' @name write-to-docs
#' @examples
#' if (FALSE) {
#'   options(roogledocs.disabled=FALSE)
#'   with_project("Test project")
#'   tag1 = "some value"
#'   write_value(tag1)
#'   figure1 = ggplot2::ggplot()
#'   write_plot(figure1)
#'   table1 = huxtable::as.hux()
#'   write_table(table1)
#'   bibfile = "/path/to/my/bibfile"
#'   update_citations(bibfile,"vancouver")
#' }
NULL

#' @describeIn write-to-docs Get a document in the current project.
project_doc = function(supp = NA) {
  gd = .currentdoc$doc(supp)
  return(gd)
}

#' @describeIn write-to-docs Write a value to the current manuscript
#' @param value A formatted text snippet.
write_value = function(value, name = deparse(substitute(value)), supp = NA) {
  gd = .currentdoc$doc(supp)
  gd$updateTaggedText(as.character(value), name)
  return(invisible(as.character(value)))
}

#' @describeIn write-to-docs Write a plot to a document in the current project
#' @param p A ggplot object.
write_plot = function(
  p,
  name = deparse(substitute(p)),
  size = std_size$half,
  supp = NA
) {
  png = ggplot_to_png(p, size$width, size$height)
  write_png(png, name, supp)
}

#' @describeIn write-to-docs Write a png to a document in the current project
#' @param png A path to a png image
write_png = function(png, name = deparse(substitute(png)), supp = NA) {
  gd = .currentdoc$doc(supp)
  figName = .currentdoc$figure(supp, name, "png")
  png = fs::path_abs(png)
  gd$updateTaggedImage(png, tagName = name, dpi = 300, FALSE)
  return(invisible(png))
}

#' @describeIn write-to-docs Write a table to a document in the current project
#' @param t a huxtable table
#' @param colwidths an set of relative column widths, which will be made relative
#'   to the total table width.
write_table = function(
  t,
  name = deparse(substitute(t)),
  size = std_size$full,
  colwidths = rep(1, ncol(t)),
  supp = NA
) {
  gd = .currentdoc$doc(supp)
  gd$updateTaggedTable(
    t %>% roogledocs::as.long_format_table(fontSize = 8),
    colWidths = colwidths,
    tagName = name,
    tableWidthInches = size$width
  )
  return(invisible(t))
}

#' @describeIn write-to-docs update the citations in the current manuscript
#' @param bibfile the location of the bibtex file
#' @param style a CSL (see `roogledocs::citation_styles()`)
update_citations = function(bibfile, style, supp = NA) {
  bibfile = fs::path_abs(bibfile)
  gd = .currentdoc$doc(supp)
  gd$updateCitations(bibfile, style)
  return(invisible(bibfile))
}
