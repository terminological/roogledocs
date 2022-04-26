#' Convert a square display table format to a long format suitable for applying as a sequence of formatting operations in a google doc or as a ggplot. Currently only huxtables are supported.
#' Flextables look very similar. Only a limited subset of formatting features is implemented.
#'
#' @param table 
#'
#' @return a format that is considered valid for roogledocs::RoogleDocs$updateTable()
#' @export
#'
#' @examples
as.long_format_table = function(table) {
  UseMethod("as.long_format_table", table)
}

as.long_format_table.huxtable = function(table) {
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
    fontName = attr(table,"font") %>% as.vector() %>% tidyr::replace_na("Roboto"),
    fontSize = attr(table,"font_size") %>% as.vector() %>% tidyr::replace_na(8),
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
  
  return(tidy2)  
}

.remove_spans = function(tidy) {
  # assumes a row,col,rowSpan,colSpan dataframe, with rowSpan and colSpan only defined in the top left cell of a merged cell
  # now we have to get rid of merged cells
  spans = tidy %>% select(row,col,rowSpan,colSpan) %>% 
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
  
  tidy %>% anti_join(dups, by=c("row","col"))
}





