# Using roogledocs for presentations

### Initialising the library

Prior to doing any analysis we may have some form of template. This
might be a document skeleton or report template. It can contain empty
tables, place-holder images, and double-brace tags, all of which can be
replaced by calculated content from R. To do this we initialise the
`roogledocs` library.

``` r

# There is a global flag to disable `roogledocs` in case you want to develop and
# test offline.
options('roogledocs.disabled'=FALSE)

# roogledocs stores an authentication token on your local hard drive.
options("roogledocs.tokenDirectory"="~/.roogledocs-test")
```

Most of the time you will be creating or updating a single document. For
this vignettes sake it is useful to be able to delete previous versions.
The point of `roogledocs` though is actually to work with a continuously
updated document and therefore deleting documents is usually not what
you want to do. Likewise for this vignette it is useful to get a copy of
the Google doc as a PDF from R. This may not be that useful in real
life. The main function here though is the `findOrCloneTemplate()`
method which lets you find a Google doc by name, or clone a template
document if you can’t find it. There are also equivalent methods to find
or create blank documents, or just find Google docs by name or sharing
URL if they already exist.

``` r
roogledocs::delete_slides("roogleslides-demo",areYouSure = TRUE)
## Initialised roogledocs
## Initialising RoogleDocs. Local token directory: /home/terminological/.roogledocs-test
## Deleting file: roogleslides-demo
```

Sometimes (particularly if the `roogledocs` library has been updated) we
get a `TokenResponseException`, saying the token has been expired or
revoked. In this event explicitly re-authenticating the library can be
done though a call to
[`roogledocs::reauth()`](../../reference/reauth.md).

``` r
roogledocs::reauth()
```

Once authentication is working getting a

``` r
doc = roogledocs::slides_from_template(
  "roogleslides-demo",
  "https://docs.google.com/presentation/d/18jqzzDI1zBruO3Rc0RlzX_rxhsDiOuKbTM4vnwuimn4/edit?usp=sharing")
## Created new presentation with title: roogleslides-demo
fs::dir_create(here::here("docs/articles/web-only"))
doc$saveAsPdf(here::here("docs/articles/web-only/example-template-slides.pdf"))
## Created new presentation with title: tmp_copy_for_pdf_6e341429-74e9-4768-a553-79bdebbdcb7d
## Deleting file: tmp_copy_for_pdf_6e341429-74e9-4768-a553-79bdebbdcb7d
```

Running the chunk above should authenticate you and grab a publicly
shared template I created, and make a copy of it in your Google drive
under the name “roogledocs-demo”. [The document template can be seen
here](example-template-slides.pdf), or as the [original Google
doc](https://docs.Google.com/document/d/1XnrBgBJFz7jEMYtw3o3YKbOuMdWvUkzIul4hb2B-SC4/edit?usp=sharing).

## Tabular data

Inserting tables in this document is done by index. There is already a
blank table 1 in the document. At the moment we support only `huxtable`
tables and plain data-frames. The following chunk creates a sample
`huxtable` from the diamonds data set, applies some formatting and
replaces the content of table one in the template with this data. The
formatting is preserved more or less. There is only support for basic
text formatting, borders (black solid only at present), background
colour, and alignment. The table will respect column widths as a
relative measure and the command takes a overall table width parameter.
Layout will then depend on the content. Custom row heights are not
supported.

``` r
hux = diamonds %>% mutate(colorCat = ifelse(color <= "G", "D-G","G-J")) %>% group_by(cut,colorCat) %>% summarise(
  `Size (mean + sd)` = sprintf("%1.2f \u00B1 %1.2f",mean(carat),sd(carat)),
  `Cost (mean + sd)` = sprintf("%1.0f \u00B1 %1.0f",mean(price),sd(price))
) %>% huxtable::as_hux() %>%
  huxtable::theme_article() %>% 
  huxtable::set_all_padding(value = 0) %>%
  huxtable::merge_repeated_rows()
## `summarise()` has regrouped the output.
## ℹ Summaries were computed grouped by cut and colorCat.
## ℹ Output is grouped by cut.
## ℹ Use `summarise(.groups = "drop_last")` to silence this message.
## ℹ Use `summarise(.by = c(cut, colorCat))` for per-operation grouping
##   (`?dplyr::dplyr_by`) instead.

hux %>% roogledocs::as.long_format_table() %>% doc$updateTaggedTable(tagName = "diamonds-tag" )
## Autotext replacing: {{diamonds-tag}} with table

hux
```

| cut       | colorCat | Size (mean + sd) | Cost (mean + sd) |
|-----------|----------|------------------|------------------|
| Fair      | D-G      | 0.93 ± 0.43      | 3997 ± 3312      |
|           | G-J      | 1.24 ± 0.58      | 4972 ± 3873      |
| Good      | D-G      | 0.78 ± 0.39      | 3620 ± 3380      |
|           | G-J      | 1.00 ± 0.54      | 4610 ± 4194      |
| Very Good | D-G      | 0.72 ± 0.39      | 3587 ± 3666      |
|           | G-J      | 1.00 ± 0.54      | 4873 ± 4358      |
| Premium   | D-G      | 0.79 ± 0.44      | 4060 ± 4044      |
|           | G-J      | 1.11 ± 0.59      | 5633 ± 4732      |
| Ideal     | D-G      | 0.63 ± 0.36      | 3151 ± 3562      |
|           | G-J      | 0.88 ± 0.53      | 4233 ± 4273      |

## Updating figures

A similar process exists for figures. We need to have the figure as a
PNG image on the local computer as a result of, for example, a ggplot.
Once a local PNG is available, it is temporarily uploaded to your Google
drive, added to the document and then temporary drive file deleted. In
this example we update figure 1 replacing the `{{figure_1}}` tag in the
original Google doc with the image:

``` r

dim = doc$slideDimensions()

g = ggplot(diamonds, aes(x=carat,y=price, colour=color))+geom_point()
figure_1 = roogledocs::ggplot_to_png(g, width=dim$width, height=dim$height)

# If the first parameter is passed as a variable and no tag is given, 
# as in this example the variable name is used as the tag and the image is 
# inserted into the document at the location of the tag:

doc$updateTaggedImage(figure_1)
## Uploading: file14b243c5c5e11.png; with type: image/png
## Autotext replacing: {{figure_1}} with image: https://lh3.googleusercontent.com/drive-storage/AJQWtBPfGYCIq8GCtuQxGdhPjxvw3RnS4RyiEDiyTIl2oEUNTN4fz40SSkEf4PwnJ5ubp57KhoA-XHyKZTArbKv6nLBgU3pVarKzG4p0zh2sbpGH8cUV=s16383
## Figure figure_1 updated
## Deleting file: temp_2f1c2370-e9aa-47e9-98e7-0b35e54d6d13.png

# This is equivalent to:
# doc$updateTaggedImage(figure_1, tagName = "figure_1")
```

Updating a second figure can happen in the same way. The dimensions of
the image in the Google slides… resized to fit the container. If the
container is rotated the result will be rotated.

``` r
g2 = ggplot(diamonds, aes(x=cut,y=price, fill=cut))+
  geom_violin(draw_quantiles = c(0.95,0.5,0.05))+
  scale_fill_brewer()
## Warning: The `draw_quantiles` argument of `geom_violin()` is deprecated as of ggplot2
## 4.0.0.
## ℹ Please use the `quantiles.linetype` argument instead.
## This warning is displayed once per session.
## Call `lifecycle::last_lifecycle_warnings()` to see where this warning was
## generated.
figure_2 = roogledocs::ggplot_to_png(g2, width=5, height=4)

doc$updateTaggedImage(figure_2)
## Uploading: file14b24456b0740.png; with type: image/png
## Autotext replacing: {{figure_2}} with image: https://lh3.googleusercontent.com/drive-storage/AJQWtBO6edQVGFebQ9RdWmK7ApuqvV3vT9PzynccU4PlSIwgH43S24L4YdHiW32IvV7Wd4mPakKkLRyn9YTCz2hzyVStvfwkL_StzM8VPVDSgFq0s_BXIA=s16383
## Figure figure_2 updated
## Deleting file: temp_90e0e66b-0b88-4814-b832-50cf51d062b0.png
```

## Updating tagged text

If you want to update small textual results - e.g. results in the
abstract of a paper (similar to RMarkdown in-line chunks) you can place
a double-brace tag into the Google doc and replace this with text
generated in R. The result is inserted in the Google doc as a URL link
so that further changes or updates in code can find the tagged text.
Links like this can be moved around the document, or copied and pasted
without losing the tag. You can get a list of the tags present in a
document like this:

``` r
doc$tagsDefined()
```

| tag                          | count |
|------------------------------|------:|
| date                         |     1 |
| cite:challen2019             |     1 |
| diamonds-tag                 |     1 |
| cite:r6gen                   |     1 |
| cite:challen2019;challen2021 |     1 |
| figure_2                     |     1 |
| figure_1                     |     1 |
| cite:roogledocs              |     2 |
| diamonds_mean_sd             |     1 |

Here we have 2 tags. The tags can be then set to specific content like
this:

``` r
format(Sys.Date(),"%d/%m/%Y") %>% doc$updateTaggedText(tagName = "date")
## Autotext replacing: {{date}} with 17/03/2026
## Text date updated
diamonds_mean_sd = sprintf("%1.1f \u00B1 %1.1f",mean(diamonds$price),sd(diamonds$price)) 

# if we don't give a specific tag name then the variable name is used:
doc$updateTaggedText(diamonds_mean_sd)
## Autotext replacing: {{diamonds_mean_sd}} with 3932.8 ± 3989.4
## Text diamonds_mean_sd updated
```

## New content

Appending new content is also possible either as a simple styled text
string, with consistent formatting, or as a continuous block (or blocks)
with different styles, as specified in a data-frame. At the moment this
is only possible at the end of the document and is really designed if a
document is being generated completely from scratch.

``` r

content = tibble::tribble(
  ~label, ~link, ~fontName, ~fontFace,
  "Roogledocs", "https://terminological.github.io/roogledocs/r-library/docs/", "Courier New", "plain",
  " is also able to add text at the end of the document with complex formatting. ", NA, NA, "plain",
  "Supporting fonts and font face formatting such as ",  NA, NA, "plain",
  "bold, ", NA, NA, "bold",
  "italic ", NA, NA, "italic",
  "and underlined", NA, NA, "underlined",
  " amongst other things.", NA, NA, "plain"
  )

doc$appendFormattedSlide("Adding new content", content)

content
```

| label | link | fontName | fontFace |
|----|----|----|----|
| Roogledocs | https://terminological.github.io/roogledocs/r-library/docs/ | Courier New | plain |
| is also able to add text at the end of the document with complex formatting. |  |  | plain |
| Supporting fonts and font face formatting such as |  |  | plain |
| bold, |  |  | bold |
| italic |  |  | italic |
| and underlined |  |  | underlined |
| amongst other things. |  |  | plain |

It could be possible to combine writing new content and updating tagged
text in the same script to programmatically generate replacement
content. Likewise this could be used for captions of tables and figures
when they are added. When you write new content you can write in
double-brace tags and these can then be updated at a later stage for
example.

## Citations

Assuming we have a bibliography file we can provide this to `roogledocs`
and it will insert citations.

``` r

doc$updateCitations(here::here("vignettes/web-only/test.bib"), citationStyle = "journal-of-infection")
```

Finally we can write out the new document to a PDF, mostly so we can see
what we have done. When we write out a document any `roogledocs` links
are removed:

``` r
doc$saveAsPdf(here::here("docs/articles/web-only/slides-after-update.pdf"))
## Created new presentation with title: tmp_copy_for_pdf_1a7703b8-b043-4bfb-98dd-009f03db25ec
## Deleting file: tmp_copy_for_pdf_1a7703b8-b043-4bfb-98dd-009f03db25ec
```

After the analysis has run we have a new version of the Google slides
which should [look like this](slides-after-update.pdf).
