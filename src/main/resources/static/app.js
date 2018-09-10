/*TODO:
 * show player names.
 * show who is choosing and who has chosen
 * show illegal move
*/
var stompClient = null;

var hasChosen = false;

//var player1Name;

//var player2Name;

var player1Score = 0;

var player2Score =0;

var connected = false;

var player1Present = false;
var player2Present = false;



$(document).ready(function(){
	$("#game").toggle();
	$("#status").hide();
	$("#choises").hide();
	$("#winner").hide();
	$("#warning").hide();
});

function connect(){
	
	if(connected){
		return;
	}
	
	var socket = new SockJS("/endpoint");
	stompClient = Stomp.over(socket);
	
	stompClient.connect({name:$("#name").val()},function(){
		
		toggleGame();
		
		stompClient.subscribe("/user/queue/disconnect",function(msg){
			disconnect();
		});
		
		stompClient.subscribe("/topic/winner",function(msg){
			var asd = JSON.parse(msg.body);
			var message = asd.winner;
			var player1Number = asd.choice1;
			var player2Number = asd.choice2;
			
			var player1Choice;
			var player2Choice;
			
			if(player1Number == 1){
				player1Choice = "rock";
			}else if(player1Number == 2){
				player1Choice = "paper";
			}else{
				player1Choice = "scissors";
			}
			
			if(player2Number == 1){
				player2Choice = "rock";
			}else if(player2Number == 2){
				player2Choice = "paper";
			}else{
				player2Choice = "scissors";
			}
			
			
			
			hasChosen = false;
			if(message == "player1"){
				player1Score++;
				console.log(player1Score);
				console.log("a");
				$("#score1").html(player1Score);
				
				$("#winner").html("player1 picked "+player1Choice+", player2 picked "+ player2Choice +" ,player1 wins!");
				$("#winner").show();
				$("#winner").fadeOut(3000);
			}else if(message == "player2"){
				console.log("b");
				player2Score++;
				
				$("#score2").html(player2Score);
				
				$("#winner").html("player1 picked "+player1Choice+", player2 picked "+ player2Choice +" ,player2 wins!");
				$("#winner").show();
				$("#winner").fadeOut(3000);
			}else{
				console.log("C");
				$("#winner").html("player1 picked "+player1Choice+", player2 picked "+ player2Choice +",draw!");
				$("#winner").show();
				$("#winner").fadeOut(3000);
			}
			
			$("#choosing1").html("choosing");
			$("#choosing2").html("choosing");
			$("#choice").html("");
			
			
		});
		
		
		stompClient.subscribe("/topic/player",function(msg){
			
			var message = JSON.parse(msg.body);
			
			var name1 = "tbd";
			var name2 ="tbd";
			
			if(message.player1 != null){
				player1Present = true;
				name1 = message.player1;
			}
			
			if(message.player2 != null){
				player2Present = true;
				name2 = message.player2;
			}
			
			
			if(player1Present && player2Present){
				$("#status").show();
				$("#choises").show();
			}
			
			
			$("#player1").html(name1);
			$("#player2").html(name2);
		});
		
		
		stompClient.subscribe("/topic/removePlayer",function(msg){
			var message = msg.body;
			
			if(message == 1){
				player1Present = false;
				$("#player1").html("tbd");
			}else if(message == 2){
				player2Present = false;
				$("#player2").html("tbd");
			}
			
			if(!player2Present || !player1Present){
				$("#status").hide();
				$("#status").hide();
				player1Score =0;
				player2Score =0;
				$("#score1").html(player1Score);
				$("#score2").html(player2Score);
			}
			
			
		});
		
		stompClient.subscribe("/topic/hasChosen",function(msg){
			var message = msg.body;
			
			if(message == 1){
				$("#choosing1").html("has chosen");
			}else if(message == 2){
				$("#choosing2").html("has chosen");
			}
		});
		
	});
	
	
}


function disconnect(){
	
	if(!connected){
		return;
	}
	
	if(stompClient != null){
		stompClient.disconnect(function(){
			toggleGame();
		});
	//console.log("why does this happen");
	}
}

function choose(choice){
	
	if(hasChosen){
		$("warning").html("you have already chosen");
	}else{
		if(choice === 1 || choice === 2 || choice === 3){
			
			stompClient.send("/app/sendrps",{},choice);
			
			var something;
			if(choice === 1){
				something = "rock";
			}else if(choice === 2){
				something = "paper";
			}else{
				something = "scissors";
			}
			
			$("#choice").html("you chose " + something);
			hasChosen = true;
		}else{
			$("#warning").html("you chose something invalid");
			$("#warning").show();
			$("#warning").fadeOut(3000);
		}
		
	}
	
}

function toggleGame(){
	
	connected = !connected;
	
	$("#game").toggle();
	
}

