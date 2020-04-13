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
import uk.ac.man.cs.eventlite.entities.UpdateVenue;
import uk.ac.man.cs.eventlite.entities.Venue;

@Controller
@RequestMapping(value = "/venues", produces = { MediaType.TEXT_HTML_VALUE })
public class VenuesController {

	private final static Logger log = LoggerFactory.getLogger(InitialDataLoader.class);
	
	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@RequestMapping(method = RequestMethod.GET)
	public String getAllEvents(Model model) {

		//model.addAttribute("events", eventService.findAll());
		model.addAttribute("venues", venueService.findAll());

		return "venues/index";
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public String getOneVenue(@PathVariable("id") long id,
		@RequestParam(value = "name", required = false, defaultValue = "World") String name, Model model) {

		Optional<Venue> venue = venueService.findVenueById(id);
		model.addAttribute("venue", venue);

		return "venues/details";
	}	

	@RequestMapping(value = "/update/{id}", method = RequestMethod.GET)
	public String getVenueToUpdate(Model model, @PathVariable Long id) {
		
		log.info("Request to open the UPDATE VENUE form page");

		Venue venue = venueService.findVenueById(id).get();
		UpdateVenue venueToBeUpdated = new UpdateVenue();
		venueToBeUpdated.setId(venue.getId());
		venueToBeUpdated.setName(venue.getName());
		
		venueToBeUpdated.setDescription(venue.getDescription());

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
		
	//@RequestMapping(value = "/manage/{id}", method = RequestMethod.POST)
	//public String updateVenue(@Valid Venue venue,
	        //BindingResult errors, Model model, RedirectAttributes redirectAttrs) {
	    //if (errors.hasErrors()) {
	        //redirectAttrs.addFlashAttribute("fail_message", "Please enter valid information.");
	        //model.addAttribute("venue", venue);
	        //return "redirect:/venues/manage/"+venue.getId();
	    //}
	    //venue.setCoords();
	    //try {
	        //Thread.sleep(1000L);
	    //} catch (InterruptedException e) {
	    // TODO Auto-generated catch block
	    //e.printStackTrace();
	    //}
	        //venueService.save(venue);
	       // redirectAttrs.addFlashAttribute("ok_message", "Venue updated.");

	       // return "redirect:/venues";
	   // }
		
		log.info("Request to save the changes to update a venue.");
		
		Venue venueToBeSaved = venueService.findVenueById(id).get();
		venueToBeSaved.setName(venue.getName());
		venueToBeSaved.setDescription(venue.getDescription());
		venueToBeSaved.setCapacity(venue.getCapacity());
		venueToBeSaved.setCoords(venue.getCoords());

		venueService.save(venueToBeSaved);
		redirectAttrs.addFlashAttribute("ok_message", "The venue has been successfully updated.");
		
		return "redirect:/venues";
	} // updateVenue
	
}
