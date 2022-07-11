# roogledocs: 0.1.0

* Feature complete r google docs library for testing
     - find or create document by name
     - find or create document from template
     - open doc from share url
     - delete doc
     - list documents
     - upsert PNG image by index
     - upsert table by index
     - replace double brace tags with test
     - append formatted text to document
     - CI and testing setup.
     
* TODO prior to general usefulness: 
     - Verify app with google
     - investigate rJava hang if authentication fails / is not completed. (RService$initialiseService()) Some sort fo timeout required
     - add testers here:  https://console.cloud.google.com/apis/credentials/consent?project=roogledocs
