package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
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


import org.springframework.boot.web.server.LocalServerPort;
import uk.ac.man.cs.eventlite.EventLite;
import static org.hamcrest.Matchers.containsString;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.web.client.TestRestTemplate.HttpClientOption;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventsControllerIntegrationTest extends AbstractTransactionalJUnit4SpringContextTests {
	
	@LocalServerPort
	private int port;
	
	private String loginUrl;
	private String eventsURL;
	private String addEventURL;
	private String anEventURL;
	private String fakeEventURL;
	private String urlNotExist;

	private static final String INDEX = "/5";
	
	private HttpEntity<String> httpEntity;
	
	@Autowired
	private TestRestTemplate state;
	
	private final TestRestTemplate anonymous = new TestRestTemplate();
	
	@Autowired
	private TestRestTemplate template;

	@BeforeEach
	public void setup() {
		this.loginUrl = "http://localhost:" + port + "/sign-in";
		this.eventsURL = "http://localhost:" + port + "/events";
		this.addEventURL = "http://localhost:" + port + "/events/add";
		this.anEventURL = "http://localhost:" + port + "/events/" + INDEX;
		this.fakeEventURL = "http://localhost:" + port + "/events/" + 0;
		this.urlNotExist = "http://localhost:" + port + "/urlNotExist";
		
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
	}
	
	
	@Test
	public void addEventNoLogin() {
		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("name", "TEST_1");
		form.add("date", "2025-02-15");
		form.add("venue", "1");
		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(form,
				postHeaders);

		ResponseEntity<String> response = anonymous.exchange(addEventURL, HttpMethod.POST, postEntity, String.class);
		
		//count the number of events
		int count = countRowsInTable("Events");

		assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));
		assertThat(response.getHeaders().getLocation().toString(), equalTo(loginUrl));
		assertThat(count, equalTo(countRowsInTable("Events")));
	}
	
	@Test
	public void addEventNoData() {
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
		
		int count = countRowsInTable("Events");

		ResponseEntity<String> response = state.exchange(addEventURL, HttpMethod.POST, postEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
		//there should not be more events stored than before
		assertThat(count, equalTo(countRowsInTable("Events")));
	}
	
	@Test
	public void addEventBadData() {
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

		assertThat(location, equalTo("http://localhost:" + port + "/"));
		assertThat(redirectedEvents.getStatusCode(), equalTo(HttpStatus.OK));

		getHead.set("Cookie", cookie);
		getEntity = new HttpEntity<>(getHead);
		formResponse = state.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
		csrfToken = formResponse.getBody().substring(formResponse.getBody().indexOf("id=\"_csrf\" content=") + 20 , formResponse.getBody().indexOf("id=\"_csrf\" content=") + 56);

		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", csrfToken);
		form.add("name", "2050-52-43");
		form.add("date", "Bad Data Test");
		form.add("venue", "0");
		postEntity = new HttpEntity<MultiValueMap<String, String>>(form, postHeaders);
		
		int count = countRowsInTable("Events");

		ResponseEntity<String> response = state.exchange(addEventURL, HttpMethod.POST, postEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
		assertThat(count, equalTo(countRowsInTable("Events")));
	}
	
	@Test
	@DirtiesContext
	public void addEventWithLogin() {
		state = new TestRestTemplate(HttpClientOption.ENABLE_COOKIES);

		HttpHeaders getHead = new HttpHeaders();
		getHead.setAccept(Collections.singletonList(MediaType.TEXT_HTML));

		HttpHeaders postHeaders = new HttpHeaders();
		postHeaders.setAccept(Collections.singletonList(MediaType.TEXT_HTML));
		postHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		HttpEntity<String> getEntity = new HttpEntity<>(getHead);
		ResponseEntity<String> formResponse = state.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
		
		String csrfToken = formResponse.getBody().substring(formResponse.getBody().indexOf("id=\"_csrf\" content=") + 20 , formResponse.getBody().indexOf("id=\"_csrf\" content=") + 56);//getCsrfToken(formResponse.getBody());

		String cookie = formResponse.getHeaders().getFirst("Set-Cookie").split(";")[0];

		postHeaders.set("Cookie", cookie);
		MultiValueMap<String, String> login = new LinkedMultiValueMap<>();
		login.add("_csrf", csrfToken);
		login.add("username", "Organiser");
		login.add("password", "Organiser");

		HttpEntity<MultiValueMap<String, String>> postEntity = new HttpEntity<MultiValueMap<String, String>>(login, postHeaders);
		ResponseEntity<String> loginResponse = state.exchange(loginUrl, HttpMethod.POST, postEntity, String.class);
		assertThat(loginResponse.getStatusCode(), equalTo(HttpStatus.FOUND));
		
		String location = loginResponse.getHeaders().getLocation().toString();
		ResponseEntity<String> redirectedEvents = anonymous.exchange(location, HttpMethod.GET,httpEntity, String.class);

		assertThat(location, equalTo("http://localhost:" + port + "/"));
		assertThat(redirectedEvents.getStatusCode(), equalTo(HttpStatus.OK));
		
		getHead.set("Cookie", cookie);
		getEntity = new HttpEntity<>(getHead);
		formResponse = state.exchange(loginUrl, HttpMethod.GET, getEntity, String.class);
		csrfToken = formResponse.getBody().substring(formResponse.getBody().indexOf("id=\"_csrf\" content=") + 20 , formResponse.getBody().indexOf("id=\"_csrf\" content=") + 56);//getCsrfToken(formResponse.getBody());
		
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("_csrf", csrfToken);
		form.add("name", "Integration Test");
		form.add("date", "2021-03-10");
		form.add("venue", "3");
		postEntity = new HttpEntity<MultiValueMap<String, String>>(form, postHeaders);
		
		int count = countRowsInTable("Events");

		ResponseEntity<String> response = state.exchange(addEventURL, HttpMethod.POST, postEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));
		assertThat(response.getHeaders().getLocation().toString(), containsString(eventsURL));
		assertThat(count+1, equalTo(countRowsInTable("Events")));
	}
	
	@Test
	public void getOneEvent() {
		ResponseEntity<String> response = anonymous.exchange(anEventURL, HttpMethod.GET, httpEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
		assertThat(response.getBody() , containsString("Event Apple"));
		assertThat(response.getBody() , containsString("Venue A")); 
		assertThat(response.getBody() , containsString("At 18:30 on Friday, 13")); 
		assertThat(response.getBody() , containsString("August 2021")); 
		assertThat(response.getHeaders().getContentType().toString(), containsString(MediaType.TEXT_HTML_VALUE));
	}
	
	@Test
	public void getNonExistentEvent() {
		ResponseEntity<String> response = anonymous.exchange(fakeEventURL, HttpMethod.GET, httpEntity, String.class);
		String location = response.getHeaders().getLocation().toString();
		ResponseEntity<String> redirected = anonymous.exchange(location, HttpMethod.GET,
											httpEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));
		assertThat(location, equalTo(urlNotExist));
		assertThat(redirected.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
		assertThat(redirected.getBody() , containsString("error"));
	}
	
	@Test
	public void testGetAllEventsWithNoSearch() {
		ResponseEntity<String> response = template.exchange("/events", HttpMethod.GET, httpEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
		assertThat(response.getHeaders().getContentType().toString(), containsString(MediaType.TEXT_HTML_VALUE));
	}
	
	@Test
	public void testGetAllEventsWithSearch() {
		ResponseEntity<String> response = template.exchange("/events/search?search=hi", HttpMethod.GET, httpEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
		assertThat(response.getHeaders().getContentType().toString(), containsString(MediaType.TEXT_HTML_VALUE));
	}
	
	@Test
	public void testGetAllEventsWithEmptySearch() {
		ResponseEntity<String> response = template.exchange("/events/search?search=", HttpMethod.GET, httpEntity, String.class);
		String location = response.getHeaders().getLocation().toString();
		ResponseEntity<String> redirected = anonymous.exchange(location, HttpMethod.GET, httpEntity, String.class);

		assertThat(response.getStatusCode(), equalTo(HttpStatus.FOUND));
		assertThat(location, equalTo(eventsURL));
		assertThat(redirected.getStatusCode(), equalTo(HttpStatus.OK));
	}
}
