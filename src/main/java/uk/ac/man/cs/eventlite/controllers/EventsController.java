package uk.ac.man.cs.eventlite.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;

@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@RequestMapping(method = RequestMethod.GET)
	public String getAllEvents(Model model) {

		model.addAttribute("events", eventService.findAll());
		//model.addAttribute("venues", venueService.findAll());

		return "events/index";
	}
	
	@RequestMapping(value = "/add",method = RequestMethod.GET)
	public String getAddEvents(Model model) {

		model.addAttribute("event", new Event());
		model.addAttribute("venues", venueService.findAll());

		return "events/add/index";
	}
	
	@RequestMapping(value="/add",method = RequestMethod.POST)
	public String saveNewEvent(final Event event, final BindingResult bindingResult, final ModelMap model) {
	    if (bindingResult.hasErrors()) {
	        return "add";
	    }
	    this.eventService.save(event);
	    model.clear();
	    return "redirect:/events";
	}

}