package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.client.TestRestTemplate.HttpClientOption;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import uk.ac.man.cs.eventlite.EventLite;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VenuesControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {

	@LocalServerPort
	private int port;
	
	private String loginUrl;
	private String venuesURL;
	private String addVenueURL;
	private String aVenueURL;
	private String fakeVenueURL;
	private String urlNotExist;
	
	private static final String INDEX = "/1";
	
	private HttpEntity<String> httpEntity;
	
	// We need cookies for Web log in.
	// Initialize this each time we need it to ensure it's clean.
	@Autowired
	private TestRestTemplate state;
	
	// An anonymous and stateless log in.
	private final TestRestTemplate anonymous = new TestRestTemplate();
	
	@Autowired
	private TestRestTemplate template;

	@BeforeEach
	public void setup() {
		this.loginUrl = "http://localhost:" + port + "/sign-in";
		this.venuesURL = "http://localhost:" + port + "/venues";
		this.addVenueURL = "http://localhost:" + port + "/venues/add";
		this.aVenueURL = "http://localhost:" + port + "/venues/" + INDEX;
		this.fakeVenueURL = "http://localhost:" + port + "/venues/" + 50;
		this.urlNotExist = "http://localhost:" + port + "/urlNotExist";
		
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

		httpEntity = new HttpEntity<String>(headers);
	}
	
	private String getCsrfToken(String body) {
		Pattern pattern = Pattern.compile("(?s).*name=\"_csrf\".*?value=\"([^\"]+).*");
		Matcher matcher = pattern.matcher(body);

		assertThat(matcher.matches(), equalTo(true));

		return matcher.group(1);
	}
	
	@Test
	public void addVenueNoLogin() {
		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "Test Venue");
		form.add("capacity", "99");
		form.add("address", "X Road");
		form.add("postcode", "M00 0AA");
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(form,
				postHeaders);

		ResponseEntity<String> response = anonymous.exchange(addVenueURL, HttpMethod.POST, postEntity, String.class);

		// Count the number of venues.
		int count = countRowsInTable("Venues");

		assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));
		assertThat(response.getHeaders().getLocation().toString(), equalTo(loginUrl));
		assertThat(count, equalTo(countRowsInTable("Venues")));
	}
	
	@Test
	public void addVenueNoData() {
		state = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES);

		HttpHeaders getHead = new HttpHeaders();
		getHead.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		HttpEntity<String> getEntity = new HttpEntity<>(getHead);
		ResponseEntity<String> formResponse = state.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
		String csrfToken = formResponse.getBody().substring(formResponse.getBody().indexOf("id=\"_csrf\" content=") + 20 , formResponse.getBody().indexOf("id=\"_csrf\" content=") + 56);
		String cookie = formResponse.getHeaders().getFirst("Set-Cookie").split(";")[0];

		postHeaders.set("Cookie", cookie);
		MultiValueMap<String, String> login = new LinkedMultiValueMap<>();
		login.add("_csrf", csrfToken);
		login.add("username", "Organiser");
		login.add("password", "Organiser");

		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(login,
				postHeaders);
		ResponseEntity<String> loginResponse = state.exchange(loginUrl, HttpMethod.POST, postEntity, String.class);
		assertThat(loginResponse.getStatusCode(), equalTo(HttpStatus.FOUND));
		
		String location = loginResponse.getHeaders().getLocation().toString();
		ResponseEntity<String> redirectedEvents = anonymous.exchange(location, HttpMethod.GET,httpEntity, String.class);

		assertThat(location, equalTo("http://localhost:" + port+ "/"));
		assertThat(redirectedEvents.getStatusCode(), equalTo(HttpStatus.OK));

		getHead.set("Cookie", cookie);
		getEntity = new HttpEntity<>(getHead);
		formResponse = state.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
		csrfToken = formResponse.getBody().substring(formResponse.getBody().indexOf("id=\"_csrf\" content=") + 20 , formResponse.getBody().indexOf("id=\"_csrf\" content=") + 56);

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", csrfToken);
		postEntity = new HttpEntity<MultiValueMap<String, String>>(form, postHeaders);
		
		int count = countRowsInTable("venues");

		ResponseEntity<String> response = state.exchange(addVenueURL, HttpMethod.POST, postEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
		//there should not be more events stored than before
		assertThat(count, equalTo(countRowsInTable("venues")));		
	}
	
	@Test
	public void addVenueBadData() {
		state = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES);	
		
		HttpHeaders getHeaders = new HttpHeaders();
		getHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		HttpEntity<String> getEntity = new HttpEntity<>(getHeaders);
		ResponseEntity<String> formResponse = state.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
		String csrfToken = getCsrfToken(formResponse.getBody());
		String cookie = formResponse.getHeaders().getFirst("Set-Cookie").split(";")[0];

		postHeaders.set("Cookie", cookie);
		MultiValueMap<String, String> login = new LinkedMultiValueMap<>();
		login.add("_csrf", csrfToken);
		login.add("username", "Organiser");
		login.add("password", "Organiser");

		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(login,
				postHeaders);
		ResponseEntity<String> loginResponse = state.exchange(loginUrl, HttpMethod.POST, postEntity, String.class);
		assertThat(loginResponse.getStatusCode(), equalTo(HttpStatus.FOUND));

		getHeaders.set("Cookie", cookie);
		getEntity = new HttpEntity<>(getHeaders);
		formResponse = state.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
		csrfToken = getCsrfToken(formResponse.getBody());

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", csrfToken);
		form.add("name", "Test Venue");
		form.add("capacity", "-9");
		form.add("address", "X Road");
		form.add("postcode", "");
		postEntity = new HttpEntity<MultiValueMap<String, String>>(form, postHeaders);
		
		int count = countRowsInTable("venues");

		ResponseEntity<String> response = state.exchange(addVenueURL, HttpMethod.POST, postEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
		assertThat(count, equalTo(countRowsInTable("venues")));
	}
	
	@Test
	@DirtiesContext
	public void getAndPostVenueWithLogin() {
		state = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES);

		HttpHeaders getHeaders = new HttpHeaders();
		getHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		HttpEntity<String> getEntity = new HttpEntity<>(getHeaders);
		ResponseEntity<String> formResponse = state.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
		String csrfToken = getCsrfToken(formResponse.getBody());
		String cookie = formResponse.getHeaders().getFirst("Set-Cookie").split(";")[0];

		postHeaders.set("Cookie", cookie);
		MultiValueMap<String, String> login = new LinkedMultiValueMap<>();
		login.add("_csrf", csrfToken);
		login.add("username", "Organiser");
		login.add("password", "Organiser");

		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(login,
				postHeaders);
		ResponseEntity<String> loginResponse = state.exchange(loginUrl, HttpMethod.POST, postEntity, String.class);
		assertThat(loginResponse.getStatusCode(), equalTo(HttpStatus.FOUND));

		getHeaders.set("Cookie", cookie);
		getEntity = new HttpEntity<>(getHeaders);
		formResponse = state.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
		csrfToken = getCsrfToken(formResponse.getBody());

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", csrfToken);
		form.add("name", "Test Venue");
		form.add("capacity", "99");
		form.add("address", "X Road");
		form.add("postcode", "M00 0AA");
		
		postEntity = new HttpEntity<MultiValueMap<String, String>>(form, postHeaders);
		
		int count = countRowsInTable("venues");
		
		ResponseEntity<String> response = state.exchange(addVenueURL, HttpMethod.POST, postEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));
		assertThat(response.getHeaders().getLocation().toString(), containsString(venuesURL));
		assertThat(count + 1, equalTo(countRowsInTable("venues")));
	}
	
	@Test
	public void getOneVenue() {
		ResponseEntity<String> response = anonymous.exchange(aVenueURL, HttpMethod.GET, httpEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
		assertThat(response.getBody() , containsString("Venue A"));
		assertThat(response.getBody() , containsString("100"));
		assertThat(response.getBody() , containsString("23 Manchester Road"));
		assertThat(response.getBody() , containsString("E14 3BD"));
		assertThat(response.getHeaders().getContentType().toString(), containsString(MediaType.TEXT_HTML_VALUE));
	}
	
	@Test
	public void getNonexistentVenue() {

		ResponseEntity<String> response = anonymous.exchange(fakeVenueURL, HttpMethod.GET, httpEntity, String.class);

		String location = response.getHeaders().getLocation().toString();
		ResponseEntity<String> redirected = anonymous.exchange(location, HttpMethod.GET,
											httpEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));
		assertThat(location, equalTo(urlNotExist));
		assertThat(redirected.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
		assertThat(redirected.getBody() , containsString("error"));
	}
		
	@Test
	public void testGetAllVenuesWithNoSearch() {
		ResponseEntity<String> response = template.exchange("/venues", HttpMethod.GET, httpEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
		assertThat(response.getHeaders().getContentType().toString(), containsString(MediaType.TEXT_HTML_VALUE));
	}

	@Test
	public void testGetAllVenuesWithSearch() {
		ResponseEntity<String> response = template.exchange("/venues/search?search=hi", HttpMethod.GET, httpEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
		assertThat(response.getHeaders().getContentType().toString(), containsString(MediaType.TEXT_HTML_VALUE));
	}
	
	@Test
	public void testGetAllVenuesWithEmptySearch() {
		ResponseEntity<String> response = template.exchange("/venues/search?search=", HttpMethod.GET, httpEntity, String.class);
		String location = response.getHeaders().getLocation().toString();
		ResponseEntity<String> redirected = anonymous.exchange(location, HttpMethod.GET, httpEntity, String.class);
		
		assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));
		assertThat(location, equalTo(venuesURL));
		assertThat(redirected.getStatusCode(), equalTo(HttpStatus.OK));
	}

}
