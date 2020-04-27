package uk.ac.man.cs.eventlite.controllers;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.servlet.Filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class VenuesControllerTest {

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
    private VenuesController venuesController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mvc = MockMvcBuilders.standaloneSetup(venuesController).apply(springSecurity(springSecurityFilterChain))
                .build();
    }
 
    @Test
    public void getValidVenueDetails() throws Exception {
        Optional<Venue> testVenue = Optional.of(venue);
        Long id = (long)1;

        when(venueService.findVenueById(id)).thenReturn(testVenue);

        mvc.perform(get("/venues/1").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
                .andExpect(view().name("venues/details")).andExpect(handler().methodName("getOneVenue"));

        verify(venueService).findVenueById(id);
        verifyZeroInteractions(eventService);
        verifyZeroInteractions(event);
    }
   
	@Test
	public void deleteExistingVenue() throws Exception {
		Long id = (long) 1 ;
		Optional<Venue> testVenue = Optional.of(venue) ;
		Venue otherVenue = new Venue() ;
		
		List<Event> eventsList = new ArrayList<Event>() ;
		eventsList.add(event) ;
		Iterator<Event> eventsIterator = eventsList.iterator() ;
		Iterable<Event> events = () -> eventsIterator ;
		
		when(eventService.findAll()).thenReturn(events) ;
		when(venueService.findVenueById(id)).thenReturn(testVenue);
		when(venue.getId()).thenReturn((long) 2);
		when(event.getVenue()).thenReturn(venue);
		doNothing().when(venueService).deleteById(id);
		
		mvc.perform(get("/venues/delete_venue?venueId=1").accept(MediaType.TEXT_HTML)).andExpect(status().isFound())
		.andExpect(view().name("redirect:/venues")).andExpect(handler().methodName("deleteVenue"));

		verify(eventService).findAll();
		verify(venueService).findVenueById(id);
		verify(venueService).deleteById(id);
		verify(venue).getId();
		verify(event).getVenue();
		verifyZeroInteractions(venueService);
		verifyZeroInteractions(venue);
		verifyZeroInteractions(event);
		verifyZeroInteractions(eventService);
	}
	
	@Test
	public void deleteExistingVenueWithEvent() throws Exception {
		Long id = (long) 1 ;
		Optional<Venue> testVenue = Optional.of(venue) ;
		
		List<Event> eventsList = new ArrayList<Event>() ;
		eventsList.add(event) ;
		Iterator<Event> eventsIterator = eventsList.iterator() ;
		Iterable<Event> events = () -> eventsIterator ;
		
		when(eventService.findAll()).thenReturn(events) ;
		when(venueService.findVenueById(id)).thenReturn(testVenue);
		when(venue.getId()).thenReturn(id);
		when(event.getVenue()).thenReturn(venue);
		
		mvc.perform(get("/venues/delete_venue?venueId=1").accept(MediaType.TEXT_HTML)).andExpect(status().isFound())
		.andExpect(view().name("redirect:/venues")).andExpect(handler().methodName("deleteVenue"));

		verify(eventService).findAll();
		verify(venueService).findVenueById(id);
		verify(venue).getId();
		verify(event).getVenue();
		verifyZeroInteractions(venueService);
		verifyZeroInteractions(venue);
		verifyZeroInteractions(event);
		verifyZeroInteractions(eventService);
	}
	
	@Test
	public void deleteNonExistentVenue() throws Exception {
		Long id = (long) 1 ;
		Optional<Venue> testVenue = Optional.<Venue>empty() ;

		when(venueService.findVenueById(id)).thenReturn(testVenue);
		
		mvc.perform(get("/venues/delete_venue?venueId=1").accept(MediaType.TEXT_HTML)).andExpect(status().isFound())
		.andExpect(view().name("redirect:/venues")).andExpect(handler().methodName("deleteVenue"));

		verify(venueService).findVenueById(id);
		verifyZeroInteractions(venueService);
	}
	
	@Test
	public void getIndexWhenNoVenues() throws Exception {
		//when(eventService.findAll()).thenReturn(Collections.<Event> emptyList());
		when(venueService.findAll()).thenReturn(Collections.<Venue> emptyList());

		mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllVenues"));

		//verify(eventService).findAllPreviousEvents();
		//verify(eventService).findAllFutureEvents();
		verify(venueService).findAll();
		verifyZeroInteractions(venue);
//		verifyZeroInteractions(event);
	}

	@Test
	public void getIndexWithVenues() throws Exception {
		//when(eventService.findAll()).thenReturn(Collections.<Event> singletonList(event));
		when(venueService.findAll()).thenReturn(Collections.<Venue> singletonList(venue));

		mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("venues/index")).andExpect(handler().methodName("getAllVenues"));

		//verify(eventService).findAll();
		//verify(eventService).findAllPreviousEvents();
		//verify(eventService).findAllFutureEvents();
		verify(venueService).findAll();
		//verifyZeroInteractions(event);
		verifyZeroInteractions(venue);
	}
	
	@Test
	public void getSearchIndexWhenNoVenues() throws Exception {
		String testSearch = new String("HHHHHHHH");
		when(venueService.findSearchedBy(testSearch)).thenReturn(Collections.<Venue> emptyList());

		mvc.perform(get("/venues/search?search=HHHHHHHH").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("venues/search")).andExpect(handler().methodName("getVenuesByName"));

		verify(venueService).findSearchedBy(testSearch);
		verifyZeroInteractions(venue);
	}

	@Test
	public void getSearchIndexWithVenues() throws Exception {
		String venueSearch = new String("Venue");
		when(venueService.findSearchedBy(venueSearch)).thenReturn(Collections.<Venue> singletonList(venue));

		mvc.perform(get("/venues/search?search=Venue").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("venues/search")).andExpect(handler().methodName("getVenuesByName"));

		verify(venueService).findSearchedBy(venueSearch);
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
				.param("id", "0")
				.param("address", "address")
				.param("postcode", "postcode")
				.param("capacity", "300")
				.sessionAttr("venue", venue)
				.param("description", "TEST"))
				.andExpect(status().isMethodNotAllowed());
	}
	
}
