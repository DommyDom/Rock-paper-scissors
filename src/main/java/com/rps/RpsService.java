package com.rps;

import org.springframework.stereotype.Service;

@Service
public class RpsService {
	private User player1 = null;
	private User player2 = null;
	
	public User getPlayer1() {
		return this.player1;
	}
	
	public User getPlayer2() {
		return this.player2;
	}
	
	
	public void setPlayer2(User user) {
		this.player2 = user;
	}

	public void setPlayer1(User user) {
		this.player1 = user;
		
	}
	
	
	
	
}
