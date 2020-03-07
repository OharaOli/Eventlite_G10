package uk.ac.man.cs.eventlite.entities;

import java.time.LocalDate;
import java.time.LocalTime;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

public class UpdateEvent {
	
	private long eventId;

	@NotEmpty(message = "Event must have a name.")
	@Size(max = 255, message = "The name must be less than 256 characters.")
	private String name;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@NotNull(message = "Event must have a date.")
	@Future(message = "Date must be in the future.")
	private LocalDate date;

	private long venueId;
	
	// Time is optional.
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	@DateTimeFormat(pattern = "HH:mm")
	private LocalTime time;

	// Description is optional.
	@Size(max = 499, message = "Description must be less than 500 characters.")
	private String description;

	public void setId(long eventId) {
		this.eventId = eventId;
	}
	
	public long getId() {
		return eventId;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}	
	
	public LocalDate getDate() {
		return date;
	}

	public void setTime(LocalTime time) {
		this.time = time;
	}

	public LocalTime getTime() {
		return time;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public void setVenueId(long venueId) {
		this.venueId = venueId;
	}

	public long getVenueId() {
		return venueId;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
