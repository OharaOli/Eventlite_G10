package uk.ac.man.cs.eventlite.controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.entities.Event;


@Controller
@RequestMapping(value = "/venues", produces = { MediaType.TEXT_HTML_VALUE })
public class VenuesController {

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
	
	@RequestMapping(value = "/add",method = RequestMethod.GET)
	public String getAddEvents(Model model) {
	  model.addAttribute("venue", new Venue());

	  return "venues/add/index" ;
	}
	
	@RequestMapping(value = "/delete_venue", method = RequestMethod.GET)
	public String deleteVenue(@RequestParam(name="venueId") Long id) {

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
	
	@RequestMapping(value="/add",method = RequestMethod.POST)
	public String createVenue(@RequestBody @Valid @ModelAttribute ("venue") Venue venue, 
			BindingResult errors, Model model, RedirectAttributes redirectAttrs) {
	    if (errors.hasErrors()) {
	    	model.addAttribute("venue", venue);
	        return "venues/add/index";
	    }
	    this.venueService.save(venue);
	    return "redirect:/events";
	}

	
}
