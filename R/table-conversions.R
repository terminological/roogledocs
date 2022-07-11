# NOTE TO SELF: THIS FILE IS HARD LINKED TO FROM SEVERAL PROJECTS AND MUST BE STAND ALONE.

#' Convert a table to long format
#'
#' Converts a square display table format to a long format suitable for applying as a sequence of formatting operations
#' in a google doc or as a ggplot. Currently only plain dataframes and huxtables are supported but flextables look very doable.
#' Only a limited subset of formatting features is implemented at present as supported by roogledocs. The output format
#' is a simple dataframe with the following columns:
#'
#' - Character: label - non blank text (a single space is OK but not an empty string)
#' - Integer: row - must be an integer, 1-based from top left
#' - Integer: col - must be an integer, 1-based from top left
#' - Integer: rowSpan - must be an integer, minimum value 1
#' - Integer: colSpan - must be an integer, minimum value 1
#' - Character: fontName - font name as seen in font drop down of google docs e.g "Roboto","Arial","Times New Roman", unrecognised values will be displayed as Arial
#' - Character: fontFace - one of "bold", "bold.italic", "italic", "plain"
#' - Numeric: fontSize - in points
#' - Character: fillColour - as a hex string e.g. "#aaaaaa". N.b. British English spelling (sorry)
#' - Numeric: leftBorderWeight - border weight in points - minimum size that appears in google docs is 0.5
#' - Numeric: rightBorderWeight
#' - Numeric: topBorderWeight
#' - Numeric: bottomBorderWeight
#' - Character: alignment - one of "START","CENTER","END"
#' - Character: valignment - one of "TOP","MIDDLE","BOTTOM"
#'
#' It also has an attribute `colWidths` which is a vector the same length as the width of the
#' table containing the relative widths of the columns. The overall table width is
#' decided on rendering.
#'
#' So not supported at the moment are border line types, border colours, control of padding, row height control,
#' alignment on a decimal point, complex content / markup in cells.
#'
#' @param table the input table (e.g. a huxtable)
#' @param ... passed onto subclass methods
#'
#' @return a format that is considered valid for roogledocs::RoogleDocs$updateTable()
#' @export
as.long_format_table = function(table, ...) {
  UseMethod("as.long_format_table", table)
}

# NOTE TO SELF: THIS FILE IS HARD LINKED TO FROM SEVERAL PROJECTS AND MUST BE STAND ALONE.

#' @method as.long_format_table long_format_table
#' @export
as.long_format_table.long_format_table = function(table,...) {return(table)}

#' @method as.long_format_table data.frame
#' @export
as.long_format_table.data.frame = function(table, fontName = "Roboto", fontSize = 8, alignment = "START", valignment = "TOP", colWidths = NULL, ...) {

  tidy = table %>%
    as_tibble() %>%
    mutate(across(everything(), as.character)) %>%
    mutate(row = (row_number()+1) %>% as.integer()) %>%
    pivot_longer(cols = -row, values_to="label") %>%
    mutate(col = rep(1:ncol(table), nrow(table)) %>% as.integer()) %>%
    mutate(label = ifelse(label==""," ",label)) %>%
    select(-name) %>% mutate(
      topBorderWeight = ifelse(row == min(row),0.5,0),
      bottomBorderWeight = ifelse(row == max(row),0.5,0),
      fontFace = "plain"
    )

  header = tibble(
      label = colnames(table),
      col = 1:ncol(table),
      row = 1,
      topBorderWeight = 0.5,
      bottomBorderWeight = 0.5,
      fontFace = "bold"
    )

  tidy = bind_rows(header,tidy)

  tidy = tidy %>% mutate(
    rowSpan = as.integer(1),
    colSpan = as.integer(1),
    leftBorderWeight = 0,
    rightBorderWeight = 0,
    fontName = fontName,
    fontSize = as.numeric(fontSize),
    fillColour = "#FFFFFF",
    alignment = alignment,
    valignment = valignment
  )

  class(tidy) = c("long_format_table",class(tidy))
  if (is.null(colWidths)) colWidths = fit_col_widths(tidy)
  attr(tidy,"colWidths") = colWidths

  return(tidy)
}

# NOTE TO SELF: THIS FILE IS HARD LINKED TO FROM SEVERAL PROJECTS AND MUST BE STAND ALONE.

#' @method as.long_format_table huxtable
#' @export
as.long_format_table.huxtable = function(table, fontName = "Roboto", fontSize = 8, ...) {
  # a huxtable is fully described including merged cells:
  tidy = table %>%
    as_tibble() %>%
    mutate(row = row_number() %>% as.integer()) %>%
    pivot_longer(cols = -row, values_to="label") %>%
    mutate(col = rep(1:ncol(table), nrow(table)) %>% as.integer()) %>%
    mutate(label = ifelse(label==""," ",label)) %>%
    select(-name)

  tidy2 = tidy %>% arrange(col,row) %>% mutate(
    rowSpan = attr(table,"rowspan") %>% as.vector() %>% as.integer(),
    colSpan = attr(table,"colspan") %>% as.vector() %>% as.integer(),
    alignment = case_when(
      attr(table,"align") %>% as.vector() == "left" ~ "START",
      attr(table,"align") %>% as.vector() == "center" ~ "CENTER",
      attr(table,"align") %>% as.vector() == "right" ~ "END",
      TRUE ~ "END"
    ),
    valignment = case_when(
      attr(table,"valign") %>% as.vector() == "top" ~ "TOP",
      attr(table,"valign") %>% as.vector() == "middle" ~ "MIDDLE",
      attr(table,"valign") %>% as.vector() == "bottom" ~ "BOTTOM",
      TRUE ~ "TOP"
    ),
    fontName = attr(table,"font") %>% as.vector() %>% tidyr::replace_na(fontName),
    fontSize = attr(table,"font_size") %>% as.vector() %>% tidyr::replace_na(fontSize),
    fillColour = attr(table,"background_color") %>% as.vector() %>% tidyr::replace_na("#FFFFFF"),
    bold = attr(table,"bold") %>% as.vector() %>% tidyr::replace_na(FALSE),
    italic = attr(table,"italic") %>% as.vector() %>% tidyr::replace_na(FALSE),
    fontFace = case_when(
      bold & italic ~ "bold.italic",
      bold ~ "bold",
      italic ~ "italic",
      TRUE ~ "plain"
    ),
    topBorderWeight = head(attr(table,"tb_borders")$thickness,-1) %>% as.vector() %>% tidyr::replace_na(0),
    bottomBorderWeight = attr(table,"tb_borders")$thickness[-1,] %>% as.vector() %>% tidyr::replace_na(0),
    leftBorderWeight = t(head(t(attr(table,"lr_borders")$thickness),-1)) %>% as.vector() %>% tidyr::replace_na(0),
    rightBorderWeight = attr(table,"lr_borders")$thickness[,-1] %>% as.vector() %>% tidyr::replace_na(0)
  ) %>% select(-bold,-italic)

  tidy2 = tidy2 %>% .remove_spans()
  attr(tidy2,"colWidths") = table %>% huxtable::col_width() %>% tidyr::replace_na(1) %>% unname()
  class(tidy2) = c("long_format_table",class(tidy2))
  return(tidy2)
}

# NOTE TO SELF: THIS FILE IS HARD LINKED TO FROM SEVERAL PROJECTS AND MUST BE STAND ALONE.

.remove_spans = function(tidy) {
  # assumes a row,col,rowSpan,colSpan dataframe, with rowSpan and colSpan only defined in the top left cell of a merged cell
  # now we have to get rid of merged cells
  spans = tidy %>% select(row,col,rowSpan,colSpan,bottomBorderWeight,rightBorderWeight) %>%
    mutate(
      # make sure rowspan and colspan are valid.
      rowSpan = ifelse(rowSpan<1,1,rowSpan) %>% tidyr::replace_na(1),
      colSpan = ifelse(colSpan<1,1,colSpan) %>% tidyr::replace_na(1)
    ) %>%
    mutate(minCol = col, maxCol = col+colSpan-1) %>%
    # first of all look at each row and find the min and max of each column span
    group_by(row) %>% arrange(col) %>% mutate(maxCol = cummax(maxCol)) %>% group_by(row,maxCol) %>% mutate(minCol = min(col)) %>%
    # then for each set of columns find the most number of rows that spans
    # this is because huxtable only stores the rowSpan and colSpan in the top left corner of a rowspan
    group_by(row,minCol,maxCol) %>% mutate(rowSpan = max(rowSpan)) %>%
    # calculate the end of each row span
    mutate(minRow = row, maxRow = row+rowSpan-1) %>%
    # then for each column, find the min and max of each row span
    group_by(col) %>% arrange(row) %>% mutate(maxRow = cummax(maxRow)) %>% group_by(col,maxRow) %>% mutate(minRow = min(row)) %>%
    # then for each set of rows find the most number of cols that span
    group_by(col,minRow,maxRow) %>% mutate(colSpan = max(colSpan))

  dups = spans %>% arrange(col,row) %>% filter((row > minRow & row <= maxRow) | (col>minCol & col<=maxCol))

  # these cells will have a bit of info about bottom and right borders at the end of a span.
  spanEnds = dups %>% filter(row == maxRow & col == maxCol) %>% select(row = minRow, col = minCol, bottomBorderWeight,rightBorderWeight)

  tidy %>% anti_join(dups, by=c("row","col")) %>% left_join(spanEnds, by=c("row","col"), suffix=c("",".new")) %>%
    mutate(
      bottomBorderWeight = ifelse(is.na(bottomBorderWeight.new), bottomBorderWeight, bottomBorderWeight.new),
      rightBorderWeight = ifelse(is.na(rightBorderWeight.new), rightBorderWeight, rightBorderWeight.new)
    ) %>% select(-ends_with(".new"))
}

# NOTE TO SELF: THIS FILE IS HARD LINKED TO FROM SEVERAL PROJECTS AND MUST BE STAND ALONE.

