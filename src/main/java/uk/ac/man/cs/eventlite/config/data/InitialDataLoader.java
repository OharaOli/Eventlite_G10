package uk.ac.man.cs.eventlite.config.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Component
@Profile({ "default", "test" })
public class InitialDataLoader implements ApplicationListener<ContextRefreshedEvent> {

	private final static Logger log = LoggerFactory.getLogger(InitialDataLoader.class);

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {

		
		if (venueService.count() == 0) {
			Venue newVenue = new Venue();
			//newVenue.setId(1) ;
			newVenue.setName("Venue");
			newVenue.setCapacity(5);
			
			venueService.save(newVenue);
		}
		else
		{
			log.info("Database already populated. Skipping data initialization.");
		}


		if (eventService.count() == 0) {
			Event event1 = new Event();
			event1.setName("PrinceEvent");
			event1.setVenue(1);
			eventService.save(event1);
		}
		else {
			log.info("Database already populated. Skipping data initialization.");

		}

	}
}