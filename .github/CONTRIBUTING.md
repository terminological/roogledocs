# Contributing

When contributing to this repository, please first discuss the change you wish
to make via issue, email, or any other method with the owners of this repository
before making a change.

## Development notes

This library uses an R-code generation process `r6-generator-maven-plugin` and `rJava`. 

For Google API OAuth a client id is required. This repository does not include the client_secrets.json file for this but this
must be included as a symbolic link when project is cloned and BEFORE running `mvn install`. Installation will not fail without
it but the resulting library will not work. This is only of relevance for development. For use it is all bundled

To do this you must get a client_secret.json file from:

- <https://developers.Google.com/identity/protocols/oauth2>
- <https://console.cloud.Google.com/apis/credentials?project=your_project_name>

You are requesting a OAuth client id; type "desktop application". The client_secret.json file should then be saved outside of
the github repository, and symbolic linked to the `src/main/resources` directory. Symbolic links are implicitly ignored by github, 
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
devtools::load_all("~/Git/roogledocs",force = TRUE)
```

N.b. When you make changes to the java part of the library there are sometimes some caching issues. Full restart of R maybe 
required, and rebuild all data. Tweaking the library whilst doing a complex analysis is generally not a good idea (from experience).

### Client secrets

Are the OAuth client secrets really necessarily secret?

Generic answer for Google secrets:

<https://stackoverflow.com/questions/62315535/are-there-any-security-concerns-with-sharing-the-client-secrets-of-a-Google-api>

But difference between OAuth client id secret and types discussed on [this page](https://developers.Google.com/identity/protocols/oauth2)
which states:

"Installed applications: The Google OAuth 2.0 endpoint supports applications that are installed on devices such as computers, mobile devices, 
and tablets. When you create a client ID through the Google API Console, specify that this is an Installed application, then select Android, 
Chrome app, iOS, Universal Windows Platform (UWP), or Desktop app as the application type.

The process results in a client ID and, in some cases, a client secret, which you embed in the source code of your application. (In this context, 
the client secret is obviously not treated as a secret.)"

Which suggests this is not an issue in this situation. In either event storing the raw client_secrets.json file in github seems lika a bad idea. However 
as github is the distribution method of the library it is not actually possible to totally avoid all security issues.

