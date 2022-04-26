# roogledocs

R library to perform limited interactions with google docs and (one day) slides
in R via the Java API library.

This does not include the client_secrets.json file which must be included as a symbolic link when project cloned: 

e.g. 

```
cd ~/Git/roogledocs/src/main
mkdir resources
cd resources
ln -s ~/Dropbox/roogledocs/client_secret.json
```

BEFORE running mvn install.


client_secrets files are from:

https://developers.google.com/identity/protocols/oauth2

https://console.cloud.google.com/apis/credentials?project=roogledocs

Type OAuth client id; type "desktop app"

Are these secrets really necessarily secret?

Generic answer for:

https://stackoverflow.com/questions/62315535/are-there-any-security-concerns-with-sharing-the-client-secrets-of-a-google-api

But difference between OAuth client id secret and types discussed here:

On the page (https://developers.google.com/identity/protocols/oauth2):

Installed applications
The Google OAuth 2.0 endpoint supports applications that are installed on devices such as computers, mobile devices, and tablets. When you create a client ID through the Google API Console, specify that this is an Installed application, then select Android, Chrome app, iOS, Universal Windows Platform (UWP), or Desktop app as the application type.

The process results in a client ID and, in some cases, a client secret, which you embed in the source code of your application. (In this context, the client secret is obviously not treated as a secret.)


  