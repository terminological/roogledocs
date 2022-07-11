

.tokenDirectory = function() {
  normalizePath(getOption("roogledocs.tokenDirectory",default="~/.roogledocs"), mustWork = FALSE)
}