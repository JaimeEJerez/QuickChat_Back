/**
 * 
 */

const validFileTypes = [".png",".jpg",".bmp",".gif",".pdf",".zip",".rar"];
const kPNG	= 0;
const kJPG	= 1;
const kBMP	= 2;
const kGIF	= 3;
const kPDF	= 4;
const kZIP	= 5;
const kRAR	= 6;


var popup;
var append_backdrop;
var progressDot;
var cancelBtn;
var cancelBtn;
var parragraph;
var progress;
	
var recordRow;
var sendBtn;
var fileName;

var appendButton = document.getElementById("appendButton");

var gSENDER_ID;
var file2send;

appendButton.onclick = function() 
{
	append_drawAll();
}


function isValidFile( fName )
{
	let lowerCase = fName.toLowerCase();
	
	for (var i = 0; i < validFileTypes.length; i++)
	{
		if ( lowerCase.includes( validFileTypes[i] ) )
		{
			return true;
		}
	}
	
	return false;
}

function isImageName( fName )
{
	let lowerCase = fName.toLowerCase();
	
	return 	lowerCase.includes( validFileTypes[kPNG] ) ||
			lowerCase.includes( validFileTypes[kJPG] ) ||
			lowerCase.includes( validFileTypes[kBMP] ) ||
			lowerCase.includes( validFileTypes[kGIF] );
}

function sendMessage()
{	
	const isImage = isImageName( file2send );
					
	alert( "sendMessage( '" + file2send + "' ) / isImage : " + isImage );
	
	var USER_UUID 			= document.gUSER_UID;
  	var USER_DISPLAY_NAME 	= document.gUSER_NAME;
  	     
  	if ( isImage )
  	{ 	    
		const msg = {
	    	type:"IMAGE_MSG",
		    senderType:"kSingleUser",
		  };
				
		msg.receiverName	= document.gRECEIVER_NAME;
		msg.receiverID		= document.gRECEIVER_ID;
		msg.receiverType  	= document.gRECEIVER_TYPE;
		msg.file2send		= file2send;
		msg.senderName		= USER_DISPLAY_NAME;
		msg.senderID		= USER_UUID;
		
  		// Send the msg object as a JSON-formatted string.
  		if ( window.getConnection() )
  		{
			//alert( "connection.send( JSON.stringify(msg) )" );
	
			window.getConnection().send( JSON.stringify(msg) ); 
		}
		else
		{
			alert("ERROR: Not conection opened...")
		}
	}
	else
	{
		const msg = {
	    	type:"DOCUMENT_MSG",
		    senderType:"kSingleUser",
		  };
		
		msg.receiverName	= document.gRECEIVER_NAME;
		msg.receiverID		= document.gRECEIVER_ID;
		msg.receiverType  	= document.gRECEIVER_TYPE;
		msg.file2send		= file2send;
		msg.senderName		= USER_DISPLAY_NAME;
		msg.senderID		= USER_UUID;
		
  		// Send the msg object as a JSON-formatted string.
  		if ( window.getConnection() )
  		{
			//alert( "connection.send( JSON.stringify(msg) )" );
	
			window.getConnection().send( JSON.stringify(msg) ); 
		}
		else
		{
			alert("ERROR: Not conection opened...")
		}

	}
	
	append_close();
}

window.append_close = function append_close() 
{
	document.body.removeChild( append_backdrop );
}

window.append_drawAll = function append_drawAll() 
{
	popup = document.createElement("div");
	popup.className = "vmsg-popup";
	popup.addEventListener("click", (e) => e.stopPropagation());

	append_backdrop = document.createElement("div");
	append_backdrop.className = "vmsg-backdrop";
	append_backdrop.addEventListener("click", () => append_close() );

	append_backdrop.appendChild(popup);

	document.body.appendChild(append_backdrop);

	recordRow = document.createElement("div");
	recordRow.style.width 		= "100%";
	recordRow.style				= "display: flex; justify-content: flex-end"

	var dropDiv = document.createElement("div");
	dropDiv.style.height 	= "300px";
	dropDiv.style.width 	= "100%";
	dropDiv.className 		= "dropDiv";
	popup.appendChild(dropDiv);

	dropDiv.addEventListener("dragenter", noop, false);
	dropDiv.addEventListener("dragexit", noop, false);
	dropDiv.addEventListener("dragover", noop, false);
	dropDiv.addEventListener("drop", dropUpload, false);

	parragraph = document.createElement("p");
	parragraph.innerHTML = 'Soltar un archivo o imágen<br>en este recuadro,<br>son válidos:<br><b>.png .jpg .bmp .gif o .pdf</b><br>para grupos de archivos<br>u otro tipo utilize<br><b>.zip o .rar</b>';
	parragraph.style.textAlign = "center";
	dropDiv.appendChild(parragraph);
	
	progress = document.createElement("p");
	progress.innerHTML = '0%';
	progress.style.visibility	="hidden";
	progress.style.textAlign 	= "center"
	dropDiv.appendChild(progress);

//<img  id="sendButton"   	src="assets/img/send.png"   alt="buttonpng" border="0" class="ghost size01">
//<div id="record" class="size01">
//	<img  id="sendButton"   	src="assets/img/send.png"   alt="buttonpng" border="0" class="ghost size01">
//	<img  id="recordButton" 	src="assets/img/record.png" alt="buttonpng" border="0" class="size01">
//</div>
			
	sendBtn 			= document.createElement("button");
	let	sendImg 		= document.createElement("img");
	sendImg.src 		= document.gPREFIX + "assets/img/send.png"
	sendImg.className 	= "size01";
	sendBtn.appendChild(sendImg);
	
	sendBtn.className 	= "size01";
	sendBtn.disabled 	= true;
	sendBtn.id			= "record";

	sendBtn.addEventListener("click", () => sendMessage());
	
	recordRow.appendChild(sendBtn);

	//cancelBtn = document.createElement("button");
	//cancelBtn.className = "vmsg-button vmsg-record-button";
	//cancelBtn.textContent = "Cancelar";
	//cancelBtn.addEventListener("click", () => this.close(this.recorder.blob));
	//recordRow.appendChild(cancelBtn);

	popup.appendChild(recordRow);

	popup.appendChild(gainWrapper);
}

function noop(event) 
{
	event.stopPropagation();
	event.preventDefault();
}

function dropUpload(event) 
{
	noop(event);
	
	var files = event.dataTransfer.files;

	if (files.length > 0) 
	{
		upload(files[0]);
	}
}

function upload( file ) 
{
	fileName = file.name;
	
	if ( !isValidFile( fileName ) )
	{
		alert( "\r\nTIPO DE ARCHIVO INVALIDO.\r\n"  );
	
		return;
	}
	
	progress.innerHTML = 'Subiendo archivo<br>' + file.name + '<br>0%';
	progress.style.visibility="visible";
	
	var formData = new FormData();
	
	//alert( file.name );
	
	formData.append( "file", file );
	formData.append( "userID", document.gUSER_UID );

	var xhr = new XMLHttpRequest();
	xhr.upload.addEventListener("progress", uploadProgress, false);
	xhr.addEventListener("load", uploadComplete, false);
	xhr.open("POST", document.gPREFIX + "UploadTemporalFile", true); // If async=false, then you'll miss progress bar support.
	xhr.send(formData);
	
	xhr.onreadystatechange = function() 
	{
  		if (this.readyState == 4 && this.status == 200) 
  		{
    		//alert(this.responseText);
    		
    		file2send = this.responseText;
  		}
	};
}

function uploadProgress(event) 
{
	// Note: doesn't work with async=false.
	var pgrs = Math.round(event.loaded / event.total * 100);
		
	progress.innerHTML = 'Subiendo archivo<br>' + fileName + '<br>' + pgrs + '%';
}

function uploadComplete(event) 
{	
	progress.innerHTML = 'Subiendo archivo<br>' + fileName + '<br>100%';
	
	sendBtn.disabled 	= false;
	/*
	if (!event.target.responseText.startsWith("OK")) 
	{
		document.getElementById("status").innerHTML = event.target.responseText;
	}
	else 
	{
		document.getElementById("status").innerHTML = "";

		setAvatarImage("GetAvatarImage");
	}
	*/
}

function setAvatarImage(src) 
{
	var d = new Date();
	var n = d.getTime();

	src = src + "?time:" + n;

	document.getElementById('avatar').src = document.gPREFIX + src;
}

