package uk.ac.man.cs.eventlite.controllers;

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

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

        Mockito.when(venueService.findVenueById(id)).thenReturn(testVenue);

        mvc.perform(MockMvcRequestBuilders.get("/venues/1").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
                .andExpect(view().name("venues/details")).andExpect(handler().methodName("getOneVenue"));

        verify(venueService).findVenueById(id);
        verifyZeroInteractions(eventService);
        verifyZeroInteractions(event);
    }
   
	@Test
	public void deleteExistingVenue() throws Exception {
		Long id = (long) 1 ;
		Event testEvent = new Event() ;
		Event testFutureEvent = new Event() ;
		Optional<Venue> testVenue = Optional.of(venue) ;
	
		List<Event> futureEventsList = new ArrayList<Event>() ;
		futureEventsList.add(testFutureEvent) ;
		Iterator<Event> futureEventsIterator = futureEventsList.iterator() ;
		Iterable<Event> futureEvents = () -> futureEventsIterator ;
		
		List<Event> eventsList = new ArrayList<Event>() ;
		eventsList.add(event) ;
		Iterator<Event> eventsIterator = eventsList.iterator() ;
		Iterable<Event> events = () -> eventsIterator ;
		
		when(eventService.findAllFutureEvents()).thenReturn(futureEvents) ;
		when(eventService.findAll()).thenReturn(events) ;
		when(venueService.findVenueById(id)).thenReturn(testVenue);
		when(venue.getId()).thenReturn(id);
		when(event.getVenue()).thenReturn(venue);
		doNothing().when(event).setVenue(venue);
		doNothing().when(venueService).deleteById(id);
		
		mvc.perform(get("/venues/delete_venue?venueId=1").accept(MediaType.TEXT_HTML)).andExpect(status().isFound())
		.andExpect(view().name("redirect:/events")).andExpect(handler().methodName("deleteVenue"));

		verify(eventService).findAllFutureEvents();
		verify(eventService).findAll();
		verify(venueService, times(2)).findVenueById(id);
		verify(venueService).deleteById(id);
		verify(venue, times(2)).getId();
		verify(event, times(2)).getVenue();
		verify(event).setVenue(venue);
		verifyZeroInteractions(venueService);
		verifyZeroInteractions(venue);
		verifyZeroInteractions(event);
		verifyZeroInteractions(eventService);
	}
	
	@Test
	public void deleteExistingVenueWithFutureEvent() throws Exception {
		Long id = (long) 1 ;
		Optional<Venue> testVenue = Optional.of(venue) ;
	
		List<Event> futureEventsList = new ArrayList<Event>() ;
		futureEventsList.add(event) ;
		Iterator<Event> futureEventsIterator = futureEventsList.iterator() ;
		Iterable<Event> futureEvents = () -> futureEventsIterator ;
		
		List<Event> eventsList = new ArrayList<Event>() ;
		eventsList.add(event) ;
		Iterator<Event> eventsIterator = eventsList.iterator() ;
		Iterable<Event> events = () -> eventsIterator ;
		
		when(eventService.findAllFutureEvents()).thenReturn(futureEvents) ;
		when(eventService.findAll()).thenReturn(events) ;
		when(venueService.findVenueById(id)).thenReturn(testVenue);
		when(venue.getId()).thenReturn(id);
		when(event.getVenue()).thenReturn(venue);
		
		mvc.perform(get("/venues/delete_venue?venueId=1").accept(MediaType.TEXT_HTML)).andExpect(status().isFound())
		.andExpect(view().name("redirect:/events")).andExpect(handler().methodName("deleteVenue"));

		verify(eventService).findAllFutureEvents();
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
		.andExpect(view().name("redirect:/events")).andExpect(handler().methodName("deleteVenue"));

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
	public void getIndexWithVenuess() throws Exception {
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
	
}
