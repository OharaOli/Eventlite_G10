package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.ac.man.cs.eventlite.testutil.MessageConverterUtil.getMessageConverters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;


import static org.hamcrest.Matchers.*;

import java.time.LocalDate;
import java.time.LocalTime;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import uk.ac.man.cs.eventlite.EventLite;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.annotation.RequestParam;
import uk.ac.man.cs.eventlite.dao.VenueService;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class EventsControllerApiTest {

	private MockMvc mvc;

	@Autowired
	private Filter springSecurityFilterChain;

	@Mock
	private EventService eventService;

	@InjectMocks
	private EventsControllerApi eventsController;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mvc = MockMvcBuilders.standaloneSetup(eventsController).apply(springSecurity(springSecurityFilterChain))
				.setMessageConverters(getMessageConverters()).build();
	}

	@Test
	public void getIndexWhenNoEvents() throws Exception {
		when(eventService.findAll()).thenReturn(Collections.<Event> emptyList());

		mvc.perform(get("/api/events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getAllEvents")).andExpect(jsonPath("$.length()", equalTo(1)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/events")));

		verify(eventService).findAll();
	}

	@Test
	public void getIndexWithEvents() throws Exception {
		Event e = new Event();
		Venue v = new Venue();
		e.setId(0);
		e.setName("Event");
		e.setDate(LocalDate.now());
		e.setTime(LocalTime.now());
		e.setVenue(v);
		when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(e));

		mvc.perform(get("/api/events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getAllEvents")).andExpect(jsonPath("$.length()", equalTo(2)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/events")))
				.andExpect(jsonPath("$._embedded.events.length()", equalTo(1)))
				.andExpect(jsonPath("$._embedded.events[0]._links.venue.href", not(empty())))
				.andExpect(jsonPath("$._embedded.events[0]._links.venue.href", endsWith("events/0/venue")));

		

		verify(eventService).findAll();
	}
	
	
	@Test
	public void getIndexWithOneEvent() throws Exception {
		Event e = new Event();
		Venue v = new Venue();
		Long id = (long) 0;
		e.setId(id);
		e.setName("Event");
		e.setDate(LocalDate.of(2020, 5, 2));
		e.setTime(LocalTime.of(17, 30));
		e.setVenue(v);
		when(eventService.findEventById(e.getId())).thenReturn(Optional.of(e));
		
		mvc.perform(get("/api/events/0").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
		.andExpect(handler().methodName("getOneEvent"))
		.andExpect(jsonPath("$._links.self.href", endsWith("/api/events/0")))
		.andExpect(jsonPath("$._links.venue.href",  endsWith("events/0/venue")))
		.andExpect(jsonPath("$._links.event.href",  endsWith("events/0")))
		.andExpect(jsonPath("$.date", equalTo("2020-05-02")))
		.andExpect(jsonPath("$.time", equalTo("17:30:00")))
		.andExpect(jsonPath("$.name", equalTo("Event")))
		.andExpect(jsonPath("$.formattedDate", empty()));
		


		

		verify(eventService).findEventById(id);
	}
	
	@Test
	public void getSearchIndexWhenNoEvents() throws Exception {
		String testSearch = new String("HHH");
		when(eventService.findBySearchedBy(testSearch)).thenReturn(Collections.<Event> emptyList());

		mvc.perform(get("/api/events/search?search=HHH").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getEventsByName")).andExpect(jsonPath("$.length()", equalTo(1)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/events")));

		verify(eventService).findBySearchedBy(testSearch);
	}

	@Test
	public void getSearchIndexWithEvents() throws Exception {
		String testSearch = new String("event");
		Event e = new Event();
		Venue v = new Venue();
		e.setId(0);
		e.setName("My Test Event");
		e.setDate(LocalDate.now());
		e.setTime(LocalTime.now());
		e.setVenue(v);
		when(eventService.findBySearchedBy(testSearch)).thenReturn(Collections.<Event>singletonList(e));

		mvc.perform(get("/api/events/search?search=event").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(handler().methodName("getEventsByName")).andExpect(jsonPath("$.length()", equalTo(2)))
				.andExpect(jsonPath("$._links.self.href", endsWith("/api/events")))
				.andExpect(jsonPath("$._embedded.events.length()", equalTo(1)));

		verify(eventService).findBySearchedBy(testSearch);
	}
}
