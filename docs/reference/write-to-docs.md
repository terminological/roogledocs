# Simpler interface to \`roogledocs\`

A simple way to write to a document, supplementary or slides, without
having to manage individual document lifecycle, or learn a lot of . This
is designed for the most usual use case of writing a single manuscript
with possibly associated slides and or supplementary materials in the
context of one R session. It allows a document to be selected and
written to iteratively.

## Usage

``` r
project_doc(supp = NA)

write_value(value, name = deparse(substitute(value)), supp = NA)

write_plot(p, name = deparse(substitute(p)), size = std_size$half, supp = NA)

write_png(png, name = deparse(substitute(png)), supp = NA)

write_table(
  t,
  name = deparse(substitute(t)),
  size = std_size$full,
  colwidths = rep(1, ncol(t)),
  supp = NA
)

update_citations(bibfile, style, supp = NA)
```

## Arguments

- supp:

  use a supplementary? either a number e.g. 1, 2, 3... or "slides"

- value:

  A formatted text snippet.

- name:

  the tag name to replace it should be in the google doc as \`{{tag}}\`

- p:

  A ggplot object.

- size:

  a \`std_size\` object with width and height in inches.

- png:

  A path to a png image

- t:

  a huxtable table

- colwidths:

  an set of relative column widths, which will be made relative to the
  total table width.

- bibfile:

  the location of the bibtex file

- style:

  a CSL (see \`roogledocs::citation_styles()\`)

## Value

the written value invisibly, or the \`roogledocs\` document object for a
project document.

## Details

This set of functions can be used even if \`roogledocs\` is not
installed. In this case the functions will short circuit and do nothing.
\`roogledocs\` may also be globally disabled.

These functions can be imported into a project with
\`devtools::use_standalone("terminological/roogledocs")\` to allow
optional simple \`roogledocs\` use.

## Functions

- `project_doc()`: Get a document in the current project.

- `write_value()`: Write a value to the current manuscript

- `write_plot()`: Write a plot to a document in the current project

- `write_png()`: Write a png to a document in the current project

- `write_table()`: Write a table to a document in the current project

- `update_citations()`: update the citations in the current manuscript

## See also

\[with_project()\]

## Examples

``` r
if (FALSE) {
  options(roogledocs.disabled=FALSE)
  with_project("Test project")
  tag1 = "some value"
  write_value(tag1)
  figure1 = ggplot2::ggplot()
  write_plot(figure1)
  table1 = huxtable::as.hux()
  write_table(table1)
  bibfile = "/path/to/my/bibfile"
  update_citations(bibfile,"vancouver")
}
```
