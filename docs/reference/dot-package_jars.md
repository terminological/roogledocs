# Find location of some or all of the jars in a particular package.

Find location of some or all of the jars in a particular package.

## Usage

``` r
.package_jars(
  package_name,
  types = c("all", "thin-jar", "fat-jar", "shaded", "src")
)
```

## Arguments

- package_name:

  the R package name

- types:

  the jar types to look for in the package: one of
  \`all\`,\`thin-jar\`,\`fat-jar\`,\`shaded\`,\`src\`

## Value

a vector of paths to jar files in the package
