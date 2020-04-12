package uk.ac.man.cs.eventlite.controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import uk.ac.man.cs.eventlite.config.data.InitialDataLoader;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.entities.Event;

@Controller
@RequestMapping(value = "/venues", produces = { MediaType.TEXT_HTML_VALUE })
public class VenuesController {

	private final static Logger log = LoggerFactory.getLogger(InitialDataLoader.class);
	
	@Autowired
	private VenueService venueService;

	@Autowired
	private EventService eventService;

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public String getOneVenue(@PathVariable("id") long id,
		@RequestParam(value = "name", required = false, defaultValue = "World") String name, Model model) {

		Optional<Venue> venue = venueService.findVenueById(id);
		model.addAttribute("venue", venue);
		
		if (venue.isPresent()) {
			model.addAttribute("futureEvents", venueService.findUpcomingEvents(id));
		}

		return "venues/details";
	}	
	
	@RequestMapping(method = RequestMethod.GET)
	public String getAllVenues(Model model) {
		
		model.addAttribute("allVenues", venueService.findAll());

		return "venues/index";
	}

	@RequestMapping(value="/search", method = RequestMethod.GET)
	public String getVenuesByName(@RequestParam(value = "search") String venueSearch, Model model)
	{
		if(venueSearch == null || venueSearch.isEmpty())
			return "redirect:/venues";
		Iterable<Venue> venueResults = venueService.findSearchedBy(venueSearch);
		model.addAttribute("venueResults", venueResults);
		return "venues/search";
	}
	
	@RequestMapping(value = "/delete_venue", method = RequestMethod.GET)
	public String deleteVenue(@RequestParam(name="venueId") Long id) 
	{
		if (venueService.findVenueById(id).isPresent())
		{
			// Initialize variables
			boolean delete = true ;
			
			Iterator<Event> futureEventsIterator = eventService.findAllFutureEvents().iterator() ;
			List<Event> futureEvents = new ArrayList<Event>() ;
			
			// Convert Iterator to List so contains function could be used
			while (futureEventsIterator.hasNext())
				futureEvents.add(futureEventsIterator.next()) ;
			
			Iterator<Event> eventIterator = eventService.findAll().iterator() ;
			List<Event> events = new ArrayList<Event>() ;
			
			while (eventIterator.hasNext())
				events.add(eventIterator.next()) ;

			// Go through every event in DB. If a future event is held in the venue we are trying
			// to delete, then do not delete.
			for (Event event : events)
			{
				if (event.getVenue().getId() == id)
					if (futureEvents.contains(event))
						delete = false ;
			}

			if (delete)
			{
				for (Event event : events)
				{
					if (event.getVenue().getId() == id)
						event.setVenue(venueService.findVenueById((long)1).get()) ;
				}
			
				venueService.deleteById(id) ;
			}
			
		}
		
		return "redirect:/events" ;
	}
}