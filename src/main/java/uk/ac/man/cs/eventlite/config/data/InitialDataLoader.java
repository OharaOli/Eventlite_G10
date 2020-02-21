package uk.ac.man.cs.eventlite.config.data;

import java.time.LocalDate;
import java.time.LocalTime;

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

		
		if (venueService.count() == 0 || eventService.count() == 0) {
			
			Venue newVenue1 = new Venue() ;
			newVenue1.setName("Test Venue 1");
			newVenue1.setCapacity(100);
			venueService.save(newVenue1);
			
			LocalDate date1 = LocalDate.of(2020, 02, 21);
			LocalDate date2 = LocalDate.of(2020, 02, 22);
			
			LocalTime time1 = LocalTime.of(9, 30);
			LocalTime time2 = LocalTime.of(9, 31);
			
			Event newEvent1 = new Event();
			newEvent1.setName("Test Event 1");
			newEvent1.setVenue(newVenue1);
			newEvent1.setDate(date1);
			newEvent1.setTime(time1);
			eventService.save(newEvent1);
			
			Event newEvent2 = new Event();
			newEvent2.setName("Test Event 2");
			newEvent2.setVenue(newVenue1);
			newEvent2.setDate(date1);
			newEvent2.setTime(time2);
			eventService.save(newEvent2);
			
			Event newEvent3 = new Event();
			newEvent3.setName("Test Event 3");
			newEvent3.setVenue(newVenue1);
			newEvent3.setDate(date2);
			newEvent3.setTime(time1);
			eventService.save(newEvent3);
			
		}
		else
		{
			log.info("Database already populated. Skipping data initialization.");
		}

	}
}
