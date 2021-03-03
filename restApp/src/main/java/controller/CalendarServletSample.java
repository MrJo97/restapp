package controller;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.extensions.appengine.datastore.AppEngineDataStoreFactory;
import com.google.api.client.extensions.servlet.auth.oauth2.AbstractAuthorizationCodeServlet;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;

public class CalendarServletSample extends AbstractAuthorizationCodeServlet {

	//private static final DataStoreFactory DATA_STORE_FACTORY = new AppEngineDataStoreFactory() ;
	private static final String TOKENS_DIRECTORY_PATH = "tokens";

	  @Override
	  protected String getRedirectUri(HttpServletRequest req) throws ServletException, IOException {
	    GenericUrl url = new GenericUrl(req.getRequestURL().toString());
	    url.setRawPath("/restApp/loginWithGoogle");
	    return url.build();
	  }
	  
	  @Override
	  protected AuthorizationCodeFlow initializeFlow() throws IOException {
	    return new GoogleAuthorizationCodeFlow.Builder(
	        new NetHttpTransport(), JacksonFactory.getDefaultInstance(),
	        "595271248680-ca0u49c96v14unqgkt5gju5mkcrpuj4q.apps.googleusercontent.com", 
	        "5RwsaUwWmObl73UAsCFJ7iUo", Collections.singleton(CalendarScopes.CALENDAR))
	    	.setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH))).setAccessType("offline").build();
	  }

	  @Override
	  protected String getUserId(HttpServletRequest req) throws ServletException, IOException {
		  return null;
	  }
	}
