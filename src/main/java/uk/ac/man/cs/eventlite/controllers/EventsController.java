package uk.ac.man.cs.eventlite.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import uk.ac.man.cs.eventlite.config.data.InitialDataLoader;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Tweet;
import uk.ac.man.cs.eventlite.entities.UpdateEvent;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;


@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {

	private final static Logger log = LoggerFactory.getLogger(InitialDataLoader.class);
	
	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@RequestMapping(method = RequestMethod.GET)
	public String getAllEvents(Model model) throws TwitterException {
		
		model.addAttribute("prevEvents", eventService.findAllPreviousEvents());
		model.addAttribute("futureEvents", eventService.findAllFutureEvents());
		model = getLatestTweets(model) ;
		
		return "events/index";
	}

	public Model getLatestTweets(Model model) throws TwitterException {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey("I8uQvkttsHY61PSnBPy9Re83I")
		  .setOAuthConsumerSecret("MyFaQ0KR0AsZ3cNJxk6a3fl27HtHEvCTeBigOc9Sunk3VyYVQi")
		  .setOAuthAccessToken("1252996199390601221-F1RqpAKQ7ZY6iUyJqiCQDREmavl7EV")
		  .setOAuthAccessTokenSecret("m7r7ypo7wpiKu9CF88qGClwBtQ6bQxSoI1V8Jpg5t4pL7");
		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter = tf.getInstance();
		
		List<Status> allTweets = twitter.getHomeTimeline() ;
		
		List<Status> latestTweets = new ArrayList<Status>();
		
		for (int i = 0; i < 5; i++)
			latestTweets.add(allTweets.get(i)) ;
		
		model.addAttribute("tweets", (Iterable<Status>) latestTweets) ;
		
		return model ;
	}
	
	@RequestMapping(value = "/delete_event", method = RequestMethod.GET)
	public String deleteEvent(@RequestParam(name="eventId") Long id) {
		if (eventService.findById(id).isPresent())
		  eventService.deleteById(id) ;
		
		return "redirect:/events" ;
	}

		

	@RequestMapping(value = "/add",method = RequestMethod.GET)
	public String getAddEvents(Model model) {
	  model.addAttribute("event", new Event());
	  model.addAttribute("venues", venueService.findAll());

		return "events/add/index" ;
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
		model.addAttribute("tweet", new Tweet());

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
	
	
	@RequestMapping(value="/add",method = RequestMethod.POST)
	public String createEvent(@RequestBody @Valid @ModelAttribute ("event") Event event, 
			BindingResult errors, Model model, RedirectAttributes redirectAttrs) {
	    if (errors.hasErrors()) {
	    	model.addAttribute("event", event);
	        return "events/add/index";
	    }
	    this.eventService.save(event);
	    return "redirect:/events";
	}
	
	@RequestMapping(value="/tweet/{id}", method = RequestMethod.POST)
	public String newTweet(@RequestBody @Valid @ModelAttribute("tweet") Tweet tweet,
			BindingResult errors, Model model, @PathVariable Long id,
			RedirectAttributes redirectAttrs) throws TwitterException {
		
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey("I8uQvkttsHY61PSnBPy9Re83I")
		  .setOAuthConsumerSecret("MyFaQ0KR0AsZ3cNJxk6a3fl27HtHEvCTeBigOc9Sunk3VyYVQi")
		  .setOAuthAccessToken("1252996199390601221-F1RqpAKQ7ZY6iUyJqiCQDREmavl7EV")
		  .setOAuthAccessTokenSecret("m7r7ypo7wpiKu9CF88qGClwBtQ6bQxSoI1V8Jpg5t4pL7");
		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter = tf.getInstance();
		
		if (errors.hasErrors()) {
	    	model.addAttribute("tweet", tweet);
	        return "redirect:/events/{id}";
	    }
		
		try {
			Status status = twitter.updateStatus(tweet.getText());
			String tweetMade = status.getText();
			redirectAttrs.addFlashAttribute("tweetMade", tweetMade);
			redirectAttrs.addFlashAttribute("ok_message", true);
			return "redirect:/events/{id}";
		} catch (TwitterException e) {
     		e.printStackTrace();
			return "redirect:/events";
		}
	}


}
