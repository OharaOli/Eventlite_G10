package uk.ac.man.cs.eventlite.dao;

import java.util.Optional;

import uk.ac.man.cs.eventlite.entities.Venue;

public interface VenueService{

	public long count();

	public Iterable<Venue> findAll();
	
    public void update(Venue venue);
	
	public Venue findOne(long id);

	
	public Venue save(Venue venue);
	
	public Optional<Venue> findVenueById(Long id);
	
}
