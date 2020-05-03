package uk.ac.man.cs.eventlite.dao;

import java.util.Optional;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

public interface EventService {

	public long count();
	
	public Event save(Event event);

	public Iterable<Event> findAll();
	
	public void update(Event event);
	
	public Event findOne(long id);

	public Optional<Event> findEventById(Long id);

	public Iterable<Event> findAllPreviousEvents();

	public Iterable<Event> findAllFutureEvents();
	
	public Iterable<Event> findBySearchedBy(String search);
	
	public Iterable<Event> findFutureSearchedBy(String search);
	
	public Iterable<Event> findPastSearchedBy(String search);
	
	public void deleteById(Long id);
	
	public Optional<Event> findById(Long id);
	
	public Iterable<Event> findFirst3UpcomingEventsByVenue(Venue venue);

}
