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
			
			Venue venueA = new Venue() ;
			venueA.setName("Venue A");
			venueA.setAddress("23 Manchester Road");
			venueA.setPostcode("E14 3BD");
			venueA.setCapacity(50);
			venueA.setDescription("This is a venue");
			venueService.save(venueA);
			

			Venue venueB = new Venue() ;
			venueB.setName("Venue B");
			venueB.setAddress("Highland Road");
			venueB.setPostcode("S43 2EZ");
			venueB.setCapacity(100);
			venueA.setDescription("This is a venue");
			venueService.save(venueB);
			
			Venue venueC = new Venue() ;
			venueC.setName("Venue C");
			venueC.setAddress("19 Acacia Avenue");
			venueC.setPostcode("WA15 8QY");
			venueC.setCapacity(10);
			venueA.setDescription("This is a venue");
			venueService.save(venueC);
			
			Venue venueD = new Venue() ;
			venueD.setName("Venue D");
			venueD.setAddress("69 Benedict Dover road");
			venueD.setPostcode("TU55 6FL");
			venueD.setCapacity(300);
			venueService.save(venueD);
			
			LocalDate date1 = LocalDate.of(2017, 07, 11);
			LocalDate date2 = LocalDate.of(2017, 07, 12);
			LocalDate date3 = LocalDate.of(2019, 01, 01);
			LocalDate date4 = LocalDate.of(2019, 02, 02);
			LocalDate date5 = LocalDate.of(2019, 03, 03);
			LocalDate date6 = LocalDate.of(2019, 04, 21);
			LocalDate date7 = LocalDate.of(2020, 05, 30);
			LocalDate date8 = LocalDate.of(2020, 8, 13);
			
			LocalTime time1 = LocalTime.of(12, 30);
			LocalTime time2 = LocalTime.of(18, 30);
			
			// Example event 1.
			Event event1 = new Event();
			event1.setName("Event Apple");
			event1.setDate(date2);
			event1.setVenue(venueA);
			event1.setDescription("Event Apple will be host to some of the world’s best iOS developers...");
			eventService.save(event1);
			
			// Example event 2.
			Event event2 = new Event();
			event2.setName("Event Alpha");
			event2.setDate(date1);
			event2.setTime(time1);
			event2.setVenue(venueB);
			event2.setDescription("Event Alpha is the first of its kind...");
			eventService.save(event2);
			
			// Example event 3.
			Event event3 = new Event();
			event3.setName("Event Previous");
			event3.setVenue(venueA);
			event3.setDate(date1);
			event3.setTime(time2);
			event3.setDescription("");
			eventService.save(event3);

			// Test event 1.
			Event testEvent1 = new Event();
			testEvent1.setName("Test Event 1");
			testEvent1.setVenue(venueC);
			testEvent1.setDate(date3);
			testEvent1.setTime(time1);
			testEvent1.setDescription("Test Event 1...");
			eventService.save(testEvent1);
			
			// Test event 2.
			Event testEvent2 = new Event();
			testEvent2.setName("Test Event 2");
			testEvent2.setVenue(venueC);
			testEvent2.setDate(date4);
			testEvent2.setTime(time1);
			testEvent2.setDescription("Test Event 2...");
			eventService.save(testEvent2);		
			
			// Test event 3.
			Event testEvent3 = new Event();
			testEvent3.setName("Test Event 3");
			testEvent3.setVenue(venueC);
			testEvent3.setDate(date5);
			testEvent3.setTime(time1);
			testEvent3.setDescription("Test Event 3...");
			eventService.save(testEvent3);	
			
			// Test event 4.
			Event testEvent4 = new Event();
			testEvent4.setName("Test Event 4");
			testEvent4.setVenue(venueC);
			testEvent4.setDate(date6);
			testEvent4.setTime(time1);
			testEvent4.setDescription("Test Event 4...");
			eventService.save(testEvent4);
			
			// Test event 5.
			Event testEvent5 = new Event();
			testEvent5.setName("Test Event 5");
			testEvent5.setVenue(venueC);
			testEvent5.setDate(date7);
			testEvent5.setTime(time1);
			testEvent5.setDescription("Test Event 5...");
			eventService.save(testEvent5);		
			
			// Test event 6.
			Event testEvent6 = new Event();
			testEvent6.setName("Test Event 6");
			testEvent6.setVenue(venueC);
			testEvent6.setDate(date8);
			testEvent6.setTime(time1);
			testEvent6.setDescription("Test Event 6...");
			eventService.save(testEvent6);	
			
		}
		else
		{
			log.info("Database already populated. Skipping data initialization.");
		}

	}
}
