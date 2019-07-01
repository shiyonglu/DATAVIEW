/**
 * The following functions are used to create and manage the 
 * treeview for both workflow and data product repository.
 * @author  Aravind Mohan, Andrey Kashlev.
*/
var tvAppKey = "";
var tvAppSecret = "";
var tvAppToken = "";

function createTreeLeftAndRight(){
	var userIDForTreeView = document.getElementById("usrId").value;
	treeLeft = new dhtmlXTreeObject("treeboxbox_treeLeft", "100%", "92%", 0);
	treeLeft.deleteChildItems(0);
	
	treeLeft.setSkin('dhx_material');
	treeLeft.setImagePath("./viewJS/dhtmlxTree/codebase/imgs/dhxtreeview_material/");
	treeLeft.enableTreeImages(true);
	treeLeft.enableDragAndDrop(false);
	
	
	
	treeRight = new dhtmlXTreeObject("treeboxbox_treeRight", "100%", "92%", 0);
	treeRight.deleteChildItems(0);
	
	treeRight.setSkin('dhx_material');
	treeRight
			.setImagePath("./viewJS/dhtmlxTree/codebase/imgs/dhxtreeview_material/");
	treeRight.enableTreeImages(true);
	treeRight.enableDragAndDrop(false);
	
	getDropboxInfo(userIDForTreeView);

	
}




function getDropboxInfo(userID){
var params = "action=getDropboxDetails&userId="+ userID;
	
	function responseHandler(argArray, jsonMetadataFromRepositoryAsString){
		var jsonMetadataFromRepository = eval('(' + jsonMetadataFromRepositoryAsString + ')');
		var tvAppToken = jsonMetadataFromRepository['token'];
		//tvAppToken = tvAppToken + lsOfDropbox[0];
		
		if(tvAppToken.length == 0){
			alert("Dropbox Token Should Be Filled to Show Tasks and Workflows");
		}
		
		
		else{
			
			treeLeft.setXMLAutoLoading("http://40.117.212.81/JavaBridge/dhtmltree/php/readDropbox.php?id=0&apptoken="
					+ tvAppToken 
					+ "&name=/dataview/tasks");
			treeLeft.setDataMode("json");
			
			treeLeft.load("http://40.117.212.81/JavaBridge/dhtmltree/php/readDropbox.php?id=0&apptoken="
					+ tvAppToken + "&name=/dataview/tasks","json");
			
			
			treeLeft.setXMLAutoLoading("http://40.117.212.81/JavaBridge/dhtmltree/php/readDropbox.php?id=0&apptoken="
					+ tvAppToken 
					+ "&name=/dataview/workflows");
			treeLeft.setDataMode("json");
			
			treeLeft.load("http://40.117.212.81/JavaBridge/dhtmltree/php/readDropbox.php?id=0&apptoken="
					+ tvAppToken + "&name=/dataview/workflows","json");
			
			
			treeRight.setXMLAutoLoading("http://40.117.212.81/JavaBridge/dhtmltree/php/readDropbox.php?id=0&apptoken="
					+ tvAppToken + "&name=");
			
			treeRight.setDataMode("json");
			treeRight.load("http://40.117.212.81/JavaBridge/dhtmltree/php/readDropbox.php?id=0&apptoken="
					+ tvAppToken + "&name=","json");
			treeRight.attachEvent("onXLS", function(id){
				setTimeout(function() {
					convertDropboxItemsToArray(getAllDropboxItemsFromTree('Dropbox'));
				}, 3000);
			});
			
			
			
		}		
	}
		
	ajaxCall(params, responseHandler, null);
	
	
	
}





function getPortsNumber(dropboxMetadata){
	var params = "action=getPortsNumber&filename="+ dropboxMetadata.dataName;
	var url = "./Mediator?";
	if (window.XMLHttpRequest) {
		xmlhttp = new XMLHttpRequest();
	} else {
		xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
	}
	xmlhttp.onreadystatechange = function() {
		if (xmlhttp.readyState == 4 && xmlhttp.status == 200) {
			// alert("xmlhttp.responseText: " + xmlhttp.responseText);
			responseHandler(null, xmlhttp.responseText);
		}
	};
	xmlhttp.open("POST", "./Mediator?", false);
	xmlhttp.setRequestHeader("Content-type",
			"application/x-www-form-urlencoded");
	xmlhttp.send(params);
	var obj 
	
function responseHandler(argArray, jsonMetadataFromRepositoryAsString){
		obj = JSON.parse(jsonMetadataFromRepositoryAsString);	
	}	
	return obj;
}



function addPortsToTree() {
	addPortToSidebarTree("InputPort", "Input Port");
	makePortDraggable("InputPort");

	addPortToSidebarTree("OutputPort", "Output Port");
	makePortDraggable("OutputPort");
}


function addReusableWorkflowToTree(id, textInSidebarTree, isCorrect) {
	treeLeft.insertNewItem("Reusable", id, "<span id=\"" + id
			+ "\" iscorrect=\"" + isCorrect
			+ "\" ondblclick=\"editWorkflow(this.parentNode.innerHTML)\"> "
			+ textInSidebarTree + " </span>", 0, 0, 0, 0, '');

}

function addSharedReusableWorkflowToTree(id, textInSidebarTree, isCorrect) {
	var styleShared = "color:blue;font-weight:bold";
	treeLeft.insertNewItem("Reusable", id, "<span id=\"" + id + "\" style=\""
			+ styleShared + "\" iscorrect=\"" + isCorrect
			+ "\" ondblclick=\"editWorkflow(this.parentNode.innerHTML)\"> "
			+ textInSidebarTree + " </span>", 0, 0, 0, 0, '');

}
function addExecutableWorkflowToTree(id, textInSidebarTree, isCorrect) {
	treeLeft.insertNewItem("Executable", id, "<span id=\"" + id
			+ "\" iscorrect=\"" + isCorrect
			+ "\" ondblclick=\"editWorkflow(this.parentNode.innerHTML)\"> "
			+ textInSidebarTree + " </span>", 0, 0, 0, 0, '');

}
function addSharedExecutableWorkflowToTree(id, textInSidebarTree, isCorrect) {
	var styleShared = "color:blue;font-weight:bold";
	treeLeft.insertNewItem("Executable", id, "<span id=\"" + id + "\" style=\""
			+ styleShared + "\" iscorrect=\"" + isCorrect
			+ "\" ondblclick=\"editWorkflow(this.parentNode.innerHTML)\"> "
			+ textInSidebarTree + " </span>", 0, 0, 0, 0, '');

}
function addStubToTree(id, textInSidebarTree) {
	treeRight.insertNewItem("0", id, "<span id=\"" + id +  "\"> "
			+ textInSidebarTree + " </span>", 0, 0, 0, 0, '');
	treeRight.setItemColor(id, "red", "red");
	
}

function addDataProductToTree(id, textInSidebarTree) {
	treeRight.insertNewItem("Data", id, "<span id=\"" + id + "\"> "
			+ textInSidebarTree + " </span>", 0, 0, 0, 0, '');

}

function addPortToSidebarTree(id, textInSidebarTree) {
	treeLeft.insertNewItem("IOPorts", id, "<span id=\"" + id + "\"> "
			+ textInSidebarTree + " </span>", 0, 0, 0, 0, '');

}

function test() {
	alert("tree.getSelectedItemId(): " + tree.getSelectedItemId());
}

function getAllReusableWorkflows() {
	return treeLeft.getSubItems("Workflows");
}

function getAllExecutableWorkflows() {
	return treeLeft.getSubItems("Executable");
}

function getAllDataProducts() {
	var allDPs = [];
	var allSubItems = treeRight.getSubItems("0");
	allDPs = allSubItems.split(",");
	return allDPs;
}
function getAllDropboxItemsFromTree(root){
	
	return treeRight.getAllSubItems(root);
}

function getAllTasksItemsFromTree(root){
	
	return treeLeft.getAllSubItems(root);
}

function getAllWorkflowItemsFromTree(root){
	
	return treeLeft.getAllSubItems(root);
}



function focusOnItemInTree(itemName) {
	treeLeft.selectItem(itemName);
	treeLeft.focusItem(itemName);

	treeRight.selectItem(itemName);
	treeRight.focusItem(itemName);

}
function searchLeft(itemName) {
	// Here we are defining a syntax for our search commands
	if (itemName.startsWith("/man")) {
		alert("test");
		treeLeft.deleteChildItems(0);
		var xmlStr = '<?xml version="1.0" encoding="iso-8859-1"?><treeLeft id="0"><item text="&lt;b&gt;Commands&lt;/b&gt;" id="Commands"></item></treeLeft>';
		treeLeft.loadXMLString(xmlStr);
		treeLeft.insertNewItem("Commands","Command1" , "<span id=\"" + "Command1" + "\"> "
				+ "/chat USERID TEXT" + " </span>", 0, 0, 0, 0, '');
		treeLeft.insertNewItem("Commands","Command2" , "<span id=\"" + "Command2" + "\"> "
				+ "/feedback TEXT" + " </span>", 0, 0, 0, 0, '');
		
	}
	else if (itemName.startsWith("/chat")) {
		// Get the text entered by the user and deliver it to the friend
		var userId = itemName.substring(5, itemName.indexOf("\"") - 1);
		var indexOfChat = itemName.indexOf("\"");
		itemName = itemName.substring(indexOfChat + 1) + "<br>";
		itemName = itemName.substring(0, itemName.length - 5);
		// Write logic to insert feedback into table
		var params = "action=insertChatInfo&message=" + itemName + "&recinfo=" + userId ;
		function responseHandler(argArray, responseFromServer) {
			updateStatus("Chat message sent successfully");
		}
		ajaxCall(params, responseHandler, null);
		
	} else if (itemName.startsWith("/feedback")) {
		// Get the text entered by the user and insert into the feedback table 
		var indexOfFeedback = itemName.indexOf("\"");
		itemName = itemName.substring(indexOfFeedback + 1) + "<br>";
		itemName = itemName.substring(0, itemName.length - 5);
		// Write logic to insert feedback into table
		var params = "action=insertFeedbackInfo&feedback=" + itemName;
		function responseHandler(argArray, responseFromServer) {
			updateStatus("Feedback sent successfully");
		}
		ajaxCall(params, responseHandler, null);
	} else if (itemName.startsWith("/milestone")) {
		// Get the text entered by the user and insert into the milestone table 
		var indexOfMilestone = itemName.indexOf("\"");
		itemName = itemName.substring(indexOfMilestone + 1) + "<br>";
		itemName = itemName.substring(0, itemName.length - 5);
		// Write logic to insert milestone into table
		var params = "action=insertMilestoneInfo&milestone=" + itemName;
		function responseHandler(argArray, responseFromServer) {
			updateStatus("Milestone sent successfully");
		}
		ajaxCall(params, responseHandler, null);
	} else {
		// This section of the code is to search for partial keyword
		treeLeft.deleteChildItems(0);
		var xmlStr = '<?xml version="1.0" encoding="iso-8859-1"?><treeLeft id="0"><item text="&lt;b&gt;I/O Ports&lt;/b&gt;" id="IOPorts"></item></treeLeft>';
		treeLeft.loadXMLString(xmlStr);
		treeLeft.insertNewNext("IOPorts", "Workflows", "<b>Workflows</b>", 0, 0,
				0, 0, '');
		treeLeft.insertNewItem("Workflows", "Reusable", "Reusable", 0, 0, 0, 0,
				'');
		treeLeft.insertNewItem("Workflows", "Executable", "Executable", 0, 0,
				0, 0, '');
		addPortsToTree();
		filterWorkflows(itemName);
		setTimeout(function() {
			filterSharedWorkflows(itemName);
		}, 2000);
		treeLeft.selectItem(itemName);
		treeLeft.focusItem(itemName);

	}

}
function searchRight(itemName) {
	treeRight.deleteChildItems(0);
	var xmlStr = '<?xml version="1.0" encoding="iso-8859-1"?><treeRight id="0"><item text="&lt;b&gt;Data&lt;/b&gt;" id="Data"></item></treeRight>';
	treeRight.loadXMLString(xmlStr);

	filterDataProducts(itemName);
	treeRight.selectItem(itemName);
	treeRight.focusItem(itemName);
}
function updateItem(itemId, newText) {
	treeLeft.setItemText(itemId, newText);
	treeRight.setItemText(itemId, newText);

}

function updateIsCorrectAttribute(workflowName, isCorrectNewValue) {
	var oldText = treeLeft.getItemText(workflowName);
	var beforeIsCorrectDigit = oldText.substring(0, oldText
			.indexOf("iscorrect=\"") + 11);
	var afterIsCorrectDigit = oldText.substring(
			oldText.indexOf("iscorrect=\"") + 12, oldText.length);
	var newText = beforeIsCorrectDigit + isCorrectNewValue
			+ afterIsCorrectDigit;
	treeLeft.setItemText(workflowName, newText);
}

function deleteWorkflowFromTree(workflowName) {
	treeLeft.deleteItem(workflowName, false);
}
