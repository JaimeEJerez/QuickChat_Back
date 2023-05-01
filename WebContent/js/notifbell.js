

document.onclick = function(e)
{
    document.getElementById("notif-dropdown").style.display='none';
}
    
window.addNotifNumber = function addNotifNumber()
{
	let notifNumberElement = document.getElementById( "notification-bell-number" );
	
	if ( notifNumberElement )
	{
		let num = parseInt( notifNumberElement.innerHTML ) + 1;
		
		notifNumberElement.innerHTML = num.toString();
	}
}

window.removeNotifNumber = function removeNotifNumber()
{
	let notifNumberElement = document.getElementById( "notification-bell-number" );
	
	if ( notifNumberElement )
	{
		let num = parseInt( notifNumberElement.innerHTML ) - 1;
		
		notifNumberElement.innerHTML = num.toString();
	}
}

window.openNotifDropdown = function openNotifDropdown()
{
	window.closeDropDownMenu();
	
	document.getElementById('notif-dropdown').style.display='block';
}

window.showNotif = function showNotif( groupName, groupID, messageID )
{	
	let messageElem = document.getElementById( "ddm" + messageID );

	if ( messageElem )
	{		
		messageElem.scrollIntoView({behavior: "smooth", block: "start"});
	}
	else
	{
		window.reload_at( groupID, groupName, messageID ); 
	}
}

window.deleNotif = function deleNotif( userID, notifID )
{
	//alert( "deleNotif(" + userID + "," + notifID + ")" );
					
	let xhr = new XMLHttpRequest();

	xhr.open( "POST", document.gPREFIX + "DeleteNotif" );
	
	xhr.setRequestHeader( "Content-Type", "application/json" );
		  
	const data = 
	{
	  type: "JSON",
	};
	
	data.userID		= userID;
	data.notifID	= notifID;
		 	
	xhr.onload = () => 
	{		
		//alert( xhr.responseText );
														
		var element = document.getElementById( "ntf" + notifID );
			
		if ( element ) 
		{
			//alert( "A" );
			
			if ( element.parentNode )
			{
				//alert( "B" );
				
  				element.parentNode.removeChild(element);
  			}
		}
		
		window.removeNotifNumber();
	}
	
	xhr.send( JSON.stringify(data) );
}

/*
<div id="notif-dropdown" class="notif-dropdown-content">
	<div id="ntf611" class="notif-dropdown-div">
		<a onclick="showNotif( 'All Users','GR000001','1417' )">Usted ha sido mencionado por Manuel en el grupo 'All Users' a las 02-2-2023 11:28</a>
		<b onclick="deleNotif( 'df9d3f82-8e89-4698-8b37-866962882a31','611' )">&#x274C</b>
	</div>
</div>
*/
window.addNotifElement = function addNotifElement( obj )
{	
	let notif_dropdown 		= document.getElementById("notif-dropdown");
	var notif_dropdown_div 	= document.createElement('div');
	let element_a 			= document.createElement("a");
	let element_b 			= document.createElement("b");

	notif_dropdown_div.id = "ntf" + obj.id;
	notif_dropdown_div.classList.add('notif-dropdown-div');
	
	element_a.innerHTML = obj.content.message;
	element_b.innerHTML = "&#x274C";
		
	const c1 = obj.content.value1;
	const c2 = obj.content.value2;
	const c3 = obj.content.value3;
	element_a.onclick = function() { showNotif( c1, c2 ,c3 ); };
	
	const c4 = obj.receiverID;
	const c5 = obj.id;
	element_b.onclick = function() { deleNotif( c4, c5 ); };
	
	notif_dropdown_div.appendChild( element_a );
	notif_dropdown_div.appendChild( element_b );
	notif_dropdown.appendChild( notif_dropdown_div );
}


