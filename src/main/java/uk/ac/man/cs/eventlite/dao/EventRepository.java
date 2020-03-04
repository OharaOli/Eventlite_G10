package uk.ac.man.cs.eventlite.dao;

import java.time.LocalDate;

import org.springframework.data.repository.CrudRepository;

import uk.ac.man.cs.eventlite.entities.Event;

public interface EventRepository extends CrudRepository<Event, Long>{
	
	public Iterable<Event> findByIdIsNotNullOrderByDateAscTimeAsc();
	
	public Iterable<Event> findAllByNameContainsIgnoreCase(String search);
	
	public Iterable<Event> findAllByDateBeforeAndNameContainsIgnoreCase(LocalDate current, String search);
	
	public Iterable<Event> findAllByDateAfterAndNameContainsIgnoreCase(LocalDate current, String search);
	
	public Iterable<Event> findByIdIsNotNullAndDateAfterOrderByDate(LocalDate current);
	
	public Iterable<Event> findByIdIsNotNullAndDateBeforeOrderByDate(LocalDate current);

}
