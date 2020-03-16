package uk.ac.man.cs.eventlite.controllers;

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

import java.util.Collections;
import java.util.Optional;
import javax.servlet.Filter;

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

//    @Test
//    public void getIndexWhenNoVenues() throws Exception {
//        when(venueService.findAll()).thenReturn(Collections.emptyList());
//        mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
//                .andExpect(view().name("venues/index")).andExpect(handler().methodName("findAll"));
//
//        verify(venueService).findAll();
//        verifyZeroInteractions(venue);
//    }
//
//    @Test
//    public void getIndexWithVenues() throws Exception {
//        when(venueService.findAll()).thenReturn(Collections.singletonList(venue));
//
//        mvc.perform(get("/venues").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
//                .andExpect(view().name("venues/index")).andExpect(handler().methodName("findAll"));
//
//        verify(venueService).findAll();
//        verifyZeroInteractions(venue);
//    }

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

}
