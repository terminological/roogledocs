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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
import com.google.api.services.slides.v1.Slides;
import com.google.api.services.slides.v1.SlidesScopes;
import com.google.api.services.slides.v1.model.Presentation;

public class RService {
    
	static Map<String,RService> services = new HashMap<>();
	
	// Application name
    private static final String APPLICATION_NAME = "R Google Docs API";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    // Global instance of the scopes required by this application (If modifying these scopes, delete your previously saved tokens/ folder.)
    private static final List<String> SCOPES = Arrays.asList(
    		DocsScopes.DOCUMENTS,
    		DocsScopes.DRIVE,
    		SlidesScopes.PRESENTATIONS
    		);
    private static final String CREDENTIALS_FILE_PATH = "/client_secret.json";
    private Logger log = LoggerFactory.getLogger(RService.class);
    
    // Configuration
    private Path tokenDirectory = null;
    private Docs service = null;
    private Drive driveService = null;
	private Slides slideService = null;
    
    private RService() throws IOException, GeneralSecurityException {
    	this(Paths.get(System.getProperty("user.home"), ".roogledocs"));
    }
    
    private RService(String tokenDirectory) throws IOException, GeneralSecurityException {
    	this(Paths.get(tokenDirectory));
    }
    
    public static RService with(String tokenDirectory) throws IOException, GeneralSecurityException {
    	if (!services.containsKey(tokenDirectory)) {
    		RService out = new RService(tokenDirectory);
    		services.put(tokenDirectory, out);
    	}
    	return services.get(tokenDirectory);
    }
    
    // Constructor
    private RService(Path tokenDirectory) throws IOException, GeneralSecurityException {
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
    
    Slides getSlides()  {
    	return slideService;
    }
    
    protected Path getTokenDirectory() {return tokenDirectory;}
    
    public static class Exceptional<X> {
    	Exception e;
    	X result;
    	
    	public static <Y> Supplier<Exceptional<Y>> unsafe(UnsafeFunction<Y> function) {
    		return () -> of(function);
    	}
    	
    	public static <Y> Exceptional<Y> of(UnsafeFunction<Y> function) {
    		Exceptional<Y> ex = new Exceptional<>();
    		try {
    			ex.result = function.call();
    		} catch (RuntimeException f) {
    			throw f;
    		} catch (Exception f) {
    			ex.e = f;
    		}
    		return ex;
    	}
    	
    	public X result() throws Exception {
    		if (e != null) throw e;
    		return result;
    	}
    	
    	public Optional<X> opt() {
    		if (e != null) return Optional.empty();
    		return Optional.ofNullable(result);
    	}
    }
    
    private static interface UnsafeFunction<X> {
    	public X call() throws Exception;
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

//        HttpRequestInitializer timeout = new HttpRequestInitializer() {
//			@Override
//			public void initialize(HttpRequest request) throws IOException {
//				request.setConnectTimeout(30);
//				request.setReadTimeout(30);
//			}
//        };
        
        
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(tokenDirectory.toFile()))
                .setAccessType("offline")
//                .setRequestInitializer(timeout)
                .build();
        // TODO: need to override local server reciever to time out if no response
        // as this can hang R process.
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        AuthorizationCodeInstalledApp loginApp = new AuthorizationCodeInstalledApp(flow, receiver);
        
        Credential credential;
        try { 
        	flow.loadCredential("user");
        } catch (IOException e) {
        	log.warn("Existing credentials could not be loaded and have been deleted.");
        	deregister(tokenDirectory.toString());
        }
        
		try {
			credential = CompletableFuture
					.supplyAsync(Exceptional.unsafe(() -> loginApp.authorize("user")))
					.get(60, TimeUnit.SECONDS)
					.result();
		} catch (InterruptedException | TimeoutException e) {
			try {
				receiver.stop();
			} catch (IOException e2) {}
			throw new IOException("Authorisation interrupted or timed out.", e.getCause() == null ? e : e.getCause());
		} catch (Exception e) {
			try {
				receiver.stop();
			} catch (IOException e2) {}
			throw new IOException("Problem authenticating using existing credentials.", e.getCause() == null ? e : e.getCause());
		} 
		
        //returns an authorized Credential object.
        
        service = new Docs.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
        driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
        		.setApplicationName(APPLICATION_NAME)
        		.build();
        slideService = new Slides.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
        		.setApplicationName(APPLICATION_NAME)
        		.build();
    }

    public static final String MIME_SLIDES =  "application/vnd.google-apps.presentation";
    public static final String MIME_DOCS =  "application/vnd.google-apps.document";
    public static final String MIME_PNG =  "image/png";
    
    public List<Tuple<String,String>> search(String documentName, String mimeType) throws IOException {
    	return search(documentName, mimeType, false);
    }
    
    public List<Tuple<String,String>> search(String documentName, String mimeType, boolean exact) throws IOException {
    	return search(documentName, mimeType, exact, Collections.emptyList());
    }
    
    // file id, file name tuple list (maybe multiple with same name)
    public List<Tuple<String,String>> search(String documentName, String mimeType, boolean exact, List<String> parents) throws IOException {
    	// https://developers.google.com/drive/api/guides/search-files
    	List<Tuple<String,String>> out = new ArrayList<>();
    	String pageToken = null;
    	String qry = 
    			"name "+(exact ? "=" : "contains")+" '"+documentName+"'" +
    			(mimeType!=null ? " and mimeType = '"+mimeType+"'" : "")+
    			" and trashed = false";
    	String pQry = parents.stream().map(p -> " and '"+p+"' in parents").collect(Collectors.joining());
    	qry = qry+pQry;
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
    
    public String findUniqueId(String documentName, String mimeType) throws IOException {
    	List<Tuple<String,String>> tmp = search(documentName, mimeType, true);
    	if (tmp.size() == 0) throw new IOException(documentName+" not found.");
    	if (tmp.size() >1) throw new IOException("Multiple matches for "+documentName);
    	return(tmp.get(0).getFirst());
    }
    
    public RDocument getOrCreateDocument(String documentName) throws IOException {
    	List<Tuple<String, String>> tmp = search(documentName, MIME_DOCS, true);
    	if (tmp.size() > 0) {
    		if(tmp.size() > 1) log.warn("More than one possible match detected. Using first found. It is probably better to use a share url or document id.");
    		String docId = tmp.get(0).getFirst(); 
    		return new RDocument(docId,documentName,this);
    	}
    	Document doc = new Document().setTitle(documentName);
    	doc = getDocs().documents().create(doc).execute();
    	log.info("Created new document with title: " + doc.getTitle());
    	return new RDocument(doc.getDocumentId(),documentName,this);
    }
    
    public RPresentation getOrCreatePresentation(String documentName) throws IOException {
    	List<Tuple<String, String>> tmp = search(documentName, MIME_SLIDES, true);
    	if (tmp.size() > 0) {
    		if(tmp.size() > 1) log.warn("More than one possible match detected. Using first found. It is probably better to use a share url or presentation id.");
    		String docId = tmp.get(0).getFirst(); 
    		return new RPresentation(docId,documentName,this);
    	}
    	Presentation doc = new Presentation().setTitle(documentName);
    	doc = getSlides().presentations().create(doc).execute();
    	log.info("Created new presentation with title: " + doc.getTitle());
    	return new RPresentation(doc.getPresentationId(),documentName,this);
    }
    
    public RDocument getOrCloneDocument(String documentName, String templateUri) throws IOException {
    	List<Tuple<String, String>> tmp = search(documentName, MIME_DOCS, true);
    	if (tmp.size() > 0) {
    		if(tmp.size() > 1) log.warn("More than one possible match detected. Using first found. It is probably better to use a share url or document id.");
    		String docId = tmp.get(0).getFirst(); 
    		return new RDocument(docId,documentName,this);
    	}
    	
    	String templateId = extractDocId(templateUri);
    	File f = new File().setName(documentName);
    	File newf = driveService.files().copy(templateId, f).execute();
    	if (!newf.getMimeType().equals(MIME_DOCS)) {
    		driveService.files().delete(newf.getId());
    		throw new IOException("templateUri must refer to a google doc.");
    	}
    	log.info("Created new document with title: " + documentName);
    	return new RDocument(newf.getId(),documentName,this);
    }
    
    public RPresentation getOrClonePresentation(String documentName, String templateUri) throws IOException {
    	List<Tuple<String, String>> tmp = search(documentName, MIME_SLIDES, true);
    	if (tmp.size() > 0) {
    		if(tmp.size() > 1) log.warn("More than one possible match detected. Using first found. It is probably better to use a share url or document id.");
    		String docId = tmp.get(0).getFirst(); 
    		return new RPresentation(docId,documentName,this);
    	}
    	
    	String templateId = extractDocId(templateUri);
    	File f = new File().setName(documentName);
    	File newf = driveService.files().copy(templateId, f).execute();
    	if (!newf.getMimeType().equals(MIME_SLIDES)) {
    		driveService.files().delete(newf.getId());
    		throw new IOException("templateUri must refer to a google slides.");
    	}
    	log.info("Created new document with title: " + documentName);
    	return new RPresentation(newf.getId(),documentName,this);
    }
    
    protected static String extractDocId(String docId) throws IOException {
    	if (docId == null) throw new IOException("URL format docId cannot be parsed: "+docId);
    	if (docId.startsWith("http")) {
    		Pattern p = Pattern.compile("/([^/]+)/[^/]+$");
    		Matcher m = p.matcher(docId);
    		if (!m.find()) throw new IOException("URL format docId cannot be parsed: "+docId);
    		docId = docId.substring(m.start(1),m.end(1));
    	}
    	return docId;
    }
    
    public RDocument getDocument(String docId) throws IOException {
    	
    	// https://docs.google.com/document/d/1woDbkXAkf6RbvtjGlXMPBOl7zvDCviAvAWDZGNerYCk/edit?usp=sharing
    	docId = extractDocId(docId);
    	
    	File f = driveService.files().get(docId).execute();
    	if (f != null) {
    		if (f.getMimeType().equals(MIME_DOCS)) {
    			return new RDocument(docId,f.getName(), this);
    		} else {
    			throw new IOException("File: "+f.getName()+" is not a google doc");
    		}
    	}
    	throw new IOException("No document for: "+docId);
    }
    
    public RPresentation getPresentation(String docId) throws IOException {
    	
    	// https://docs.google.com/presentation/d/1FTXrtDafyVfoOj8CYaZlxVA8XnrcZdDJHUnDOmvCsjY/edit?usp=sharing
    	docId = extractDocId(docId);
    	
    	File f = driveService.files().get(docId).execute();
    	if (f != null) {
    		if (f.getMimeType().equals(MIME_SLIDES)) {
    			return new RPresentation(docId, f.getName(), this);
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
    
    public String uploadTmp(Path file) throws IOException {
    	return upload(
    			"temp_"+UUID.randomUUID()+getExtension(file).orElse(""),
    			file);
    }
    
    //TODO: A copy to same directory 
    
    public List<String> getFileParents(String driveId) throws IOException {
    	File f = driveService.files().get(driveId).setFields("id,kind,mimeType,name,createdTime,modifiedTime,size,parents").execute();
    	return f.getParents() == null ? Collections.emptyList() : f.getParents();
    }
    
    public String upload(String documentName, Path file) throws IOException {
    	return upload(documentName, file, Collections.emptyList(), false, false);
    }
    
    public String upload(String documentName, Path file, List<String> parents, boolean overwrite, boolean duplicate) throws IOException {
    	URLConnection connection = file.toUri().toURL().openConnection();
        String mimeType = connection.getContentType();
        
        List<Tuple<String,String>> existingMatches = this.search(documentName, null, true, parents);
		if (existingMatches.size() > 0) {
			if (!overwrite && !duplicate) {
				throw new IOException("Aborting upload as a file of this name already exists: "+documentName);
			}
			if (!duplicate) {
				for (Tuple<String,String> existing: existingMatches) {
					log.info("Deleting existing file: "+existing.getSecond());
					String id = existing.getFirst();
					delete(id);
				};
			} else {
				log.info("Duplicating existing file: "+documentName);
			}
		}	
        
    	File fileMetadata = new File().setName(documentName).setParents(parents);
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
    
    public void deleteByName(String documentName, String mimeType) throws IOException {
    	List<Tuple<String, String>> tmp = search(documentName, mimeType, true);
    	if (tmp.size() == 1) {
    		String docId = tmp.get(0).getFirst();
    		delete(docId);
    	} else if (tmp.size() > 1) {
    		throw new IOException("More than one possible match detected. aborting delete.");
    	} else {
    		log.info("No documents found with name: "+documentName);
    	}
    }
    
    public void delete(String fileId) throws IOException {
    	File tmp = getDrive().files()
    			.get(fileId)
    			.execute();
    	log.info("Deleting file: "+tmp.getName());
    	getDrive().files().delete(tmp.getId()).execute();
    }

	public static void deregister(String tokenDirectory) throws IOException {
		Path tokenPath = Paths.get(tokenDirectory);
		Files.walk(tokenPath)
			.filter(f -> !Files.isDirectory(f))
			.forEach(f -> {
				try {
					Files.delete(f);
				} catch (IOException e) {
					System.out.println("problem deleting "+f.toString());
				}
			});
		services.remove(tokenDirectory);
	}
    
	private Optional<String> getExtension(Path file) {
	    return Optional.ofNullable(file)
	    		.map(p -> p.toString())
	    		.filter(f -> f.contains("."))
	    		.map(f -> f.substring(f.lastIndexOf(".")));
	}
	
}
