% Generated by roxygen2: do not edit by hand
% Please edit documentation in R/aa_maven.R
\name{.copy_artifact}
\alias{.copy_artifact}
\title{Copy an artifact from a repository to a local directory}
\usage{
.copy_artifact(
  groupId = NULL,
  artifactId = NULL,
  version = NULL,
  ...,
  coordinates = NULL,
  artifact = NULL,
  outputDirectory = .working_dir(artifact),
  repoUrl = .default_repos(),
  nocache = FALSE,
  verbose = c("normal", "quiet", "debug")
)
}
\arguments{
\item{groupId}{optional, the maven `groupId`,}

\item{artifactId}{optional, the maven `artifactId`,}

\item{version}{optional, the maven version,}

\item{...}{other maven coordinates such as classifier or packaging}

\item{coordinates}{optional, coordinates as a coordinates object,}

\item{artifact}{optional, coordinates as an artifact string
`groupId:artifactId:version[:packaging[:classifier]]` string}

\item{outputDirectory}{optional path, defaults to the `rmaven` cache
directory}

\item{repoUrl}{the URLs of the repositories to check (defaults to maven
central, `Sonatype snaphots` and `jitpack`)}

\item{nocache}{normally artifacts are only fetched if required, `nocache`
forces fetching}

\item{verbose}{how much output from maven, one of "normal", "quiet", "debug"}
}
\value{
the output of the system2 call. 0 on success.
}
\description{
This essentially runs a `maven-dependency-plugin:copy` goal to copy a JAR
file from (usually) a remote repository to a local directory. The directory
is under the users control but defaults to the `.m2` repository.
}
\keyword{internal}
