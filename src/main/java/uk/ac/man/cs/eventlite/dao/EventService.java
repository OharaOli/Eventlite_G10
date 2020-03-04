package uk.ac.man.cs.eventlite.dao;

import java.time.LocalDate;
import java.util.Optional;

import uk.ac.man.cs.eventlite.entities.Event;

public interface EventService {

	public long count();
	
	public Event save(Event event);

	public Iterable<Event> findAll();
	
	public Iterable<Event> findAllPreviousEvents();

	public Iterable<Event> findAllFutureEvents();
	
	public Iterable<Event> findBySearchedBy(String search);
	
	public Iterable<Event> findFutureSearchedBy(String search);
	
	public Iterable<Event> findPastSearchedBy(String search);

}
