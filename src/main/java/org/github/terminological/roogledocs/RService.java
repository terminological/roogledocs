package org.github.terminological.roogledocs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.github.terminological.roogledocs.datatypes.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.DocsScopes;
import com.google.api.services.docs.v1.model.Document;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;

public class RService {
    
	
	// Application name
    private static final String APPLICATION_NAME = "R Google Docs API";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    // Global instance of the scopes required by this application (If modifying these scopes, delete your previously saved tokens/ folder.)
    private static final List<String> SCOPES = Arrays.asList(
    		DocsScopes.DOCUMENTS,
    		DocsScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "/client_secret.json";
    private Logger log = LoggerFactory.getLogger(RService.class);
    
    
    // Configuration
    private Path tokenDirectory = null;
    private Docs service = null;
    private Drive driveService = null;
    
    public RService() throws IOException, GeneralSecurityException {
    	this(Paths.get(System.getProperty("user.home"), ".roogledocs"));
    }
    public RService(String tokenDirectory) throws IOException, GeneralSecurityException {
    	this(Paths.get(tokenDirectory));
    }
    
    // Constructor
    public RService(Path tokenDirectory) throws IOException, GeneralSecurityException {
    	Iterator<Path> it = tokenDirectory.iterator();
    	while (it.hasNext()) {
    		Path p = it.next();
    		if (Files.isDirectory(p.resolve(".git"))) throw new IOException("The token directory path is in a GIT subdirectory. This is a terrible idea from a security point of view."); 
    	};
    	this.tokenDirectory = tokenDirectory;
    	log.info("Initialising RoogleDocs. Local token directory: "+tokenDirectory);
    	initialiseService();
    }
    
    Docs getDocs()  {
    	return service;
    }
    
    Drive getDrive()  {
    	return driveService;
    }
    
    // do auth stuff when RoogleDocs first called.
    private void initialiseService() throws IOException, GeneralSecurityException {
    	final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        
        // Load client secrets.
        InputStream in = RService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(tokenDirectory.toFile()))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        //returns an authorized Credential object.
        
        service = new Docs.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
        driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
        		.setApplicationName(APPLICATION_NAME)
        		.build();
    }

    public static final String MIME_DOCS =  "application/vnd.google-apps.document";
    public static final String MIME_PNG =  "image/png";
    
    public List<Tuple<String,String>> search(String documentName, String mimeType) throws IOException {
    	return search(documentName, mimeType, false);
    }
    
    public List<Tuple<String,String>> search(String documentName, String mimeType, boolean exact) throws IOException {
    	// https://developers.google.com/drive/api/guides/search-files
    	List<Tuple<String,String>> out = new ArrayList<>();
    	String pageToken = null;
    	String qry = 
    			"name "+(exact ? "=" : "contains")+" '"+documentName+"'" +
    			(mimeType!=null ? " and mimeType = '"+mimeType+"'" : "")+
    			" and trashed = false";
    	do {
    	  FileList result = getDrive().files().list()
    	      .setQ(qry)
    	      .setSpaces("drive")
    	      .setFields("nextPageToken, files(id, name)")
    	      .setPageToken(pageToken)
    	      .execute();
    	  for (File file : result.getFiles()) {
    	    out.add(Tuple.create(file.getId(), file.getName()));
    	  }
    	  pageToken = result.getNextPageToken();
    	} while (pageToken != null);
    	return out;
    }
    
    public RDocument getOrCreate(String documentName) throws IOException {
    	List<Tuple<String, String>> tmp = search(documentName, MIME_DOCS, true);
    	if (tmp.size() > 0) {
    		String docId = tmp.get(0).getFirst(); 
    		return new RDocument(docId,this);
    	}
    	Document doc = new Document().setTitle(documentName);
    	doc = getDocs().documents().create(doc).execute();
    	log.info("Created new document with title: " + doc.getTitle());
    	return new RDocument(doc.getDocumentId(),this);
    }
    
    public RDocument getDocument(String docId) throws IOException {
    	
    	// https://docs.google.com/document/d/1woDbkXAkf6RbvtjGlXMPBOl7zvDCviAvAWDZGNerYCk/edit?usp=sharing
    	if (docId.startsWith("http")) {
    		Pattern p = Pattern.compile("/([^/]+)/[^/]+$");
    		Matcher m = p.matcher(docId);
    		if (!m.find()) throw new IOException("URL format docId cannot be parsed: "+docId);
    		docId = docId.substring(m.start(1),m.end(1));
    	}
    	
    	File f = driveService.files().get(docId).execute();
    	if (f != null) {
    		if (f.getMimeType().equals(MIME_DOCS)) {
    			return new RDocument(docId,this);
    		} else {
    			throw new IOException("File: "+f.getName()+" is not a google doc");
    		}
    	}
    	throw new IOException("No document for: "+docId);
    }
    
    public String upload(Path file) throws IOException {
    	return upload(
    			file.getFileName().toString(),
    			file);
    }
    
    public String upload(String documentName, Path file) throws IOException {
    	File fileMetadata = new File().setName(documentName);
    	URLConnection connection = file.toUri().toURL().openConnection();
        String mimeType = connection.getContentType();
        log.info("Uploading: "+file.getFileName()+"; with type: "+mimeType); 
        FileContent mediaContent = new FileContent(mimeType, file.toFile());
    	File newFile = driveService.files().create(fileMetadata, mediaContent)
    	    .setFields("id")
    	    .execute();
    	return newFile.getId();
    }
    
    public URI getPublicUri(String fileId) throws IOException {
    	Permission domainPermission = new Permission()
    		    .setType("anyone")
    		    .setRole("reader")
    		    .setAllowFileDiscovery(Boolean.TRUE);
    	getDrive().permissions().create(fileId, domainPermission)
    		    .setFields("id").execute();
    	File tmp = getDrive().files()
    			.get(fileId)
    			.setFields("webContentLink,webViewLink,thumbnailLink")
    			.execute();
    	URI uri = URI.create(tmp.getWebContentLink());
    	URI finalUri = getFinalURI(uri);
    	boolean success = false;
		int i = 1;
		while (!success && i < 10) {
			try {
				URLConnection con = finalUri.toURL().openConnection();
				con.getContentType();
				success = true;
				log.debug("Content available at: "+uri+"/"+finalUri+" after "+i+" attempts.");
			} catch (Exception e) {
				i = i + 1;
				try	{
				    Thread.sleep(1000);
				} catch(InterruptedException ex) {
				    Thread.currentThread().interrupt();
				}
			}
		}
		if (!success) throw new IOException("Cannot open public URL: "+uri+" using: "+finalUri);
		return finalUri;
		// https://stackoverflow.com/questions/70311191/access-to-the-provided-image-was-forbidden-even-though-it-was-uploaded-from-the
    }
    
    public URI getThumbnailUri(String fileId) throws IOException {
    	Permission domainPermission = new Permission()
    		    .setType("anyone")
    		    .setRole("reader")
    		    .setAllowFileDiscovery(Boolean.TRUE);
    	getDrive().permissions().create(fileId, domainPermission)
    		    .setFields("id").execute();
    	File tmp = getDrive().files()
    			.get(fileId)
    			.setFields("webContentLink,webViewLink,thumbnailLink")
    			.execute();
    	
    	URI uri = URI.create(
    			tmp.getThumbnailLink().replaceAll("=s220", "=s16383")
    			);
    	return uri;
		// https://stackoverflow.com/questions/70311191/access-to-the-provided-image-was-forbidden-even-though-it-was-uploaded-from-the
    }
    //s16383 is the biggest thumbnail allowed. It will give the original image
    
    private static URI getFinalURI(URI uri) throws IOException {
        HttpURLConnection con = (HttpURLConnection) uri.toURL().openConnection();
        con.setInstanceFollowRedirects(false);
        con.connect();
        con.getInputStream();

        if (con.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM || con.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
            String redirectUrl = con.getHeaderField("Location");
            return getFinalURI(URI.create(redirectUrl));
        }
        
        return uri;
    }
    
    public void delete(String fileId) throws IOException {
    	File tmp = getDrive().files()
    			.get(fileId)
    			.execute();
    	log.info("Deleting file: "+tmp.getName());
    	getDrive().files().delete(tmp.getId()).execute();
    }
    
}
