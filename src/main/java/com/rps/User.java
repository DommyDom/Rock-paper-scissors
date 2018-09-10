package com.rps;

public class User {
	private String name;
	private String session;
	
	/*
	 * 1:rock
	 * 2:paper
	 * 3:scissors
	 */
	
	private int choice =0;
	
	
	public User(String name, String sessionId) {
		this.name = name;
		this.session = sessionId;
	}
	
	
	


	public void setChoice(int choice) {
		this.choice = choice;
		
		if(listener != null) listener.onChoice();
	}
	
	public void resetChoice() {
		this.choice = 0;
	}
	
	
	public int getChoice() {
		return choice;
	}
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSession() {
		return session;
	}
	public void setSession(String session) {
		this.session = session;
	}
	
	private RpsEventListener listener;
	
	public void setRpsListener(RpsEventListener listener) {
		this.listener = listener;
	}
	
	
	
}
