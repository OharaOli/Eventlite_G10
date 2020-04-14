package uk.ac.man.cs.eventlite.controllers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
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
public class VenueControllerTest {

	private MockMvc mvc;

	@Autowired
	private Filter springSecurityFilterChain;

	@Mock
	private Event event;

	@Mock
	private Venue venue;

//	@Mock
//	private EventService eventService;

	@Mock
	private VenueService venueService;

//	@InjectMocks
//	private EventsController eventsController;
	
	@InjectMocks
	private VenuesController venuesController;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mvc = MockMvcBuilders.standaloneSetup(venuesController).apply(springSecurity(springSecurityFilterChain))
				.build();
	}

	@Test
	public void getIndexWhenNoVenues() throws Exception {
//		when(eventService.findAll()).thenReturn(Collections.<Event> emptyList());
		when(venueService.findAll()).thenReturn(Collections.<Venue> emptyList());

		mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllVenues"));

//		verify(eventService).findAll();
		verify(venueService).findAll();
//		verifyZeroInteractions(event);
		verifyZeroInteractions(venue);
	}

	@Test
	public void getIndexWithVenues() throws Exception {
//		when(eventService.findAll()).thenReturn(Collections.<Event> singletonList(event));
		when(venueService.findAll()).thenReturn(Collections.<Venue> singletonList(venue));

		mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllVenues"));

//		verify(eventService).findAll();
		verify(venueService).findAll();
//		verifyZeroInteractions(event);
		verifyZeroInteractions(venue);
	}
	
	@Test
	public void getValidVenueDetails() throws Exception {
		Optional<Venue> testVenue = Optional.of(venue);
		Long id = (long)1;
		
		when(venueService.findVenueById(id)).thenReturn(testVenue);
		
		mvc.perform(get("/venues/1").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
		.andExpect(view().name("venues/details")).andExpect(handler().methodName("getOneVenue"));
		
		verify(venueService).findVenueById(id);
		verifyZeroInteractions(venueService);
		verifyZeroInteractions(venue);
	}
	
	@Test
	public void getInvalidVenueDetails() throws Exception {
		Optional<Venue> testVenue = Optional.<Venue>empty();
		Long id = (long)0;
		
		when(venueService.findVenueById(id)).thenReturn(testVenue);
		
		mvc.perform(get("/venues/0").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
		.andExpect(view().name("venues/details")).andExpect(handler().methodName("getOneVenue"));
		
		verify(venueService).findVenueById(id);
		verifyZeroInteractions(venueService);
		verifyZeroInteractions(venue);			
	}
	
	@Test
	@WithMockUser(username="admin", roles= {"ADMINISTRATOR"})
	public void updateVenue() throws Exception
	{
		when(venueService.findOne(0)).thenReturn(venue);
		
		mvc.perform(MockMvcRequestBuilders.patch("/venues/0").accept(MediaType.TEXT_HTML).with(csrf())
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.param("name", "Test Venue 1")
				.param("capacity", "100")
				.param("coordonates", "12M AL")
				.param("description", "Test Venue 1..."))
		.andExpect(status().isMethodNotAllowed());
	}
	
	@Test
	@WithMockUser(username="admin", roles= {"ADMINISTRATOR"})
	public void updateVenueInvalid() throws Exception
	{
		when(venueService.findOne(0)).thenReturn(null);
		
		mvc.perform(MockMvcRequestBuilders.patch("/venues/0").accept(MediaType.TEXT_HTML).with(csrf())
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.param("capacity", "200")
				.param("coordonates", "5A 7MN")
				.param("name", "venue")
				.sessionAttr("venue", venue)
				.param("description", "TEST"))
		.andExpect(status().isMethodNotAllowed());
	}
	
	@Test
	@WithMockUser(username="admin", roles= {"ADMINISTRATOR"})
	public void updateVenueNoName() throws Exception
	{
		when(venueService.findOne(0)).thenReturn(null);
		
		mvc.perform(MockMvcRequestBuilders.patch("/venues/0").accept(MediaType.TEXT_HTML).with(csrf())
				.contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.param("capacity", "300")
				.param("coordonates", "166 MHHS")
				.param("name", "Testing")
				.sessionAttr("venue", venue)
				.param("description", "TEST"))
		.andExpect(status().isMethodNotAllowed());
	}
		
}
