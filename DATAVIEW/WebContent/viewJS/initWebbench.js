/**
 * The following functions are used for webbench initialization. 
 * Opening the DATAVIEW webench triggers these javascript functions.
 * @author  Aravind Mohan, Andrey Kashlev, Changxin Bai.
*/

function createTreeInTheSidebar() {
	delete graphG.getStylesheet().getDefaultEdgeStyle()[mxConstants.STYLE_ENDARROW];
	
	var treeDivLeft = document.createElement('div');
	treeDivLeft.setAttribute("id", "treeboxbox_treeLeft");
	var styleOfTreeLeft = "width: 220px; height:100%; overflow:scroll;";
	treeDivLeft.setAttribute("style", styleOfTreeLeft);
	sidebarLeft.appendChild(treeDivLeft);
	
	var treeDivRight = document.createElement('div');
	treeDivRight.setAttribute("id", "treeboxbox_treeRight");
	var styleOfTreeRight = "width: 220px; height:100%; overflow:scroll;";
	treeDivRight.setAttribute("style", styleOfTreeRight);
	sidebarRight.appendChild(treeDivRight);
	 
	
	
	createTreeLeftAndRight();

	addStubToTree(("outputDP"+ numberOfOutputDataProducts),"Output Stub");
	makeOutputStubDraggable();
	
	createMenu();
};


function convertDropboxItemsToArray(items){
	
	var itemsarray = items.split(",");
	var jsonString = "{\"Dropbox\":[";
	for (temp in itemsarray ) {
		//alert(itemsarray[temp]);
		jsonString += "{\"dataType\":\"Dropbox\",\"dataDescription\":\"" 
	    	+ itemsarray[temp] + "\",\"dataName\":\""
	    	+ itemsarray[temp] + "\"},";
	}
	jsonString = jsonString.replace(/,\s*$/, "");
	jsonString += "]}";
	
	console.log(jsonString);
	var jsonMetadataForDropboxItems = eval('('
			+ jsonString + ')');
	var dropboxMetadata = jsonMetadataForDropboxItems['Dropbox'];
	
	for ( var i= 0; i < getAllKeysFromJSONMap(dropboxMetadata).length; i++) {
		makeDropboxDraggable(dropboxMetadata[i]);
		
	}
		
		
}




function convertWorkflowsItemsToArray(items){
	var itemsarray = items.split(",");
	var jsonString = "{\"Workflows\":[";
	for (temp in itemsarray ) {
		//alert(itemsarray[temp]);
		var tempstr = itemsarray[temp].substring(itemsarray[temp].lastIndexOf("/")+1, itemsarray[temp].length+1);
		//console.log(tempstr);
		jsonString += "{\"dataType\":\"Workflows\",\"dataName\":\"" 
	    	+ tempstr + "\",\"dataPath\":\""
	    	+ itemsarray[temp] + "\"},";
	}
	jsonString = jsonString.replace(/,\s*$/, "");
	jsonString += "]}";
	var jsonMetadataForDropboxItems = eval('('
			+ jsonString + ')');
	var dropboxMetadata = jsonMetadataForDropboxItems['Workflows'];
	for ( var i= 0; i < getAllKeysFromJSONMap(dropboxMetadata).length; i++) {
		var dropboxMetadataItem = dropboxMetadata[i];
		//console.log(dropboxMetadataItem);
//		var id = document.getElementById(dropboxMetadataItem.dataPath);
//		id.addEventListener("click", function() {
//			displayWorkflowOnCanvas(dropboxMetadataItem); 
//        })
		//
		
		makeWorkflowDraggable(dropboxMetadataItem);
		
	}
	
	
}


function convertTaskItemsToArray(items){
	//console.log(items);
	var itemsarray = items.split(",");
	var jsonString = "{\"Tasks\":[";
	for (temp in itemsarray ) {
		//alert(itemsarray[temp]);
		var tempstr = itemsarray[temp].substring(itemsarray[temp].lastIndexOf("/")+1, itemsarray[temp].lastIndexOf("."));
		//console.log(tempstr);
		jsonString += "{\"dataType\":\"Tasks\",\"dataDescription\":\"" 
	    	+ tempstr + "\",\"dataName\":\""
	    	+ itemsarray[temp] + "\"},";
	}
	jsonString = jsonString.replace(/,\s*$/, "");
	jsonString += "]}";
	
	//console.log(jsonString);
	var jsonMetadataForDropboxItems = eval('('
			+ jsonString + ')');
	var dropboxMetadata = jsonMetadataForDropboxItems['Tasks'];
	for ( var i= 0; i < getAllKeysFromJSONMap(dropboxMetadata).length; i++) {
		var dropboxMetadataItem = dropboxMetadata[i];
		var obj = getPortsNumber(dropboxMetadata[i]);
		dropboxMetadataItem.inputPorts = obj.inputPorts;
		dropboxMetadataItem.outputPorts = obj.outputPorts;

		makeTaskDraggable(dropboxMetadataItem);
		
		
	}
}






function addWFToSidebarTreeAndMakeDraggable(wfName, runAfterwards) {
		//focusOnItemInTree(wfName);
		if (runAfterwards)
			run();

}

function getAllKeysFromJSONMap(jsonMap) {
	var keys = [];
	for ( var key in jsonMap) {
		if (jsonMap.hasOwnProperty(key)) {
			keys.push(key);
		}
	}
	return keys;
}

function ajaxCall(params, responseHandler, argArray) {
	var url = "./Mediator?";
	if (window.XMLHttpRequest) {
		xmlhttp = new XMLHttpRequest();
	} else {
		xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
	}
	xmlhttp.onreadystatechange = function() {
		if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
			// alert("xmlhttp.responseText: " + xmlhttp.responseText);
			responseHandler(argArray, xmlhttp.responseText);
		}
	};
	xmlhttp.open("POST", "./Mediator?", true);
	xmlhttp.setRequestHeader("Content-type",
			"application/x-www-form-urlencoded");
	xmlhttp.send(params);
	
};
