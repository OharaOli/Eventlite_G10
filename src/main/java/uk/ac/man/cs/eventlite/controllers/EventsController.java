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
	
	@RequestMapping(value = "/update/{id}", method = RequestMethod.GET)
	public String getUpdateEvent(Model model, @PathVariable("id") long id)
	{
		if (!model.containsAttribute("event")) {
			
			model.addAttribute("event", eventService.findOne(id));
		}
		
		if (!model.containsAttribute("venueList")) {
			model.addAttribute("venueList", venueService.findAll());
		}

		return "events/update";
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.PATCH, 
			consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String updateEvent(@RequestBody @Valid @ModelAttribute Event event,
			BindingResult errors,  Model model, @PathVariable("id") long id, 
			RedirectAttributes redirectAttrs)
	{
		if (errors.hasErrors()) {
			model.addAttribute("event", event);
			model.addAttribute("venueList", venueService.findAll());
			
			return "events/update";
		}
		
		Event eventToUpdate = eventService.findOne(id);
		
		// Redirect if invalid event
		if (eventToUpdate == null)
		{
			redirectAttrs.addFlashAttribute("unsuccessful", "Event does not exist");
			return "redirect:/events";
		}
		
		// Update the values of the event
		eventToUpdate.setName(event.getName());
		eventToUpdate.setDate(event.getDate());
		eventToUpdate.setDescription(event.getDescription());
		eventToUpdate.setTime(event.getTime());
		eventToUpdate.setVenue(event.getVenue());
		eventService.update(eventToUpdate);
		
		model.addAttribute("eventName", eventService.findOne(id).getName());
		model.addAttribute("eventDate", eventService.findOne(id).getDate());
		model.addAttribute("eventTime", eventService.findOne(id).getTime());
		model.addAttribute("eventVenue",
				eventService.findOne(id).getVenue() == null ? "" : eventService.findOne(id).getVenue().getName());
		model.addAttribute("eventDescription", eventService.findOne(id).getDescription());
		model.addAttribute("venues", venueService.findAll());
		
		return "events/show";
	}

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









