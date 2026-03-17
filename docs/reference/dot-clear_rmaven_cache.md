# Clear out the \`rmaven\` cache

Deletes all content in the \`rmaven\` cache. This should not be
necessary, but never say never, and if there is really a problem with
the cache, then deleting it may be the best thing. This will wait for
confirmation from the user. If running unattended the
\`options("rmaven.allow.cache.delete"=TRUE)\` must be set for the action
to occur, otherwise it will generate a warning and do nothing.

## Usage

``` r
.clear_rmaven_cache()
```

## Value

nothing, called for side effects
