package uk.ac.man.cs.eventlite.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Service
public class VenueServiceImpl implements VenueService {

	@Autowired
	private EventService eventService;

	@Autowired
	private VenueRepository venueRepository;
	
	@Override
	public long count() {
		return venueRepository.count();
	}

	@Override
	public Iterable<Venue> findAll() {
		return venueRepository.findAll();
	}
	
	@Override
    public Venue findOne(long id) {
        return venueRepository.findById(id).orElse(null);
    }
	
	@Override
    public void update(Venue venue)
    {
        venueRepository.save(venue);
    }


	@Override
	public Venue save(Venue venue) {
		return venueRepository.save(venue);
	}
	
	@Override
	public Optional<Venue> findVenueById(Long id) {
		return venueRepository.findById(id);
	}

	@Override
	public Iterable<Event> findUpcomingEvents(long id) {
		Optional<Venue> venue = findVenueById(id);
		if (venue == null)
			return new ArrayList<>();
		else {
			Iterable<Event> future_events = eventService.findAllFutureEvents();
			List<Event> events_list = new ArrayList<>();
			for (Event e : future_events)
				events_list.add(e);

			events_list.retainAll(venue.get().getEvents());
			return events_list;
		}
	}
	
	@Override
	public void deleteById(Long id) {
		venueRepository.deleteById(id) ;
	}
	
	@Override
	public Iterable<Venue> findSearchedBy(String search){
		return venueRepository.findAllByNameContainsIgnoreCase(search);
	}
}
