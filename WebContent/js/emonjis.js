/**
 * 
 */

const emonjisrc = [ "assets/img/emon01.png", 
					"assets/img/emon02.png", 
					"assets/img/emon03.png", 
					"assets/img/emon04.png", 
					"assets/img/emon05.png", 
					"assets/img/emon06.png" ];

//const emonjis = [ "&#x1F9E1", "&#128077", "&#128078", "&#128170", "&#x1F44F", "&#x1F595", "&#x1F60A", "&#x274C" ];

window.getReactionDiv = function getReactionDiv( obj )
{	
	let split  			= obj.reactions.split(",");
	let reactionsDiv 	= document.createElement( 'div' );
	
	reactionsDiv.classList.add('reactions-content');
	
	reactionsDiv.id='ddm" + messageID + "';

	for ( let i=0; i<emonjisrc.length; i++ )
	{		
	    let intVal = parseInt( split[i], 16 );
	    	    
	    if ( intVal > 0 )
	    {		 
			let div 	= document.createElement( 'div' );
			div.classList.add( 'emonjis-center-aligned' );
			
			let img 	= document.createElement( 'img' );
			img.classList.add( 'emonjis-background-image' );
			img.src 	= document.gPREFIX + emonjisrc[i];
						
			let num 	= document.createElement( 'div' );
			num.classList.add( 'emonjis-text' );
			num.innerHTML = intVal;
			
			div.appendChild( img );
			div.appendChild( num );
			
	        reactionsDiv.appendChild( div );
	    }
	}
				
	return reactionsDiv;
}			
