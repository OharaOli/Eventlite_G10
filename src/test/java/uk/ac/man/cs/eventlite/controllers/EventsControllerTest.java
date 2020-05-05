package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.mockito.Mockito.doNothing;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import javax.servlet.Filter;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventsControllerTest {

	private MockMvc mvc;

	@Autowired
	private Filter springSecurityFilterChain;

	@Mock
	private Event event;

	@Mock
	private Venue venue;

	@Mock
	private EventService eventService;

	@Mock
	private VenueService venueService;

	@InjectMocks
	private EventsController eventsController;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mvc = MockMvcBuilders.standaloneSetup(eventsController).apply(springSecurity(springSecurityFilterChain))
				.build();
	}

	@Test
	public void getIndexWhenNoEvents() throws Exception {
		when(eventService.findAll()).thenReturn(Collections.<Event> emptyList());
//		when(venueService.findAll()).thenReturn(Collections.<Venue> emptyList());

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		verify(eventService).findAllPreviousEvents();
		verify(eventService).findAllFutureEvents();
//		verify(venueService).findAll();
		verifyZeroInteractions(event);
//		verifyZeroInteractions(venue);
	}

	@Test
	public void getIndexWithEvents() throws Exception {
		when(eventService.findAll()).thenReturn(Collections.<Event> singletonList(event));
//		when(venueService.findAll()).thenReturn(Collections.<Venue> singletonList(venue));

		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));

		//verify(eventService).findAll();
		verify(eventService).findAllPreviousEvents();
		verify(eventService).findAllFutureEvents();
//		verify(venueService).findAll();
		verifyZeroInteractions(event);
//		verifyZeroInteractions(venue);
	}
	
	@Test
	public void getSearchIndexWhenNoEvents() throws Exception {
		String testSearch = new String("HHH");
		when(eventService.findBySearchedBy(testSearch)).thenReturn(Collections.<Event> emptyList());

		mvc.perform(get("/events/search?search=HHH").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/search")).andExpect(handler().methodName("getEventsByName"));

		verify(eventService).findPastSearchedBy(testSearch);
		verify(eventService).findFutureSearchedBy(testSearch);
		verifyZeroInteractions(event);
	}

	@Test
	public void getSearchIndexWithEvents() throws Exception {
		String eventSearch = new String("Event");
		when(eventService.findBySearchedBy(eventSearch)).thenReturn(Collections.<Event> singletonList(event));

		mvc.perform(get("/events/search?search=Event").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/search")).andExpect(handler().methodName("getEventsByName"));

		verify(eventService).findPastSearchedBy(eventSearch);
		verify(eventService).findFutureSearchedBy(eventSearch);
		verifyZeroInteractions(event);
	}

	@Test
	public void getValidEventDetails() throws Exception {
		Optional<Event> testEvent = Optional.of(event);
		Long id = (long)1;
		
		when(eventService.findEventById(id)).thenReturn(testEvent);
		
		mvc.perform(get("/events/1").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
		.andExpect(view().name("events/details")).andExpect(handler().methodName("getOneEvent"));
		
		verify(eventService).findEventById(id);
		verifyZeroInteractions(eventService);
		verifyZeroInteractions(event);
	}
	
	@Test
	public void getInvalidEventDetails() throws Exception {
		Optional<Event> testEvent = Optional.<Event>empty();
		Long id = (long)0;
		
		when(eventService.findEventById(id)).thenReturn(testEvent);
		
		mvc.perform(get("/events/0").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
		.andExpect(view().name("events/details")).andExpect(handler().methodName("getOneEvent"));
		
		verify(eventService).findEventById(id);
		verifyZeroInteractions(eventService);
		verifyZeroInteractions(event);			
	}
	
	@Test
	public void deleteExistingEvent() throws Exception {
		Optional<Event> testEvent = Optional.of(event);
		Long id = (long)1;
		
		when(eventService.findById(id)).thenReturn(testEvent);
		doNothing().when(eventService).deleteById(id);
		
		mvc.perform(get("/events/delete_event?eventId=1").accept(MediaType.TEXT_HTML)).andExpect(status().isFound())
		.andExpect(view().name("redirect:/events")).andExpect(handler().methodName("deleteEvent"));
		
		verify(eventService).findById(id);
		verify(eventService).deleteById(id);
		verifyZeroInteractions(eventService);
		verifyZeroInteractions(event);			
	}
	
	@Test
	public void deleteNonExistentEvent() throws Exception {
		Optional<Event> testEvent = Optional.<Event>empty();
		Long id = (long)0;
		
		when(eventService.findById(id)).thenReturn(testEvent);
		
		mvc.perform(get("/events/delete_event?eventId=0").accept(MediaType.TEXT_HTML)).andExpect(status().isFound())
		.andExpect(view().name("redirect:/events")).andExpect(handler().methodName("deleteEvent"));
		
		verify(eventService).findById(id);
		verifyZeroInteractions(eventService);
		verifyZeroInteractions(event);			
	}
	
	@Test
	public void deleteEventNoAuth() throws Exception {
		long id = 1;
		mvc.perform(MockMvcRequestBuilders.delete("/events/{id}", id).accept(MediaType.TEXT_HTML)
			.with(csrf())).andExpect(status().isFound())
			.andExpect(header().string("Location", endsWith("/sign-in")));

		verify(eventService, never()).deleteById(id);
	}
	
	@Test
	public void deleteEventBadRole() throws Exception {
		long id = 2;
		mvc.perform(MockMvcRequestBuilders.delete("/events/{id}", id).accept(MediaType.TEXT_HTML)
			.with(user("Mustafa").roles("ATTENDEES")).with(csrf())).andExpect(status().isForbidden());
		
		verify(eventService, never()).deleteById(id);
	}
	
	@Test
	public void deleteEventNoCsrf() throws Exception {
		long id = 3;
		mvc.perform(MockMvcRequestBuilders.delete("/events/{id}", id).accept(MediaType.TEXT_HTML)
			.with(user("Mustafa").password("Mustafa").roles(Security.ORGANISER_ROLE)))
			.andExpect(status().isForbidden());

		verify(eventService, never()).deleteById(id);
	}
		
	@Test
	@WithMockUser(username="Organiser", roles= {"ORGANISER"})
	public void updateEventInvalid() throws Exception
	{
		when(eventService.findOne(0)).thenReturn(null);
		
		mvc.perform(MockMvcRequestBuilders.patch("/events/0").accept(MediaType.TEXT_HTML).with(csrf())
			.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
			.param("date", "2030-10-10")
			.param("time", "12:00")
			.param("name", "event")
			.sessionAttr("venue", venue)
			.param("description", "TEST"))
			.andExpect(status().isMethodNotAllowed());
	}
	
	@Test
	public void updateEventNoAuth() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post("/events").contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.param("name", "Event")
			.param("id", "10")
			.param("date", LocalDate.now().toString())
			.param("time", LocalTime.now().toString())
			.param("Venue_id", "10")
			.param("description", "This event is...")
			.accept(MediaType.TEXT_HTML).with(csrf())).andExpect(status().isFound())
			.andExpect(header().string("Location", endsWith("/sign-in")));

		verify(eventService, never()).save(event);
	}

	@Test
	public void updateEventBadRole() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post("/events").contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.with(user("Mustafa").roles("ATTENDEES"))
			.param("name", "Event")
			.param("id", "10")
			.param("date", LocalDate.now().toString())
			.param("time", LocalTime.now().toString())
			.param("Venue_id", "10")
			.param("description", "This event is...")
			.accept(MediaType.TEXT_HTML).with(csrf()))
			.andExpect(status().isForbidden());

		verify(eventService, never()).save(event);
	}	
	
	@Test
	public void updateEventNoCsrf() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post("/events").contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.with(user("Organiser").roles(Security.ORGANISER_ROLE))
			.param("name", "Event")
			.param("id", "10")
			.param("date", LocalDate.now().toString())
			.param("time", LocalTime.now().toString())
			.param("Venue_id", "10")
			.param("description", "This event is...")
			.accept(MediaType.TEXT_HTML))
			.andExpect(status().isForbidden());

		verify(eventService, never()).save(event);
	}
	
	@Test
	@WithMockUser(username="Organiser", roles= {"ORGANISER"})
	public void addEvent() throws Exception
	{	
		mvc.perform(post("/events/add").with(csrf())
	            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
	            .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
	                    new BasicNameValuePair("name", "Test Event 1"),
	                    new BasicNameValuePair("date", "2023-05-05"),
	                    new BasicNameValuePair("venue.id", "1"),
	                    new BasicNameValuePair("time", "12:30")
	            )))))
				.andExpect(view().name("redirect:/events"))
				.andExpect(status().isFound())
				.andExpect(handler().methodName("createEvent"));
	}
	
	@Test
	@WithMockUser(username="Organiser", roles= {"ORGANISER"})
	public void addEventInvalid() throws Exception
	{
		mvc.perform(post("/events/add").with(csrf())
	            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
	            .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
	                    new BasicNameValuePair("name", "Test Event 1"),
	                    new BasicNameValuePair("date", "2019-05-05"),
	                    new BasicNameValuePair("venue.id", "0"),
	                    new BasicNameValuePair("time", "89:90")
	            )))))
				.andExpect(view().name("events/add/index"))
				.andExpect(status().isOk())
				.andExpect(handler().methodName("createEvent"));
	}
	
	@Test
	@WithMockUser(username="Organiser", roles= {"ORGANISER"})
	public void addEventNodata() throws Exception
	{
		mvc.perform(post("/events/add").with(csrf())
	            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
	            .content(EntityUtils.toString(new UrlEncodedFormEntity(Arrays.asList(
	            )))))
				.andExpect(view().name("events/add/index"))
				.andExpect(status().isOk())
				.andExpect(handler().methodName("createEvent"));
	}

	public void postEventBadRole() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post("/events").with(user("Rob").roles("ATTENDEES"))
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.param("name", "Event")
			.param("id", "8")
			.param("date", (LocalDate.now().plus(1, ChronoUnit.DAYS)).toString())
			.param("time", LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")))
			.param("Venue_id", "88")
			.param("description", "This event is...")
			.accept(MediaType.TEXT_HTML).with(csrf()))
			.andExpect(status().isForbidden());

		verify(eventService, never()).save(event);
	}	
}
