package uk.ac.man.cs.eventlite.entities;

import javax.persistence.*;

@Entity
@Table(name = "venues")
public class Venue {

	@Id
	@GeneratedValue
	private long id;

	private String name;

	private long capacity;
	
	private String description;
	
	private String coordonates;

	public Venue() {
	}

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

	public long getCapacity() {
		return capacity;
	}

	public void setCapacity(long capacity) {
		this.capacity = capacity;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String desc) {
		this.description = desc;
	}
	
	public String getCoordonates() {
		return coordonates;
	}

	public void setCoordonates(String coordonates) {
		this.coordonates = coordonates;
	}

}
