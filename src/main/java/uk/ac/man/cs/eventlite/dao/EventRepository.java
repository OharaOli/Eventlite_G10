package uk.ac.man.cs.eventlite.dao;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

public interface EventRepository extends CrudRepository<Event, Long>{
	
	public Iterable<Event> findAllByNameContainsIgnoreCase(String search);
	
	public Iterable<Event> findAllByDateBeforeAndNameContainsIgnoreCase(LocalDate current, String search);
	
	public Iterable<Event> findAllByDateAfterAndNameContainsIgnoreCase(LocalDate current, String search);
	
	public Iterable<Event> findByIdIsNotNullAndDateAfterOrderByDate(LocalDate current);
	
	public Iterable<Event> findByIdIsNotNullAndDateBeforeOrderByDate(LocalDate current);

	public Iterable<Event> findByIdIsNotNullOrderByDateAscTimeAsc();

	@Query("SELECT e FROM Event e "
			 + "WHERE (e.date > :date OR e.date = :date AND e.time >= :time) AND (e.venue = :venue) "
			 + "ORDER BY e.date ASC")
	public Page<Event> findByNotEarlierThanDateTimeByVenueIdOrderByDateAsc(@Param("date") LocalDate date, @Param("time") LocalTime time, @Param("venue") Venue venue, Pageable pageable);
		
}
