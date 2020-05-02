package uk.ac.man.cs.eventlite.entities;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;


public class Tweet {
    
	@NotEmpty(message = "You can't tweet an empty message")
	@Size(max = 280, message = "A tweet can be at most 280 characters")
	private String text;
	
	public Tweet() {
	}
	
	public Tweet(String text) {
		setText(text);
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
}
