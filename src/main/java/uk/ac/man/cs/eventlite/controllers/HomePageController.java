package uk.ac.man.cs.eventlite.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value = "/index", produces = { MediaType.TEXT_HTML_VALUE })
public class HomePageController {

    @Autowired
    private EventService eventService;

    @Autowired
    private VenueService venueService;

    private final int EventsNo = 3;
    
    private final int VenuesNo = 3;

    @GetMapping
    public String initHomePage(Model model) {
    	
        Iterable<Event> allFutureEvents = eventService.findAllFutureEvents();
        List<Event> threeEvents = new ArrayList<>();
        int number = 0;
        for (Event e : allFutureEvents) {
        	if (number >= EventsNo)
        		break;
        	
        	threeEvents.add(e);
	        number++;            
        }
        model.addAttribute("threeEvents", threeEvents);

        Iterable<Venue> sortedVenues = venueService.sortByNoOfEventsDesc();
        List<Venue> threeVenues = new ArrayList<>();
        number = 0;
        for (Venue v : sortedVenues) {
        	if (number >= VenuesNo)
        		break;
        	
        	threeVenues.add(v);
	        number++;
        }
        model.addAttribute("threeVenues", threeVenues);
        
        return "/index";
    }
}
