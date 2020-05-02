package uk.ac.man.cs.eventlite.controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Collections;
import java.util.Optional;

import javax.servlet.Filter;

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
import org.springframework.web.bind.annotation.RequestParam;

import uk.ac.man.cs.eventlite.EventLite;
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

//	@Mock
//	private VenueService venueService;

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
	
	@WithMockUser(username="Organiser", roles= {"ORGANISER"})
	public void updateEvent() throws Exception
	{
		when(eventService.findOne(0)).thenReturn(event);
		
		mvc.perform(MockMvcRequestBuilders.patch("/events/0").accept(MediaType.TEXT_HTML).with(csrf())
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.param("name", "Test Event 1")
				.param("date", "2019-01-01")
				.param("time", "12:30")
				.param("description", "Test Event 1..."))
		.andExpect(status().isMethodNotAllowed());
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
	@WithMockUser(username="Organiser", roles= {"ORGANISER"})
	public void updateEventNoName() throws Exception
	{
		when(eventService.findOne(0)).thenReturn(null);
		
		mvc.perform(MockMvcRequestBuilders.patch("/events/0").accept(MediaType.TEXT_HTML).with(csrf())
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.param("date", "2030-10-10")
				.param("time", "12:00")
				.param("name", "")
				.sessionAttr("venue", venue)
				.param("description", "TEST"))
		.andExpect(status().isMethodNotAllowed());
	}
		
}
