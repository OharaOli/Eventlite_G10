package uk.ac.man.cs.eventlite.entities;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class UpdateVenue {
	
	@Id
	@GeneratedValue
	private long id;

	@NotEmpty(message = "Venue must have a name.")
	@Size(max = 255, message = "The name must be less than 256 characters.")
	private String name;

	@NotNull(message = "Venue must have a capacity.")
	@Min(value = 1, message = "Venue must have a positive capacity")
	private int capacity;
	
	@NotEmpty(message = "Venue must have an address.")
	@Size(max = 299, message = "The address must be less than 300 characters.")
	private String address;
	
	@NotEmpty(message = "Venue must have a postcode.")
	@Size(max = 299, message = "The postcode must be less than 300 characters.")
	private String postcode;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPostcode() {
		return postcode;
	}

	public void setPostcode (String postcode) {
		this.postcode = postcode;
	}
	
}
