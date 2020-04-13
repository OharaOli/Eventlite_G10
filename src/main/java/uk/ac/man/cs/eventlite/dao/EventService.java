package uk.ac.man.cs.eventlite.dao;

import java.util.Optional;

import uk.ac.man.cs.eventlite.entities.Event;

public interface EventService {

	public long count();
	
	public Event save(Event event);

	public Iterable<Event> findAll();
	
	public void update(Event event);
	
	public Event findOne(long id);

	
	public Optional<Event> findEventById(Long id);
}
