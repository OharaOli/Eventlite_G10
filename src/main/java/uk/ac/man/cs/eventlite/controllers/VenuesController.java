package uk.ac.man.cs.eventlite.controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import uk.ac.man.cs.eventlite.config.data.InitialDataLoader;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.UpdateEvent;
import uk.ac.man.cs.eventlite.entities.UpdateVenue;

@Controller
@RequestMapping(value = "/venues", produces = { MediaType.TEXT_HTML_VALUE })
public class VenuesController {

	private final static Logger log = LoggerFactory.getLogger(InitialDataLoader.class);
	
	@Autowired
	private EventService eventService;
	
	@Autowired
	private VenueService venueService;

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

	@RequestMapping(value = "/add",method = RequestMethod.GET)
	public String getAddEvents(Model model) {
	  model.addAttribute("venue", new Venue());

	  return "venues/add/index" ;
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
			
			Iterator<Event> eventIterator = eventService.findAll().iterator() ;
			List<Event> events = new ArrayList<Event>() ;
			
			while (eventIterator.hasNext())
				events.add(eventIterator.next()) ;

			// Go through every event in DB. If an event is held in the venue we are trying
			// to delete, then do not delete.
			for (Event event : events)
			{
				if (event.getVenue().getId() == id)
					delete = false ;
			}

			if (delete)
				venueService.deleteById(id) ;
		}
		
		return "redirect:/venues" ;
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

	@RequestMapping(value = "/update/{id}", method = RequestMethod.GET)
	public String getVenueToUpdate(Model model, @PathVariable Long id) {
		
		log.info("Request to open the UPDATE VENUE form page");

		Venue venue = venueService.findVenueById(id).get();
		UpdateVenue venueToBeUpdated = new UpdateVenue();
		venueToBeUpdated.setId(venue.getId());
		venueToBeUpdated.setName(venue.getName());
		venueToBeUpdated.setDescription(venue.getDescription());
		venueToBeUpdated.setCoordonates(venue.getCoordonates());
		log.info("Request to open the UPDATE VENUE form page " + venue.getDescription() + " " + venue.getCoordonates() + "\\\\");

		model.addAttribute("venue", venueToBeUpdated);
		model.addAttribute("venues", venueService.findAll());
		
		return "venues/update";
	} // getvenueToUpdate

	@RequestMapping(value = "/update/{id}", method = RequestMethod.PUT)
	public String updateVenue(@RequestBody @Valid @ModelAttribute ("venue") UpdateVenue venue,
			BindingResult errors,@PathVariable Long id, Model model, RedirectAttributes redirectAttrs) {
		
		if (errors.hasErrors()) {
			model.addAttribute("venue", venue);
			model.addAttribute("venues", venueService.findAll());
			return "venues/update";
		}
		
		log.info("Request to save the changes to update a venue.");
		
		Venue venueToBeSaved = venueService.findVenueById(id).get();
		venueToBeSaved.setName(venue.getName());
		venueToBeSaved.setDescription(venue.getDescription());
		venueToBeSaved.setCapacity(venue.getCapacity());
		venueToBeSaved.setCoordonates(venue.getCoordonates());
		log.info("Request to save the changes to update a venue. " + venue.getCapacity() + " " + venue.getCoordonates() + "////");

		venueService.save(venueToBeSaved);
		redirectAttrs.addFlashAttribute("ok_message", "The venue has been successfully updated.");
		
		return "redirect:/venues";
	} // updateVenue
	
}
