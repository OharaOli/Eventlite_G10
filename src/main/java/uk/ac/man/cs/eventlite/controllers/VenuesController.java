package uk.ac.man.cs.eventlite.controllers;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Venue;

@Controller
@RequestMapping(value = "/venues", produces = { MediaType.TEXT_HTML_VALUE })
public class VenuesController {

	@Autowired
	private VenueService eventService;
	
	@Autowired
	private VenueService venueService;
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public String getOneVenue(@PathVariable("id") long id,
		@RequestParam(value = "name", required = false, defaultValue = "World") String name, Model model) {

		Optional<Venue> venue = venueService.findVenueById(id);
		model.addAttribute("venue", venue);
		
		if (venue.isPresent()) {
			model.addAttribute("events", eventService);
		}
		

		return "venues/details";
	}	
	
}
