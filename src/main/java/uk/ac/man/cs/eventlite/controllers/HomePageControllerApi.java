package uk.ac.man.cs.eventlite.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/api/index", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
public class HomePageControllerApi {

    @Autowired
    EventService eventService;

    @Autowired
    VenueService venueService;

    @Autowired
    EventsControllerApi eventsControllerApi;

    @Autowired
    VenuesControllerApi venuesControllerApi;

    private final int EventsNo = 3;
    
    private final int VenuesNo = 3;
    
    @GetMapping(value = "/threeUpComingEvents")
    public Resources<Resource<Event>> threeUpComingEvents() {
        Iterable<Event> allFutureEvents = eventService.findAllFutureEvents();
        List<Event> threeEvents = new ArrayList<>();
        int number = 0;
        for (Event e : allFutureEvents) {
            if (number >= EventsNo) 
            	break;

        	threeEvents.add(e);
        	number++;
        }
        return eventsControllerApi.eventToResource(threeEvents);
    }

    @GetMapping(value = "/threeVenuesWithMostEvents")
    public Resources<Resource<Venue>> threeVenuesWithMostEvents() {
        Iterable<Venue> sortedVenues = venueService.sortByNoOfEventsDesc();
        List<Venue> threeVenues = new ArrayList<>();
        int number = 0;
        for (Venue v : sortedVenues) {
            if (number >= VenuesNo) 
            	break;
          
            threeVenues.add(v);
        	number++;
        }
        return venuesControllerApi.venueToResource(threeVenues);
    }
}
