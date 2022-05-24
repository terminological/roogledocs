# roogledocs

[![R-CMD-check](https://github.com/terminological/roogledocs/workflows/R-CMD-check/badge.svg)](https://github.com/terminological/roogledocs/actions)

[![DOI](https://zenodo.org/badge/475030092.svg)](https://zenodo.org/badge/latestdoi/475030092)


R library to perform limited interactions with google docs (and maybe one day slides)
in R via the Java API library. The purpose being to support google docs as a 
platform for interactive development and documentation of data analysis in R for scientific
publication, although it is not limited to this purpose. The workflow supported is a parallel documentation and analysis
where a team of people are working collaboratively on documentation, whilst at the same time analysis 
is being performed and results updated repeatedly as a result of new data. In this environment updating
numeric results, tabular data and figures in word documents manually becomes annoying. With roogledocs
you can automate this a bit like a RMarkdown document, but with the added benefit that the content 
can be updated independently of the analysis, by the wider team. 

## Installation instructions

Roogledocs is not on cran yet. Installation from this repo can be done as follows:

```R
devtools::install_github("terminological/roogledocs")
```

## R library documentation

[The R package site is here](https://terminological.github.io/roogledocs/docs/)

## Simple usage

```R
J = roogledocs::JavaApi$get()
paper = J$RoogleDocs$new()
paper$findOrCreateDocument("my-new-nature-paper")
paper$updateFigure("/full/path/to/figure-1.png", figureIndex = 1, dpi = 300)
```

Which will authenticate you, create a blank document and insert an image for figure 1 at the end of the document.

After some editing of the document and further analysis you are ready for a new version of the figure:

```{R}
paper$updateFigure("/full/path/to/figure-1-v2.png", figureIndex = 1, dpi = 300)
```

Which updates the figure leaving the rest of the google doc content in place. Clearly this can be combined with ggplot 
to produce a seamless scripted data pipeline, one output of which is a google doc. This can be executed in RMarkdown, in 
which case the markdown can be a notebook documenting the code and methodology, and the google doc is the write up.   

## Development notes

This library uses an R-code generation process `r6-generator-maven-plugin` and `rJava`. 

For Google api OAuth a client id is required. This repository does not include the client_secrets.json file for this but this
must be included as a symbolic link when project is cloned and BEFORE running `mvn install`. Installation will not fail without
it but the resulting library will not work. 

To do this you must get a client_secret.json file from:

- <https://developers.google.com/identity/protocols/oauth2>
- <https://console.cloud.google.com/apis/credentials?project=your_project_name>

You are requesting a OAuth client id; type "desktop application". The client_secret.json file should then be saved outside of
the github repository, and linked to some the `src/main/resources` directory. Symbolic links are implictly ignored by github, 
but it should also be explicitly excluded by `.gitignore`:

e.g. in my case:

```
cd ~/Git/roogledocs/src/main
mkdir resources
cd resources
ln -s ~/Dropbox/roogledocs/client_secret.json
cd ~/Git/roogledocs
mvn install
```

and then in R:


```R
devtools::load_all("~/Git/roogledocs/r-library",force = TRUE)
```

N.b. When you make changes to the java part of the library there are sometimes some caching issues. Full restart of R maybe 
required, and rebuild all data. Tweaking the library whilst doing a complex analysis is generally not a good idea (from experience).

### Client secrets

Are the OAuth client secrets really necessarily secret?

Generic answer for google secrets:

<https://stackoverflow.com/questions/62315535/are-there-any-security-concerns-with-sharing-the-client-secrets-of-a-google-api>

But difference between OAuth client id secret and types discussed on [this page](https://developers.google.com/identity/protocols/oauth2)
which states:

"Installed applications: The Google OAuth 2.0 endpoint supports applications that are installed on devices such as computers, mobile devices, 
and tablets. When you create a client ID through the Google API Console, specify that this is an Installed application, then select Android, 
Chrome app, iOS, Universal Windows Platform (UWP), or Desktop app as the application type.

The process results in a client ID and, in some cases, a client secret, which you embed in the source code of your application. (In this context, 
the client secret is obviously not treated as a secret.)"

Which suggests this is not an issue in this situation. In either event storing the raw client_secrets.json file in github seems lika a bad idea. However 
as github is the distribution method of the library it is not actually possible to totally avoid all security issues.

