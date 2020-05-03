package uk.ac.man.cs.eventlite.controllers;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@RestController
@RequestMapping(value = "/api/venues", produces = { MediaType.APPLICATION_JSON_VALUE, MediaTypes.HAL_JSON_VALUE })
public class VenuesControllerApi {

	private final static Logger log = LoggerFactory.getLogger(VenuesControllerApi.class);
	
	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET)
	public Resource<Venue> getOneVenue(@PathVariable("id") long id) {
		Venue venue = venueService.findOne(id);
		
		if (venue == null)
		    log.error(String.format("No venue found with id : %d", id));
	    
		return venueToResource(venue);
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public Resources<Resource<Venue>> getAllVenues() {

		return venueToResource(venueService.findAll());
	}

	@RequestMapping(value = "/search", method = RequestMethod.GET)
	public Resources<Resource<Venue>> getVenuesByName(@RequestParam(value = "search", defaultValue = "") String venueSearch)
	{
		return venueToResource(venueService.findSearchedBy(venueSearch));
	}
	
	@RequestMapping(value = "/{id}/next3events", method = RequestMethod.GET)
	public Resources<Resource<Event>> getNext3Events(@PathVariable("id") long id)
	{
		Venue venue = venueService.findOne(id);
		
		if (venue == null)
			log.error(String.format("No venue found with id %d.", id));
		
		Link selfLink = linkTo(methodOn(VenuesControllerApi.class).getNext3Events(id)).withSelfRel();
		
		return EventsControllerApi.eventToResource(eventService.findFirst3UpcomingEventsByVenue(venue), selfLink);
	}
	
	private Resource<Venue> venueToResource(Venue venue) {
		Link selfLink = linkTo(VenuesControllerApi.class).slash(venue.getId()).withSelfRel();
		Link venueLink = linkTo(VenuesControllerApi.class).slash(venue.getId()).withRel("venue");
		Link eventsLink = linkTo(VenuesControllerApi.class).slash(venue.getId()).slash("events").withRel("events");
		Link next3eventsLink = linkTo(methodOn(VenuesControllerApi.class).getNext3Events(venue.getId())).withRel("next3events");
		
		return new Resource<>(venue, selfLink, venueLink, eventsLink, next3eventsLink);
	}
	
	public Resources<Resource<Venue>> venueToResource(Iterable<Venue> venues) {
		
		Link selfLink = linkTo(methodOn(VenuesControllerApi.class).getAllVenues()).withSelfRel();		
		Link profileLink = new Link("http://localhost:8080/api/profile/venues").withRel("profile");
		
		List<Resource<Venue>> resources = new ArrayList<Resource<Venue>>();
		for (Venue venue : venues) {
			resources.add(venueToResource(venue));
		}
		
		return new Resources<Resource<Venue>>(resources, selfLink, profileLink);
	}

}