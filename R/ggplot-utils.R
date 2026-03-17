#' Plot a ggplot object to a temporary file as a png
#'
#' In `roogledocs` a path to a local png is needed as input
#' to tagged image commands. This is a simple wrapper function to
#' save a png using `ragg` with DPI fixed at 300 to a temporary directory
#' and return the file path.
#'
#' @param plot a ggplot or patchwork object
#' @param width the width of the output in inches
#' @param height the height of the output in inches
#'
#' @return a full path to a png in a temp directory
#' @export
#'
#' @importFrom ragg agg_png
#' @examples
#' if (FALSE) {
#'   doc = roogledocs::slides_by_name("roogledocs-demo")
#'   g = ggplot(diamonds, aes(x=carat,y=price, colour=color))+geom_point()
#'   figure_1 = ggplot_to_png(g, width=4, height=4)
#'   doc$updateTaggedImage(figure_1)
#'
#'   # same as:
#'   # doc$updateTaggedImage(ggplot_to_png(plot,4,4), tagName = "figure_1")
#'
#' }
#'
ggplot_to_png = function(plot, width, height) {
  temp = tempfile(fileext = ".png")
  ggplot2::ggsave(
    temp,
    plot,
    width = width,
    height = height,
    bg = "transparent",
    device = ragg::agg_png,
    dpi = 300
  )
  return(temp)
}

#' Standard image and paper sizes
#'
#' The width and height of images to fit scientific publication standards.
#'
#' @docType data
#' @name std_size
#' @format A list with width and height in inches
#' @export
#' @concept output
std_size = list(
  A4 = list(width = 8.25, height = 11.75, rot = 0),
  A5 = list(width = 5 + 7 / 8, height = 8.25, rot = 0),
  full = list(width = 5.9, height = 8, rot = 0),
  landscape = list(width = 9.75, height = 5.9, rot = 0),
  half = list(width = 5.9, height = 4, rot = 0),
  third = list(width = 5.9, height = 3, rot = 0),
  two_third = list(width = 5.9, height = 6, rot = 0),
  quarter = list(width = 5.9, height = 2, rot = 0),
  quarter_portrait = list(width = 3, height = 4, rot = 0),
  sixth = list(width = 3, height = 3, rot = 0),
  slide = list(width = 12, height = 6, rot = 0)
)
