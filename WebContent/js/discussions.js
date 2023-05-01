/**
 * 
 */
 
 var gLastActiveDiscussion;
 

window.loadDiscussion = function loadDiscussion( registryUID , securityToken, discussions_section )
{
	//alert( "window.loadDiscussion" );
	
	let xhr = new XMLHttpRequest();

	xhr.open( "POST", document.gPREFIX + "GetEnrichedContacts" );
	
	xhr.setRequestHeader( "Content-Type", "application/json" );
		  
	const data = 
	{
	  type: "JSON",
	};
	
	data.registryUID	= registryUID;
	data.sqToquen		= securityToken;
		 	
	xhr.onload = () => 
	{				
		const response 			= JSON.parse( xhr.responseText );
		const discussionsArr	= response.payload;
																												
		if ( discussions_section )
		{				
			for (var i = 0; i < discussionsArr.length; i++) 
			{     			
    			window.insertDiscussion( discussionsArr[i], discussions_section );
			}
		}
	}
	
	xhr.send( JSON.stringify(data) );
}

function activeDiscussion( userID, userName, self )
{
	if ( gLastActiveDiscussion )
	{
		gLastActiveDiscussion.classList.remove( 'message-active' );
	}
	
	gLastActiveDiscussion = self;
	
	self.classList.add( 'message-active' );
	
	window.loadChatContent( userID, userName );
	
	let elpID = "ELP_" + userID;
	
	let elpElement = document.getElementById(elpID);

	if ( elpElement )
	{
		elpElement.style.background = "rgb(255,255,255)";
	}

}

window.insertDiscussion = function insertDiscussion( obj, block_to_insert0 )
{
	var 	userID 		= obj.userID;
	var 	userName 	= obj.userName;
	var 	imageURL;// 	= obj.imageURL;
	var 	time 		= obj.time;
	var 	message 	= obj.message;
	var 	elapsed 	= obj.elapsed;
	var 	isActive 	= obj.isActive;
	var 	isOnline 	= obj.isOnline;
	 
	if ( userID.startsWith("G") )
	{
		imageURL = "../assets/img/group2.png";
	}
	else
	{
		imageURL = "../assets/img/user2.png";
	}
			
	/*
	<div class="discussion"  onClick="reload( 'GR000001','All Users' );" >
		<div class="photo" style="background-image: url(assets/img/group2.png);">
			<div id="ONL_GR000001" class="desc-contact"></div>
		</div>
		<div class="desc-contact">
			<p class="name">All Users</p>
			<p id="MSG_GR000001" class="message"></p>
		</div>
		<p hidden id="TIM_GR000001">1676596779686</p>
		<div id="ELP_GR000001" class="timer">Nada</div>
	</div>
	 */

	var discu_div = document.createElement("div");
	discu_div.classList.add('discussion');
	//if ( isActive == "true" )
	//{
	//	discu_div.classList.add( 'message-active' );
	//}
	discu_div.onclick = function() { activeDiscussion( userID, userName, this ); };

	var photo_div = document.createElement("div");
	photo_div.classList.add('photo');
	photo_div.style.backgroundImage = "url('" + imageURL + "')";

	var onlin_div = document.createElement("div");
	onlin_div.id = "ONL_" + userID;
	
	if ( isOnline == "true" )
	{
		onlin_div.classList.add('online');
	}
	else
	{
		onlin_div.classList.add('offline');
	}
	
	onlin_div.classList.add('offline');
	
	photo_div.appendChild(onlin_div);
	discu_div.appendChild(photo_div);	

	var cntct_div = document.createElement("div");
	cntct_div.classList.add('dcntct-div');
	
	var	name_prph = document.createElement("p");
	name_prph.innerHTML = userName;
	name_prph.classList.add('name');
	
	var	mssg_prph = document.createElement("p");
	mssg_prph.id = "MSG_" + userID;
	mssg_prph.innerHTML = message;
	mssg_prph.classList.add('discussions-message');

	cntct_div.appendChild(name_prph);
	cntct_div.appendChild(mssg_prph);

	var	hddn_prph = document.createElement("p");
	hddn_prph.style.display = "none";
	hddn_prph.id = "TIM_" + userID;
	hddn_prph.innerHTML = time;
	
	var elpsd_div = document.createElement("div");
	elpsd_div.classList.add('timer');
	elpsd_div.id = "ELP_" + userID;
	elpsd_div.innerHTML = elapsed;
	
	discu_div.appendChild( photo_div );
	discu_div.appendChild( cntct_div );
	discu_div.appendChild( hddn_prph );
	discu_div.appendChild( elpsd_div );
	
	block_to_insert0.appendChild( discu_div );
}

  