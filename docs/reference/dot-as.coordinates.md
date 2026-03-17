# Maven coordinates

Maven coordinates

## Usage

``` r
.as.coordinates(groupId, artifactId, version, ...)
```

## Arguments

- groupId:

  the maven \`groupId\`

- artifactId:

  the maven \`artifactId\`

- version:

  the maven version

- ...:

  other parameters ignored apart from \`packaging\` (one of
  \`jar\`,\`war\`,\`pom\` or \`ejb\`) and \`classifier\` (one of
  \`tests\`, \`client\`, \`sources\`, \`javadoc\`,
  \`jar-with-dependencies\`, or \`src\`)

## Value

a coordinates object containing the Maven artifact coordinates
