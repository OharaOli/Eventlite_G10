package uk.ac.man.cs.eventlite.controllers;

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
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.UpdateEvent;

@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

	private final static Logger log = LoggerFactory.getLogger(InitialDataLoader.class);
	
	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@RequestMapping(method = RequestMethod.GET)
	public String getAllEvents(Model model) {

		model.addAttribute("events", eventService.findAll());
//		model.addAttribute("venues", venueService.findAll());

		return "events/index";
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public String getOneEvent(@PathVariable("id") long id,
		@RequestParam(value = "name", required = false, defaultValue = "World") String name, Model model) {

		Optional<Event> event = eventService.findEventById(id);
		model.addAttribute("event", event);

		return "events/details";
	}	

	@RequestMapping(value = "/update/{id}", method = RequestMethod.GET)
	public String getEventToUpdate(Model model, @PathVariable Long id) {
		
		log.info("Request to open the UPDATE EVENT form page");

		Event event = eventService.findEventById(id).get();
		UpdateEvent eventToBeUpdated = new UpdateEvent();
		eventToBeUpdated.setId(event.getId());
		eventToBeUpdated.setName(event.getName());
		eventToBeUpdated.setDate(event.getDate());
		eventToBeUpdated.setTime(event.getTime());
		eventToBeUpdated.setVenueId(event.getVenue().getId());
		eventToBeUpdated.setDescription(event.getDescription());

		model.addAttribute("event", eventToBeUpdated);
		model.addAttribute("venues", venueService.findAll());
		
		return "events/update";
	} // getEventToUpdate

	@RequestMapping(value = "/update/{id}", method = RequestMethod.PUT)
	public String updateEvent(@RequestBody @Valid @ModelAttribute ("event") UpdateEvent event,
			BindingResult errors,@PathVariable Long id, Model model, RedirectAttributes redirectAttrs) {
		
		if (errors.hasErrors()) {
			model.addAttribute("event", event);
			model.addAttribute("venues", venueService.findAll());
			return "events/update";
		}
		
		log.info("Request to save the changes to update an event.");
		
		Event eventToBeSaved = eventService.findEventById(id).get();
		eventToBeSaved.setName(event.getName());
		eventToBeSaved.setDate(event.getDate());
		eventToBeSaved.setTime(event.getTime());
		eventToBeSaved.setVenue(venueService.findVenueById(event.getVenueId()).get());
		eventToBeSaved.setDescription(event.getDescription());

		eventService.save(eventToBeSaved);
		redirectAttrs.addFlashAttribute("ok_message", "The event has been successfully updated.");
		
		return "redirect:/events";
	} // updateEvent
	
}
