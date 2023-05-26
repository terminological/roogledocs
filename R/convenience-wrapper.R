

ggplot_to_docs = function(p, docName, figureIndex, size = std_size$third, out = outputter(tempdir(), datedFile = FALSE), doc = roogledocs::doc_by_name(docName)) {
  
  tmp = fs::path(tempdir(), sprintf("%s - figure %d", docName, figureIndex))
  withExt = function(extn) {fs::path_ext_set(tmp,extn)}
  
  raster = p %>% .ggplot_save_tmp(tmp, withExt, formats=c("png","pdf"))
  
  doc$updateFigure(withExt("png"),figureIndex = figureIndex)
  doc$uploadSupplementaryFiles(withExt("pdf"), overwrite = TRUE)
  
  if (.is_knitting() || .is_running_in_chunk()) {
    if (.is_html_output()) {
      return(knitr::asis_output(sprintf("<img src='%s'></img>", base64enc::dataURI(file = withExt("png"), mime = "image/png"))))
    } else {
      return(knitr::include_graphics(path = withExt("png"),auto_pdf = TRUE, dpi=300))
    }
  } else {
    grid::grid.raster(raster)
  }
  
}


huxtable_to_docs = function(t, docName, tableIndex, ...,  doc = roogledocs::doc_by_name(docName)) {
  doc$updateTable(t %>% roogledocs::as.long_format_table(), tableIndex = tableIndex, ...)
  #TODO: render to HTML before displaying?
  t
}


#' Standard image and paper sizes
#'
#' The width and height of images to fit scientific publication standards.
#'
#' @docType data
#' @name std_size
#' @format A list with width and height in inches
#' @export
std_size = list(
  A4 = list(width=8.25,height=11.75,rot=0),
  A5 = list(width=5+7/8,height=8.25,rot=0),
  full =  list(width=5.9,height=8,rot=0),
  landscape =  list(width=9.75,height=5.9,rot=0),
  half =  list(width=5.9,height=4,rot=0),
  third =  list(width=5.9,height=3,rot=0),
  two_third = list(width=5.9,height=6,rot=0),
  quarter = list(width=5.9,height=2,rot=0),
  quarter_portrait = list(width=3,height=4,rot=0),
  sixth = list(width=3,height=3,rot=0),
  slide = list(width=12,height=6,rot=0)
)


# TRUE if the whole document is being knitted.
# FALSE if running in chunk in RStudio, or not interactive, or
.is_knitting = function() {
  isTRUE(getOption("knitr.in.progress"))
}

# TRUE is being knitted OR running in chunk in RStudio
# FALSE if not interactive or interactive but in console in RStudio
.is_running_in_chunk = function() {
  .is_knitting() |
    isTRUE(try(
      rstudioapi::getActiveDocumentContext()$id != "#console" &
        rstudioapi::getActiveDocumentContext()$path %>% stringr::str_ends("Rmd")
    ))
}

.is_html_output = knitr::is_html_output

.ggplot_save_tmp = function(plot, withExt, size = std_size$half) {
  
  maxWidth = size$width
  maxHeight = size$height
  aspectRatio=maxWidth/maxHeight
  
  # plot comes with an aspect ratio which is expressed as height/width
  # this is generally true if the coords_fixed has been used.
  plotAr = tryCatch({plot$coordinates$ratio}, error = function(e) NULL)
  if(!is.null(plotAr)) {
    aspectRatio = 1/plotAr
    if (maxWidth/aspectRatio > maxHeight) maxWidth = maxHeight*aspectRatio
    if (maxHeight*aspectRatio > maxWidth) maxHeight = maxWidth/aspectRatio
  }
  
  dir = fs::path_dir(filename)
  if (!dir.exists(dir)) dir.create(dir,recursive = TRUE)
  
  filename = fs::path_ext_remove(filename)
  
  
  # save the pdf
  if (!capabilities()["cairo"] ) {
      
    ggplot2::ggsave(
      withExt("pdf"),
      plot, width = min(maxWidth,maxHeight*aspectRatio), height = min(maxHeight,maxWidth/aspectRatio), bg = "transparent");
    try(
      grDevices::embedFonts(withExt("pdf")),
      silent=TRUE
    );
      
  } else {
      
    ggplot2::ggsave(
      withExt("pdf"),
      plot, width = min(maxWidth,maxHeight*aspectRatio), height = min(maxHeight,maxWidth/aspectRatio), bg = "transparent",device = cairo_pdf);
    try(
      grDevices::embedFonts(withExt("pdf")),
      silent=TRUE
    );
  }

  # render the PNG from the PDF  
  raster = pdftools::pdf_render_page(withExt("pdf"),page = 1,dpi=300)
  raster %>% png::writePNG(withExt("png"))
  
  return(raster)
  
}