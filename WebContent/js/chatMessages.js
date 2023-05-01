/**
 * 
 */
 

/*
<div class='message'>
	<p onmouseover="window.openInDropDownMenu( '00000003','GR000001','23635'  );" class='text'> iiii</p>
</div>
*/
 

 window.insertTextMessage = function insertTextMessage( obj, block_to_insert0, isResponse )
 {
	var block_to_insert1 = document.createElement( 'p' );
	block_to_insert1.classList.add('text');	
	block_to_insert1.innerHTML = obj.content.text.replaceAll('\n', '<br>');
	block_to_insert0.appendChild( block_to_insert1 );
	
	return block_to_insert1;
}

//<a href="DocumentsRepositoryRaw?userID=00000003&documentRef=curl-7.87.0.zip" onmouseover="window.openInDropDownMenu( '00000003','00000001','4607'  );" class='text'> curl-7.87.0.zip</a>
 window.insertDocumentMessage = function insertDocumentMessage( obj, block_to_insert0, isResponse )
 {
	const	href		= obj.content.documentSrc;
	const  	text		= obj.content.text;
		
	var block_to_insert1 = document.createElement( 'p' );
	block_to_insert1.classList.add( 'text' );
	block_to_insert1.innerHTML = "<a href='" + href + "'><u>" + text + "</u></a>";

	//alert( text + "\r\n" + block_to_insert1.innerHTML );

	block_to_insert0.appendChild( block_to_insert1 );
	
	return block_to_insert1;
}

window.insertStaticImageMessage = function insertStaticImageMessage( obj, block_to_insert0, isResponse )
{
	const 	w 		 = parseInt(obj.content.imageWide)*3;
	const 	h 		 = parseInt(obj.content.imageHeight)*3;
	const 	imageSrc = obj.content.imageSrc;
		        		
	let block_to_insert1 	= document.createElement("img");
	block_to_insert1.src 	= imageSrc;
	block_to_insert1.width	= w;
	block_to_insert1.height	= h;
	block_to_insert1.classList.add('text');
	
	block_to_insert0.appendChild( block_to_insert1 );
	
	return block_to_insert1;
}

/*
<div class="dropdown" id="ddwn24073" >
	<div class='message'>
		<audio onmouseover="window.openInDropDownMenu( '00000002','GR000001','24073' );" controls class='text audio'>
			<source src="AudioRepositoryRaw?userID=00000002&audioUUID=5c9d1e84-e662-4a6c-9ed9-0c04ed850722.mp4" type="audio/mpeg">Your browser does not support the audio element.
		</audio>
	</div>
	<div id='ddm24073' class="dropdown-content-left">
	</div>
	<div class='reactions-content'><div class="emonjis-center-aligned"><a class="emonjis-background-image">&#x1F44F</a><div class="emonjis-text">1</div></div></div>
</div>
*/
window.insertAudioMessage = function insertAudioMessage( obj, block_to_insert0, isResponse )
{	  	    
    var 	audioName = obj.content.audioName;
		        		
	var div = document.createElement("div");
	div.classList.add('text');
	div.classList.add('audio');	

	var block_to_insert1 = document.createElement("audio");
  	block_to_insert1.setAttribute( "src", "AudioRepositoryRaw?userID=" + obj.senderID + "&audioUUID=" + audioName );
	block_to_insert1.setAttribute("controls", "controls");
	block_to_insert1.setAttribute( "type", "audio/mpeg" );
 	block_to_insert1.style.height = "25px";
 	
	div.appendChild( block_to_insert1 );
	block_to_insert0.appendChild( div );

	return div;
}

window.insertDeletedMessage = function insertDeletedMessage( block_to_insert1 )
{
	block_to_insert1 		= document.createElement( 'p' );
	block_to_insert1.classList.add('text');	
	block_to_insert1.innerHTML = "MENSAJE BORRADO";
}

window.insertObjectWihiDDMenuMessage = function insertObjectWihiDDMenuMessage( obj, block_div_0, isResponse )
{
	/*
	<div class="dropdown" id="ddwn117186" >
	
	</div>
	*/
							
	var dropdowndiv 	= document.createElement('div');
	
	dropdowndiv.classList.add('dropdown');
	dropdowndiv.id = "ddwn" + obj.id;
	
	window.updateObjectWihiDDMenuMessage( obj, dropdowndiv, isResponse )
		
	block_div_0.appendChild(dropdowndiv);	
}

window.updateObjectWihiDDMenuMessage = function updateObjectWihiDDMenuMessage( obj, dropdowndiv, isResponse )
{
	/*
	<div id="117186" class='message'>
		<p  onmouseover="window.openInDropDownMenu( 'senderID', 'receiverID', '117186' );" class='text'> Zxzx</p>
	</div>
	<div id='ddm117186' class="dropdown-content">
	</div>
	<div class='reactions-content'><a>&#128077</a></div>

	<div id="117186"  class='message'>
		<div class='response'>
			<p onmouseover="window.openInDropDownMenu( 'senderID', 'receiverID', '117186' );" class='text'> Zxzx</p>
		</div>
	</div>
	<div id='ddm117186' class="dropdown-content">
	</div>
	<div class='reactions-content-right'><a>&#128077</a></div>
	*/
							
	let deletedObject = !obj || !obj.content || obj.content == null;
													
	while (dropdowndiv.firstChild) 
	{
		dropdowndiv.removeChild(dropdowndiv.lastChild);
	}

	var mouseoverDiv 	= document.createElement('div');
	var responseDiv;
	var ddContentDiv	= document.createElement('div');
	var rectionsDiv 	= document.createElement('div');
	
	if ( !isResponse )
	{
		//alert("!isResponse");

		rectionsDiv.classList.add('reactions-content');
		
		mouseoverDiv.classList.add('message');
	}
	else
	{
		//alert("isResponse");

		responseDiv = document.createElement('div');

		rectionsDiv.classList.add('reactions-content-right');
		
		mouseoverDiv.classList.add('message-response');
	}
	
	rectionsDiv.appendChild(window.getReactionDiv(obj));
	
	mouseoverDiv.id = obj.id;
	ddContentDiv.id = "ddm" + obj.id;
	
	if ( responseDiv )
	{
		ddContentDiv.classList.add( 'dropdown-content-right' );

		var returnElement = window.insertObjectMessage( obj, responseDiv, isResponse );
		
		mouseoverDiv.appendChild( responseDiv );
		
		if ( !deletedObject )
		{
			returnElement.addEventListener( "mouseover", function() 
			{
				window.openOutDropDownMenu( obj.senderID, obj.receiverID, obj.id );
			});
		}
	}
	else
	{
		ddContentDiv.classList.add( 'dropdown-content-left' );

		var returnElement = window.insertObjectMessage( obj, mouseoverDiv, isResponse );
		
		if ( !deletedObject )
		{
			//alert( obj.senderID + " " + obj.id );
			
			returnElement.addEventListener( "mouseover", function() 
			{
				window.openInDropDownMenu( obj.senderID, obj.receiverID, obj.id );
			});
		}
	}
	
	dropdowndiv.appendChild(mouseoverDiv);
	dropdowndiv.appendChild(ddContentDiv);
	if ( !deletedObject )
	{
		dropdowndiv.appendChild(rectionsDiv);
	}
}

			
			
window.insertObjectMessage = function insertObjectMessage( obj, block_to_insert0, isResponse )
{
	//alert( obj.content );
	
	if ( !obj || !obj.content || obj.content == null )
	{		
		while (block_to_insert0.firstChild) 
		{
			block_to_insert0.removeChild(block_to_insert0.lastChild);
		}

		var block_to_insert1 = document.createElement( 'p' );
		block_to_insert1.classList.add('text');	
		block_to_insert1.innerHTML = "Mensaje borrado";
		block_to_insert0.appendChild( block_to_insert1 );
		
		return
	}

/*
	<div id="66784" onmouseover="window.openInDropDownMenu( '66784' );" class='message'>
		<p class='text'> Hola 888</p>
	</div>
	<div id='ddm66784' class="dropdown-content">
	</div>
	<div class='reactions-content'><a>&#x1F9E1</a><a>&#128077</a><a>&#128078</a><a>&#128170</a><a>&#x1F44F</a><a>&#x1F595</a></div>
*/
	var returnElement;
	let mouseoverDiv 	= document.createElement( 'div' );
	let dropdownDiv 	= document.createElement( 'div' );
			
	if ( isResponse )
	{
		mouseoverDiv.classList.add('message-response');	
	}
	else
	{
		mouseoverDiv.classList.add('message');	
	}
	
	dropdownDiv.classList.add('dropdown-content');

	if ( obj.content.chatContentClass == "quick_chat.adapters.chat.TextMessage")
	{
		returnElement = window.insertTextMessage( obj, mouseoverDiv, isResponse );
	}
	else
	if ( obj.content.chatContentClass == "quick_chat.adapters.chat.StaticImageMessage")
	{
		returnElement = window.insertStaticImageMessage( obj, mouseoverDiv,isResponse  );
	}
	else
	if ( obj.content.chatContentClass == "quick_chat.adapters.chat.AudioMessage" )
	{				
		returnElement = window.insertAudioMessage( obj, mouseoverDiv, isResponse  );
	}
	else
	if ( obj.content.chatContentClass == "quick_chat.adapters.chat.DocumentMessage")
	{
		returnElement = window.insertDocumentMessage( obj, mouseoverDiv, isResponse );
	}
	
	block_to_insert0.appendChild( mouseoverDiv );
	block_to_insert0.appendChild( dropdownDiv );
	
	return returnElement;
}


window.loadChatContent = function loadChatContent( receiverUID, receiverName )
{
	//alert( "window.loadChatContent:" + receiverUID + " " + receiverName );
	
	const receiverType = receiverUID.startsWith("G") ? "kGroupUser" : "kSingleUser";
 				 	
	window.chat_setReceiver( receiverName, receiverUID, receiverType ); 

	var container_block = document.getElementById("messages-div");

	let xhr = new XMLHttpRequest();

	xhr.open( "POST", document.gPREFIX + "GetChatContent" );
	
	xhr.setRequestHeader( "Content-Type", "application/json" );
		  
	const data = 
	{
	  type: "JSON",
	};
	
	data.registryUID	= document.gUSER_UID;
	data.receiverUID	= receiverUID;
	
	xhr.onload = () => 
	{				
		const response 			= JSON.parse( xhr.responseText );
		const messagesArr		= response.payload;
																												
		if ( messagesArr )
		{				
			while (container_block.firstChild) 
			{
    			container_block.removeChild(container_block.lastChild);
  			}
  
			for (var i = 0; i < messagesArr.length; i++) 
			{     			
				const obj = messagesArr[i];
				
				if ( obj.senderName == "NOTIFICSYS" )
				{
					continue;
				}
				
				const formattedTime 	= window.calcFormattedTime(new Date(obj.time));
				const time_name_insert 	= document.createElement('p');
				
				const isResponse 		= obj.senderID == document.gUSER_UID;
				
				//alert( obj.senderID + " " + document.gUSER_UID );
				
				if ( isResponse )
				{
					var time_div 		= document.createElement('div');
					time_div.classList.add('time');
					time_div.style.textAlign 	= "right";
			
					time_name_insert.classList.add('time');
					time_name_insert.innerHTML = formattedTime + " ";
					time_div.appendChild(time_name_insert);
					
					container_block.appendChild( time_div );
				}
				else
				{
					time_name_insert.classList.add( 'time' );
					time_name_insert.innerHTML = formattedTime + " " + obj.senderName;
					
					container_block.appendChild( time_name_insert );
				}				
				
				window.insertObjectWihiDDMenuMessage( obj, container_block, isResponse );
			}
			
			container_block.scroll(0, 100000000);	
			
			const inputDiv = document.getElementById( "inputDiv" );
	
			if ( inputDiv )
			{
				inputDiv.style.visibility= "visible";
			}
		}
	}
	
	xhr.send( JSON.stringify(data) );
}
