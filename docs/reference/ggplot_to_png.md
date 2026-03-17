# Plot a ggplot object to a temporary file as a png

In \`roogledocs\` a path to a local png is needed as input to tagged
image commands. This is a simple wrapper function to save a png using
\`ragg\` with DPI fixed at 300 to a temporary directory and return the
file path.

## Usage

``` r
ggplot_to_png(plot, width, height)
```

## Arguments

- plot:

  a ggplot or patchwork object

- width:

  the width of the output in inches

- height:

  the height of the output in inches

## Value

a full path to a png in a temp directory

## Examples

``` r
if (FALSE) {
  doc = roogledocs::slides_by_name("roogledocs-demo")
  g = ggplot(diamonds, aes(x=carat,y=price, colour=color))+geom_point()
  figure_1 = ggplot_to_png(g, width=4, height=4)
  doc$updateTaggedImage(figure_1)

  # same as:
  # doc$updateTaggedImage(ggplot_to_png(plot,4,4), tagName = "figure_1")

}
```
