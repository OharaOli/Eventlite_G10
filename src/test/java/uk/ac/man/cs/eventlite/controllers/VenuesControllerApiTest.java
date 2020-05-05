package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.ac.man.cs.eventlite.testutil.MessageConverterUtil.getMessageConverters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = EventLite.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VenuesControllerApiTest {

	private MockMvc mvc;

	@Autowired
	private Filter springSecurityFilterChain;

	@Mock
	private EventService eventService;
	
	@Mock
	private VenueService venueService;

	@InjectMocks
	private VenuesControllerApi venuesController;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mvc = MockMvcBuilders.standaloneSetup(venuesController).apply(springSecurity(springSecurityFilterChain))
				.setMessageConverters(getMessageConverters()).build();
	}

	@Test
	public void getIndexWhenNoVenues() throws Exception {
		when(venueService.findAll()).thenReturn(Collections.<Venue> emptyList());

		mvc.perform(get("/api/venues").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
			.andExpect(handler().methodName("getAllVenues")).andExpect(jsonPath("$.length()", equalTo(1)))
			.andExpect(jsonPath("$._links.self.href", endsWith("/api/venues")));

		verify(venueService).findAll();
	}
	
	@Test
	 public void getIndexWithVenues() throws Exception {
		Venue v = new Venue();
		v.setId(0);
	  
		when(venueService.findAll()).thenReturn(Collections.<Venue>singletonList(v));

		mvc.perform(get("/api/venues").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
			.andExpect(handler().methodName("getAllVenues")).andExpect(jsonPath("$.length()", equalTo(2)))
			.andExpect(jsonPath("$._links.self.href", endsWith("/api/venues")))
			.andExpect(jsonPath("$._links.profile.href", endsWith("/api/profile/venues")))
			.andExpect(jsonPath("$._embedded.venues.length()", equalTo(1)))
			.andExpect(jsonPath("$._embedded.venues[0]._links.venue.href", endsWith("venues/0")))
			.andExpect(jsonPath("$._embedded.venues[0]._links.events.href", endsWith("venues/0/events")))
			.andExpect(jsonPath("$._embedded.venues[0]._links.next3events.href", endsWith("venues/0/next3events")));

		verify(venueService).findAll();
	 }	
	
	@Test
	public void getSearchIndexWhenNoVenues() throws Exception {
		String testSearch = new String("HHHHHHHHHHH");
		when(venueService.findSearchedBy(testSearch)).thenReturn(Collections.<Venue> emptyList());

		mvc.perform(get("/api/venues/search?search=HHHHHHHHHHH").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
			.andExpect(handler().methodName("getVenuesByName")).andExpect(jsonPath("$.length()", equalTo(1)))
			.andExpect(jsonPath("$._links.self.href", endsWith("/api/venues")));

		verify(venueService).findSearchedBy(testSearch);
	}

	@Test
	public void getSearchIndexWithVenues() throws Exception {
		String testSearch = new String("venue");
		Venue v = new Venue();
		v.setId(0);
		v.setAddress("test address");
		v.setCapacity(1);
		v.setName("test venue");
		v.setPostcode("test postcode");
		when(venueService.findSearchedBy(testSearch)).thenReturn(Collections.<Venue>singletonList(v));

		mvc.perform(get("/api/venues/search?search=venue").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
			.andExpect(handler().methodName("getVenuesByName")).andExpect(jsonPath("$.length()", equalTo(2)))
			.andExpect(jsonPath("$._links.self.href", endsWith("/api/venues")))
			.andExpect(jsonPath("$._embedded.venues.length()", equalTo(1)));

		verify(venueService).findSearchedBy(testSearch);
	}
	
	@Test
	public void getNext3EventsOfVenue() throws Exception {
		long v_id = 1;
		
		Venue v = new Venue();
		v.setId(v_id);
		v.setName("test venue");
		
		Event e = new Event();
		e.setName("test event");
		e.setVenue(v);
		
		when(venueService.findOne(v_id)).thenReturn(v);
		when(eventService.findFirst3UpcomingEventsByVenue(v)).thenReturn(List.of(e));
		
		mvc.perform(get("/api/venues/{id}/next3events", v_id).accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
			.andExpect(handler().methodName("getNext3Events"))
			.andExpect(jsonPath("$._embedded.events[0].name", equalTo("test event")))
			.andExpect(jsonPath("$._embedded.events.length()", equalTo(1)))
			.andExpect(jsonPath("$._links.self.href", endsWith("/venues/" + v_id + "/next3events")));
	}

	@Test
	public void getAVenue() throws Exception {
		long id = 10;
		
		Venue v = new Venue();
		v.setId(id);
		v.setName("Test Venue");
		v.setAddress("Test Road");
		v.setPostcode("Test Postcode");
		v.setCapacity(50);
		
		when(venueService.findOne(id)).thenReturn(v);
		
		Event e = new Event();
		e.setId(11);
		e.setVenue(v);

		mvc.perform(get("/api/venues/{id}", id).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(handler().methodName("getOneVenue"))
				.andExpect(jsonPath("$.name", equalTo("Test Venue")))
				.andExpect(jsonPath("$._links.self.href", endsWith("/venues/" + id)))
				.andExpect(jsonPath("$._links.venue.href", endsWith("/venues/" + id)))
				.andExpect(jsonPath("$._links.events.href", endsWith("/events")));
		verify(venueService).findOne(id);
	}	
	
}
