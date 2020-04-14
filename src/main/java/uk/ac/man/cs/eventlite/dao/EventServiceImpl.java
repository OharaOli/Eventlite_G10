package uk.ac.man.cs.eventlite.dao;


import java.util.Optional;
import java.time.LocalDate;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.man.cs.eventlite.entities.Event;

@Service
public class EventServiceImpl implements EventService {

	private final static Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

	private final static String DATA = "data/events.json";

	@Autowired
	private EventRepository eventRepository;
	
	@Override
	public long count() {
		return eventRepository.count();
	}
	
	@Override
    public Event findOne(long id) {
        return eventRepository.findById(id).orElse(null);
    }
	
	@Override
    public void update(Event event)
    {
        eventRepository.save(event);
    }
	

	
	@Override
    public Event save(Event event)
    {
		return eventRepository.save(event);
    }
	
	@Override
	public Iterable<Event> findAll() {
			return eventRepository.findByIdIsNotNullOrderByDateAscTimeAsc();
	} // findAll
	
	@Override
	public Iterable<Event> findAllPreviousEvents() {
			return eventRepository.findByIdIsNotNullAndDateBeforeOrderByDate(LocalDate.now());
	} 
	
	@Override
	public Iterable<Event> findAllFutureEvents() {
			return eventRepository.findByIdIsNotNullAndDateAfterOrderByDate(LocalDate.now());
	} 
	
	@Override
	public Iterable<Event> findBySearchedBy(String search){
		return eventRepository.findAllByNameContainsIgnoreCase(search);
	}
	
	@Override
	public Iterable<Event> findFutureSearchedBy(String search){
		return eventRepository.findAllByDateAfterAndNameContainsIgnoreCase(LocalDate.now(),search);
	}
	
	@Override
	public Iterable<Event> findPastSearchedBy(String search){
		return eventRepository.findAllByDateBeforeAndNameContainsIgnoreCase(LocalDate.now(),search);
	}
		
	@Override
	public Optional<Event> findEventById(Long id) {
		return eventRepository.findById(id);
	}

	public void deleteById(Long id) {
		eventRepository.deleteById(id) ;
	}
	
	@Override
	public Optional<Event> findById(Long id) {
		return eventRepository.findById(id) ;
	}
		
}

