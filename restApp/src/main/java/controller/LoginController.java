package controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.PeopleServiceScopes;
import com.google.api.services.people.v1.model.EmailAddress;
import com.google.api.services.people.v1.model.ListConnectionsResponse;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.restfb.DebugHeaderInfo;
import com.restfb.DefaultFacebookClient;
import com.restfb.DefaultWebRequestor;
import com.restfb.FacebookClient;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.WebRequestor;
import com.restfb.types.User;

import org.json.*;
@Controller
public class LoginController {
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	// metodo per richiedere un nuovo access token utilizzando il refresh token
	public TokenResponse refreshAccessToken(String refreshToken) throws IOException {
		TokenResponse response = new GoogleRefreshTokenRequest(new NetHttpTransport(), new JacksonFactory(),
				refreshToken, "your clientId", // qui si inserisce il client id
				"your clientSecret") // qui si inserisce il client secret
						.execute();
		System.out.println("Access token: " + response.getAccessToken());
		return response;
	}
	
	
	

	@RequestMapping("/prepareLoginWithGoogle")
	public ModelAndView prepareLoginWithGoogle(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		ModelAndView model = new ModelAndView("HomePage");
		HttpSession session = request.getSession();

		// se viene effettuato il logout
		if (session.getAttribute("access_token_google") == null && session.getAttribute("refresh_token_google") == null) {
			CalendarServletSample CSS = new CalendarServletSample();
			AuthorizationCodeFlow oauth = CSS.initializeFlow();
			String idUser = "7";
			Credential credentials = oauth.loadCredential(idUser);

			// test
			System.out.println(credentials == null);
			System.out.println("redirect uri:" + CSS.getRedirectUri(request));
			System.out.println(oauth.getCredentialDataStore());

			if (credentials == null) // l'utente non ha ancora acceduto in questa sessione
			{
				AuthorizationCodeRequestUrl authCodeRequestURL = oauth.newAuthorizationUrl();
				// modifico l'indirizzo a cui dovrò essere reindirizzato una volta autenticato
				// NB: tale indirizzo deve essere lo stesso che è stato inserito nei campi di
				// redirect URI
				// una volta registrata l'applicazione.
				authCodeRequestURL.setRedirectUri(CSS.getRedirectUri(request));

				/*
				 * definizione degli scope NB: gli scope sono estremamente importanti perchè
				 * permettono di definire appunto gli scopi per i quali viene effettuata la
				 * richiesta.
				 * 
				 */
				List<String> scopes = new ArrayList<String>();
				scopes.add("https://www.googleapis.com/auth/profile.emails.read");
				scopes.add("https://www.googleapis.com/auth/userinfo.profile"); // questo scope mi permettedi vedere i
																				// dati del profilo utente
				scopes.add("https://www.googleapis.com/auth/contacts.readonly"); // permette di vedere i contatti
				scopes.add("https://www.googleapis.com/auth/calendar");
				authCodeRequestURL.setScopes(scopes);

				// test
				System.out.println("scopes:" + authCodeRequestURL.getScopes());
				System.out.println("redirect uri:" + authCodeRequestURL.getRedirectUri());
				// URI a cui devo essere reindirizzato per autenticarmi
				// this is the URI to which i need to be redirected to authenticate
				System.out.println("autentication uri:" + authCodeRequestURL.build());
				// id dell'applicazione (è sempre lo stesso)
				System.out.println("ID dell'applicazione registrata in google:" + authCodeRequestURL.getClientId());
				response.sendRedirect(authCodeRequestURL.build()); // vengo reindirizzato alla pagina di
																	// autenticazione
			}
		}
		// prima o poi se l'access token non è piu valido sarà necessario utilizzare il
		// refresh token per ottenerne un'altro.
		/*
		 * tokenResponse.getExpiresInSeconds(); //fornisce il numero di secondi che
		 * mancano alla scadenza dell'access token //se va a 0 allora richiediamo un
		 * nuovo access token usando il refresh token
		 * if(tokenResponse.getExpiresInSeconds()==0) { TokenResponse accessToken =
		 * refreshAccessToken(refreshToken); }
		 */

		return model;
	}

	// questo metodo viene invocato una volta che l'utente si è loggato con google
	// ed ha acconsentito
	// al recupero di determinate sue informazioni (da parte dell'applicazione).
	@RequestMapping("/loginWithGoogle")
	public ModelAndView loginWithGoogle(HttpServletRequest request)
			throws IOException, ServletException, GeneralSecurityException {
		ModelAndView model = new ModelAndView("HomePage");

		CalendarServletSample CSS = new CalendarServletSample();
		String idUser = "8";
		AuthorizationCodeFlow oauth = CSS.initializeFlow();
		// richiesta di un token

		//recupero il valore del parametro code dall'URL
		String authorizationCode = request.getParameter("code");
		AuthorizationCodeTokenRequest gactr = oauth.newTokenRequest(authorizationCode);
		System.out.println("code:" + authorizationCode);
		
		gactr.setRedirectUri(CSS.getRedirectUri(request));
		TokenResponse tokenResponse = gactr.execute();
		Credential newCredential = oauth.createAndStoreCredential(tokenResponse, idUser);
		HttpSession session = request.getSession();

		// è importante memorizzarlo: per usare le api google come Maps, Drive, gmail,
		// etc..., è necessario averlo
		session.setAttribute("access_token_google", newCredential.getAccessToken());
		// questo è utile per richiedere l'access token quando l'access token attuale è
		// scaduto
		session.setAttribute("refresh_token_google", newCredential.getRefreshToken());

		// test
		System.out.println("accessToken: " + newCredential.getAccessToken());
		System.out.println("refreshToken: " + newCredential.getRefreshToken());

		// HttpTransport HTTP_TRANSPORT = newCredential.getTransport();
		
		// uso della People API
		// Build a new authorized API client service.
		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
		PeopleService service = new PeopleService.Builder(HTTP_TRANSPORT, JSON_FACTORY, newCredential)
				/* .setApplicationName(APPLICATION_NAME) */
				.build();

		// Request 10 connections.
		ListConnectionsResponse response = service.people().connections().list("people/me").setPageSize(10)
				.setPersonFields("names,emailAddresses").execute();

		Person profile = service.people().get("people/me").setPersonFields("names,emailAddresses").execute();
		List<EmailAddress> emailAddresses = profile.getEmailAddresses();
		if (emailAddresses != null && emailAddresses.size() > 0) {
			for (EmailAddress email : emailAddresses) {
				System.out.println(
						"Indirizzo email: " + email.getDisplayName() + " " + email.getValue() + " " + email.getType());
			}
		} else {
			System.out.println("No email addresses");
		}

		// Print display name of connections if available.
		List<Person> connections = response.getConnections();
		if (connections != null && connections.size() > 0) {
			for (Person person : connections) {
				List<Name> names = person.getNames();
				if (names != null && names.size() > 0) {
					System.out.println("Name: " + person.getNames().get(0).getDisplayName());
				} else {
					System.out.println("No names available for connection.");
				}
			}
		} else {
			System.out.println("No connections found.");
		}

		return model;
	}

	
	private /*FacebookClient.AccessToken*/String getFacebookUserToken(String code, String redirectUrl) throws IOException, ParseException {
	    String appId = "2761887687391218";
	    String secretKey = "b85ee5dee02c1a8c53f7f263227e6a99";

	    WebRequestor wr = new DefaultWebRequestor();
	    WebRequestor.Response accessTokenResponse = wr.executeGet(
	            "https://graph.facebook.com/oauth/access_token?client_id=" + appId + "&redirect_uri=" + redirectUrl
	            + "&client_secret=" + secretKey + "&code=" + code);

	   
	    System.out.println(accessTokenResponse.getBody());
	    JSONParser parser = new JSONParser();
	    JSONObject json = (JSONObject) parser.parse(accessTokenResponse.getBody());
	    String access_token = (String)json.get("access_token");
	    System.out.println(access_token);
	   // return DefaultFacebookClient.AccessToken.fromQueryString(accessTokenResponse.getBody());
	    return access_token;
	}
	
	
	
	@RequestMapping("/prepareLoginWithFacebook")
	public ModelAndView prepareLoginWithFacebook(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		ModelAndView model = new ModelAndView("HomePage");
		HttpSession session = request.getSession();
		if (session.getAttribute("access_token_facebook") == null)
		{
		String url = "https://www.facebook.com/dialog/oauth";
		String client_id = "client_id=" + "2761887687391218";
		String redirect_uri = "redirect_uri=" + "http://localhost:8080/restApp/loginWithFacebook";
		String scope = "scope=email,user_gender,user_birthday";
		url = url + "?" + client_id + "&" + redirect_uri + "&" + scope;
		response.sendRedirect(url); // vengono reindirizzato alla pagina di
		// autenticazione
		}
		return model;
	}

	@RequestMapping("/loginWithFacebook")
	public ModelAndView loginWithFacebook(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException, GeneralSecurityException, ParseException {
		ModelAndView model = new ModelAndView("HomePage");
		HttpSession session = request.getSession();		
		String access_token  = getFacebookUserToken(request.getParameter("code"),"http://localhost:8080/restApp/loginWithFacebook");
		session.setAttribute("access_token_facebook", access_token);
		
		//recupero dell'indirizzo mail dell'utente (utilizzando l'access token):
		FacebookClient facebookClient = new DefaultFacebookClient(access_token, Version.VERSION_7_0);
		
		User user = facebookClient.fetchObject("me", User.class, Parameter.with("fields", "email,first_name,last_name,gender,birthday"));
		String email = user.getEmail();
		String nome = user.getFirstName();
		String cognome = user.getLastName();
		String genere = user.getGender();
		String compleanno = user.getBirthday();
		
		System.out.println("Indirizzo email: " + email);
		System.out.println("nome: " + nome);
		System.out.println("cognome: " + cognome);
		System.out.println("genere: " + genere);
		System.out.println("compleanno: " + compleanno);
		return model;
	}

	@RequestMapping("/logout")
	public ModelAndView logout(HttpServletRequest request) throws IOException, ServletException {
		ModelAndView model = new ModelAndView("login");
		HttpSession session = request.getSession();
		session.removeAttribute("accessToken");
		session.removeAttribute("refreshToken");
		session.removeAttribute("access_token_facebook");
		return model;
	}

}
