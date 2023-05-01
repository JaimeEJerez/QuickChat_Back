/**
 * 
 */
const emonjisrc_left = [ 	"assets/img/emon01.png", 
							"assets/img/emon02.png", 
							"assets/img/emon03.png", 
							"assets/img/emon04.png", 
							"assets/img/emon05.png", 
							"assets/img/emon06.png" ];


//const emonjis_left = [ "&#x1F9E1", "&#128077", "&#128078", "&#128170", "&#x1F44F", "&#x1F595", "&#x1F60A", "&#x274C" ];

const emonjis_right = [ "&#x274C" ];


var dropdowns;

var gCENDER_ID;



window.openOutDropDownMenu = function openOutDropDownMenu( senderID, receiverID, messageID ) 
{	
	let new_dropdowns = document.getElementById( "ddm" + messageID );
		
	if ( dropdowns )
	{
		closeDropDown();
	}

   	dropdowns = new_dropdowns;
	    
    while (dropdowns.firstChild) 
	{
		dropdowns.removeChild(dropdowns.lastChild);
	}

    for ( let i=0; i<emonjis_right.length; i++ )
    {
	    const a = document.createElement('a');
	    
	    a.emonji = i;
	    
	    a.innerHTML  =  emonjis_right[i];
	    
	    dropdowns.appendChild(a);
	    
	    a.onclick = function() 
	    {	
			doDelete( messageID.toString() );	
	    }
    }
                    
    dropdowns.classList.toggle("show");	
   
}

var oldDDMenuID;


window.closeDropDownMenu = function closeDropDownMenu() 
{
	//alert("closeDropDownMenu()");
	if ( dropdowns )
	{
		closeDropDown();
	}
}

window.openInDropDownMenu = function openInDropDownMenu( senderID, receiverID, messageID ) 
{		
	/*
	alert( 	"openInDropDownMenu( \n"+ document.gPREFIX + "\n" +
			"senderID   :" + senderID + "\n" +
			"receiverID :" + receiverID + "\n" +
			"messageID  :" + messageID + "  )" );
	*/
	
	let new_dropdowns = document.getElementById( "ddm" + messageID );
		
	if ( dropdowns )
	{
		closeDropDown();
	}

   	dropdowns = new_dropdowns;
	    
    while (dropdowns.firstChild) 
	{
		dropdowns.removeChild(dropdowns.lastChild);
	}

    for ( let i=0; i<emonjisrc_left.length; i++ )
    {
	    const img = document.createElement('img');
	    
	    img.emonji 		= i;
	    img.src  		= document.gPREFIX + emonjisrc_left[i];
	    img.classList.add( 'emonjis-background-image' );
	    
	    dropdowns.appendChild(img);
	    
	    img.onclick = function() 
	    {		
		//alert("clickOnEmonji");
	    	clickOnEmonji( senderID, receiverID, messageID.toString(), img.emonji );
	    }
    }
                    
    dropdowns.classList.toggle("show");
}


window.closeDropDown = function closeDropDown()
{	
	if ( dropdowns )
	{
		while (dropdowns.firstChild) 
		{
			dropdowns.removeChild(dropdowns.lastChild);
		}
	    
		dropdowns.classList.toggle("show");
		
		dropdowns = null;
	}
}



function clickOnEmonji( senderID, receiverID, messageID, indx )
{
	/*
	alert( 	"clickOnEmonji( \n"+
			"indx       :" + indx + "\n" +
			"senderID   :" + senderID + "\n" +
			"receiverID :" + receiverID + "\n" +
			"messageID  :" + messageID + "  )" );
	*/
			
	let xhr = new XMLHttpRequest();

	xhr.open( "POST", document.gPREFIX + "SetMessageEmonji" );
	
	xhr.setRequestHeader( "Content-Type", "application/json" );
		  
	const data = 
	{
	  type: "JSON",
	};
	
	data.senderID	= senderID;
	data.recipiID	= receiverID;
	data.messageID	= messageID;
	data.whoReacts	= document.gUSER_UID;
	data.emonji		= indx;
		 	
	xhr.onload = () => 
	{		
		//alert( xhr.responseText );
		
		const response 	= JSON.parse( xhr.responseText );
		const obj 		= JSON.parse( response.payload );
																
		var block_to_insert0 = document.getElementById( "ddwn" + messageID );
				
		if ( block_to_insert0 )
		{	
			//alert( "updateObjectWihiDDMenuMessage" );
			
			window.updateObjectWihiDDMenuMessage( obj, block_to_insert0, false );
		}
	}
	
	xhr.send( JSON.stringify(data) );
	
	closeDropDown();  	
}

function doDelete( messageID )
{
	//alert( 	"doDelete:" +  document.gPREFIX );
				
	let xhr = new XMLHttpRequest();

	xhr.open( "POST", document.gPREFIX + "DeleteMessage" );
	
	xhr.setRequestHeader( "Content-Type", "application/json" );
		  
	const data = 
	{
	  type: "JSON",
	};
	
	data.senderID	= document.gUSER_UID;
	data.recipiID	= document.gRECEIVER_ID;
	data.messageID	= messageID;

	//alert( 	document.gUSER_UID + " - " +  document.gRECEIVER_ID + " - " + messageID );

	xhr.onload = () => 
	{		
		//alert( xhr.responseText );
		
		const response 	= JSON.parse( xhr.responseText );		
		const obj 		= JSON.parse( response.payload );
																	
		var block_to_insert0 = document.getElementById( "ddwn" + messageID );
																						
		if ( block_to_insert0 )
		{				
			window.updateObjectWihiDDMenuMessage( obj, block_to_insert0, true );
		}
	}
	
	xhr.send( JSON.stringify(data) );
	
	closeDropDown();  	
}



  
// Close the dropdown if the user clicks outside of it
window.onclick = function(event) 
{
  if (!event.target.matches('.dropbtn')) 
  {	  
	  closeDropDown();
  }
}
