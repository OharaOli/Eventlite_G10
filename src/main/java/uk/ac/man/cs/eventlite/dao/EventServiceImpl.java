package uk.ac.man.cs.eventlite.dao;

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
    public Event save(Event event)
    {
		return eventRepository.save(event);
    }
	
	@Override
	public Iterable<Event> findAll() {
			return eventRepository.findByIdIsNotNullOrderByDateAscTimeAsc();
	} // findAll

	@Override
	public Optional<Event> findEventById(Long id) {
		return eventRepository.findById(id);
	}
		
}

