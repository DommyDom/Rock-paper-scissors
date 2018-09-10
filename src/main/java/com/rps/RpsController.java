package com.rps;


import org.springframework.beans.factory.annotation.Autowired;


import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Controller
public class RpsController implements RpsEventListener{
	
	private SimpMessagingTemplate template;
	
	@Autowired
	public RpsController(SimpMessagingTemplate template) {
		this.template = template;
	}
	
	@Autowired
	private RpsService rpsService;
	
	
	
	
	@MessageMapping("/sendrps")
	public void manageRps(@Payload int choice, StompHeaderAccessor sha) {
		
		if(rpsService.getPlayer1() == null || rpsService.getPlayer2() == null) {
			return;
		}
		
		if(choice <1 || choice > 3) {
			return;
		}
		
		String sessionId = sha.getSessionId().toString();
		User player;
		
		if(rpsService.getPlayer1().getSession() == sessionId) {
			player = rpsService.getPlayer1();
			template.convertAndSend("/topic/hasChosen",1);
			
		}else if(rpsService.getPlayer2().getSession() == sessionId) {
			player = rpsService.getPlayer2();
			template.convertAndSend("/topic/hasChosen",2);
		}else {
			return;
		}
		
		if(player.getChoice() != 0) {
			return;
		}else {
			player.setChoice(choice);
		}
		
		
	}
	
	@EventListener
	public void connectEvent(SessionConnectEvent event) {
		
		
		StompHeaderAccessor sha  = StompHeaderAccessor.wrap(event.getMessage());
		String name = sha.getNativeHeader("name").get(0);
		String sessionId = sha.getSessionId();
		
		if(rpsService.getPlayer1() == null) {
			//figure out how to get name on connect
			rpsService.setPlayer1(new User(name,sessionId));
			
			rpsService.getPlayer1().setRpsListener(this);;
			
			
			
		}else if(rpsService.getPlayer2() == null) {
			//same here
			rpsService.setPlayer2(new User(name,sessionId));
			
			
			rpsService.getPlayer2().setRpsListener(this);
		}else {
			//figure out how to dc
			System.out.println("pewpew");
			SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
			headerAccessor.setSessionId(sessionId);
			headerAccessor.setLeaveMutable(true);
			
			template.convertAndSendToUser(sessionId,"/queue/disconnect","fucking hell", headerAccessor.getMessageHeaders());
		}
	}
	
	@EventListener
	public void disconnectEvent(SessionDisconnectEvent event) {
		StompHeaderAccessor sha  = StompHeaderAccessor.wrap(event.getMessage());
		String sessionId = sha.getSessionId();
		
		if(rpsService.getPlayer1() !=null) {
			if(rpsService.getPlayer1().getSession() == sessionId) {
			rpsService.setPlayer1(null);
			template.convertAndSend("/topic/removePlayer",1);
			return;
			} 
		}
		if(rpsService.getPlayer2() != null) {
			if(rpsService.getPlayer2().getSession() == sessionId) {
			rpsService.setPlayer2(null);
			template.convertAndSend("/topic/removePlayer",2);
			return;
			}
		}
	}
	
	
	@EventListener
	public void handleSubscribe(SessionSubscribeEvent event) {
		
	
		
		StompHeaderAccessor sha  = StompHeaderAccessor.wrap(event.getMessage());
		String sessionId = sha.getSessionId();
		String subId = sha.getSubscriptionId();
		
		System.out.println(subId);
		
		if(subId.equals("sub-0")) {
			System.out.println("what about here?");
			if(rpsService.getPlayer1().getSession() != sessionId && rpsService.getPlayer2().getSession() != sessionId) {
			SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
				headerAccessor.setSessionId(sessionId);
				headerAccessor.setLeaveMutable(true);
				
				template.convertAndSendToUser(sessionId,"/queue/disconnect","full session", headerAccessor.getMessageHeaders());
			}
		}else if(subId.equals("sub-2") ) {
			System.out.println("did we get here?");
			String name1 = null;
			String name2 = null;
			
			if(rpsService.getPlayer1() != null) name1 ="\"" + rpsService.getPlayer1().getName()+ "\"";
			if(rpsService.getPlayer2() != null) name2 ="\"" + rpsService.getPlayer2().getName() +"\"";
			
			template.convertAndSend("/topic/player","{ \"player1\":"+ name1 + ", \"player2\":"+name2+" }");
			
		}
		
	}

	@Override
	public void onChoice() {
		System.out.println("this should work");
		User player1 = rpsService.getPlayer1();
		User player2 = rpsService.getPlayer2();
		if(player1 != null && player2 != null) {
			System.out.println("this  should also work");
			int choice1 = player1.getChoice();
			int choice2 = player2.getChoice();
			
			
			if(choice1 != 0 && choice2 != 0) {
				System.out.println("and this");
				if((choice1 == 1 && choice2 == 3) || (choice1 == 2 && choice2 == 1) || (choice1 == 3 && choice2 == 2)) {
					System.out.println("did");
					template.convertAndSend("/topic/winner","\"winner\":\"player1\", \"choice1\":"+choice1+",\"choice2\":"+choice2+"}");
				}else if((choice2 == 1 && choice1 == 3) || (choice2 == 2 && choice1 == 1) || (choice2 == 3 && choice1 == 2)){
					System.out.println("we");
					template.convertAndSend("/topic/winner","{\"winner\":\"player2\", \"choice1\":"+choice1+", \"choice2\":"+choice2+"}");
				}else {
					System.out.println("fail?");
					template.convertAndSend("/topic/winner","{\"winner\":\"draw\", \"choice1\":"+choice1+",\"choice2\":"+choice2+"}");
				}
				
				
				player1.resetChoice();
				player2.resetChoice();
			}
		}
	}
	
	

}
