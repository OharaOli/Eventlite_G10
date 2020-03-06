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

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.entities.Event;

@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

	@Autowired
	private EventService eventService;

	//@Autowired
	//private VenueService venueService;

	@RequestMapping(method = RequestMethod.GET)
	public String getAllEvents(Model model) {
		model.addAttribute("prevEvents", eventService.findAllPreviousEvents());
		model.addAttribute("futureEvents", eventService.findAllFutureEvents());
		return "events/index";
	}

	@RequestMapping(value="/search", method = RequestMethod.GET)
	public String getEventsByName(@RequestParam(value = "search") String eventSearch, Model model)
	{
		if(eventSearch == null || eventSearch.isEmpty())
			return "redirect:/events";
		Iterable<Event> eventResultsFuture = eventService.findFutureSearchedBy(eventSearch);
		Iterable<Event> eventResultsPast = eventService.findPastSearchedBy(eventSearch);
		model.addAttribute("eventResultsFuture", eventResultsFuture);
		model.addAttribute("eventResultsPast", eventResultsPast);
		return "events/search";
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public String getOneEvent(@PathVariable("id") long id,
		@RequestParam(value = "name", required = false, defaultValue = "World") String name, Model model) {

		Optional<Event> event = eventService.findEventById(id);
		model.addAttribute("event", event);

		return "events/details";
	}	
	
}
