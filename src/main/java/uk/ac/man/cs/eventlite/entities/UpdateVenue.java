package uk.ac.man.cs.eventlite.entities;

import java.time.LocalDate;
import java.time.LocalTime;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

public class UpdateVenue {
	
	private long venueId;
	private String coordonates;


	@NotEmpty(message = "Venue must have a name.")
	@Size(max = 255, message = "The name must be less than 256 characters.")
	private String name;


	// Description is optional.
	@Size(max = 499, message = "Description must be less than 500 characters.")
	private String description;
	
	private int capacity;

	public void setId(long venueId) {
		this.venueId = venueId;
	}
	
	public long getId() {
		return venueId;
	}


	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
	
	public void setCapacity(int cap) {
		this.capacity = cap;
	}
	
	public int getCapacity() {
		return capacity;
	}
	
	public String getCoordonates() {
		return coordonates;
	}

	public void setCoordonates(String coordonates) {
		this.coordonates = coordonates;
	}
	
}
