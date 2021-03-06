package uk.ac.man.cs.eventlite.dao;

import java.util.*;
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
		Venue result = findOne(id);
		if (result == null)
			return null;
		else
			return Optional.of(result);
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
		venueRepository.deleteById(id);
	}
	
	@Override
	public Iterable<Venue> findSearchedBy(String search){
		return venueRepository.findAllByNameContainsIgnoreCase(search);
	}
	
	@Override
	public Iterable<Venue> sortByNoOfEventsDesc() {
		
		class VenueComparator implements Comparator<Venue> 
		{
			@Override
			public int compare(Venue v1, Venue v2) {
				if (v1.getEvents().size() == v2.getEvents().size())
					return v1.getName().compareTo(v2.getName());
				else
					return v2.getEvents().size() - v1.getEvents().size();
			}
		}

		Iterable<Venue> allVenues = venueRepository.findAll();
		List<Venue> venueList = new ArrayList<>();
		for (Venue v : allVenues)
			venueList.add(v);
		venueList.sort(new VenueComparator());

		return venueList;
	}
	
}
