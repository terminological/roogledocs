# Tidy up completed async operations

Remove all processed and cancelled aync operations from the status list.
This will potentially free up some system resources

## Usage

``` r
.background_tidy_up()
```

## Value

a maybe empty dataframe with \`id\` and \`status\` columns
