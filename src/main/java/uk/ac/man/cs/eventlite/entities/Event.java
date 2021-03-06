package uk.ac.man.cs.eventlite.entities;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import java.time.LocalDate;
import java.time.LocalTime;

import javax.persistence.Table;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "events")

public class Event {
    
	@Id
	@GeneratedValue
	private long id;

	@JsonFormat(shape = JsonFormat.Shape.STRING)
	@Size(max = 499, message = "Description must be less than 500 characters.")
	private String description;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING)
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@NotNull(message = "Event must have a date.")
	@Future(message = "Date must be in the future.")
	private LocalDate date;

	@JsonFormat(shape = JsonFormat.Shape.STRING)
	@DateTimeFormat(pattern = "HH:mm")
	private LocalTime time;

	@NotEmpty(message = "Event must have a name.")
	@Size(max = 255, message = "The name must be less than 256 characters.")
	private String name;

	@ManyToOne
	@NotNull(message = "Event must have a venue.")
	@JoinColumn(name = "Venue_id")
	private Venue venue;
	
	public Event() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public LocalDate getDate() {
		return date;
	}

	@JsonIgnore
	public String getFormattedDate() {
		String formattedDate = "";
		int dayOfMonth = date.getDayOfMonth();
		final String[] months = {"January", "February", "March", "April", "May", "June", "July",
								"August", "September", "October", "November", "December"};
		final String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
		
		formattedDate += days[date.getDayOfWeek().getValue() - 1] + ", " + dayOfMonth;
		
		if (3 < dayOfMonth && dayOfMonth < 21)
			formattedDate += "<sup>th</sup>";
		else 
		{
			switch (dayOfMonth % 10) 
			{
				case 1:
					formattedDate += "<sup>st</sup>"; break;
				case 2:
					formattedDate += "<sup>nd</sup>"; break;
				case 3:
					formattedDate += "<sup>rd</sup>"; break;
				default:
					formattedDate += "<sup>th</sup>";
			}
		}
		
		formattedDate += " " + months[date.getMonthValue() - 1] + " " + date.getYear();
		
		return formattedDate;
	}
	
	@JsonIgnore
	public String getFormattedDate4Table() {
		String formattedDate = "";
		int dayOfMonth = date.getDayOfMonth();
		final String[] months = {"January", "February", "March", "April", "May", "June", "July",
								"August", "September", "October", "November", "December"};
		final String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
		
		formattedDate += (days[date.getDayOfWeek().getValue() - 1] + ", " + dayOfMonth
					     + " " + months[date.getMonthValue() - 1] + " " + date.getYear());
		
		return formattedDate;
	}
	
	public void setDate(LocalDate date) {
		this.date = date;
	}

	public LocalTime getTime() {
		return time;
	}

	public void setTime(LocalTime time) {
		this.time = time;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@JsonIgnore
	public String getDescription() {
		return description;
	}
	
	@JsonIgnore
	public void setDescription(String description) {
		this.description = description;
	}

	public Venue getVenue() {
		return venue;
	}

	public void setVenue(Venue venue) {
		this.venue = venue;
	}
}
