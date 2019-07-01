/**
 * The following functions are used to manage the 
 * webench amd wprkflow engine interactions. 
 * @author  Aravind Mohan, Andrey Kashlev, Mahdi Ebrahimi.
*/

function workflowRunConfigureDetail(userid){
	if (document.getElementById('wfrunconfig_executiontype').value == "EC2-Cloud") {
		
		var params = "action=getCloudSettingDetails&userId="+ userid;
		function responseHandler(argArray, jsonMetadataFromRepositoryAsString) {
			var jsonMetadataFromRepository = eval('(' + jsonMetadataFromRepositoryAsString + ')');
			var cloudAccessKey = jsonMetadataFromRepository['accessKey'];
			var cloudSecretKey = jsonMetadataFromRepository['secretKey'];
			var key = encodeURIComponent(cloudSecretKey)
			if(cloudAccessKey.length == 0||cloudSecretKey.length==0){
				alert("AccessKey and SecretKey Should Be Filled");
			}else{
				
				createRunningWorkflowSplash();
				
				// Write logic to store cloud vm settings table
				
				var params = "action=provisionVMsInEC2AndRunWorkflows&userID=" + userid
				+ "&name=" + currentWorkflowName 
				+ "&accessKey=" + cloudAccessKey
				+ "&secretKey=" + key;
				function responseHandler(argArray,
						response) {
					
				var allStubNames = getAllStubNames();	
				
				for (var i = 0; i < allStubNames.length; i++) {
					fillStubWithNewDP(allStubNames[i]);
				}
				removeRunningWorkflowSplash();
				var params = "action=initializeUserFolder";

				function responseHandler(argArray, someObject) {
					createTreeInTheSidebar();
					generateUniqueRunID();
				}
				ajaxCall(params, responseHandler, null);
				}
				ajaxCall(params, responseHandler, null);
			}
			
		}
		ajaxCall(params, responseHandler, null);

		
	}else{
		console.log("run the serverrun workflow");
		createRunningWorkflowSplash();
		var params = "action=serverRunWorkflows&userID=" + userid + "&name=" + currentWorkflowName ;
		function responseHandler(argArray,
				response) {
			
		var allStubNames = getAllStubNames();	
		
		for (var i = 0; i < allStubNames.length; i++) {
			fillStubWithNewDP(allStubNames[i]);
		}
		removeRunningWorkflowSplash();
		var params = "action=initializeUserFolder";

		function responseHandler(argArray, someObject) {
			createTreeInTheSidebar();
			generateUniqueRunID();
		}
		ajaxCall(params, responseHandler, null);
		}
		
		ajaxCall(params, responseHandler, null);
		
		
	}
	
	
		 
}

function workflowRunConfigure() {
  $(function() {
			 var paramUserID = $("#usrId").val();
		    $("#WorkflowRunConfigDiv").data('paramWFRConfig',{userID:paramUserID}).dialog(
		    		{
		    	        dialogClass: 'DynamicDialogStyle',
		    	        autoOpen: true,
		    	        modal: true,
		    	        buttons: { "Submit": function () {
		    	        	workflowRunConfigureDetail($(this).data("paramWFRConfig").userID);
		    	        	$("#WorkflowRunConfigDiv").dialog("close");	
		    	        }},
		    	        height:190,
		    	        width: 400,
		    	        title: 'Workflow Execution Configuration:'

		    	    }		
		    );
		  });
}


function run() {
	workflowRunConfigure();
}

function addNewDataProductsToSidebar() {
	var params = "action=getAllDPsMetadata";

	function responseHandler(argArray, response) {
		var jsonMetadataFromRepository = eval('(' + response + ')');

		for (var i = 0; i < getAllKeysFromJSONMap(jsonMetadataFromRepository).length; i++) {
			var currDPName = jsonMetadataFromRepository[i]['dataName'];
			if (!dpNameIsInSidebarAlready(currDPName)) {
				addDataProductToTree(currDPName, currDPName);
				makeDPDraggable(jsonMetadataFromRepository[i]);
				focusOnItemInTree(currDPName);
			}
		}

	}
	ajaxCall(params, responseHandler, null);
}

function dpNameIsInSidebarAlready(dpName) {
	var allDPNamesInTree = getAllDataProducts();
	for (var i = 0; i < allDPNamesInTree.length; i++) {
		if (allDPNamesInTree[i].length == dpName.length
				&& allDPNamesInTree[i].indexOf(dpName) != -1)
			return true;
	}
	return false;
}

function generateUniqueRunID() {
	allRuns = getAllRuns();
	var runID = document.getElementById('runIDInput').value;
	if (runID.trim().length == 0)
		document.getElementById('runIDInput').value = 0;

	if (runIDIsUnique(runID))
		return runID;

	var lastSymbolOfCurrentRunID = runID.substring(runID.length - 1,
			runID.length);

	if (isDigit(lastSymbolOfCurrentRunID)) {
		//alert("not uniqe and it's digit:" + runID);
		var indexAtTheEnd = "0";
		runID = runID.substring(0, runID.length - 1) + indexAtTheEnd;

		while (!runIDIsUnique(runID)) {

			indexAtTheEnd++;
			runID = runID.substring(0, runID.length
					- indexAtTheEnd.toString().length)
					+ indexAtTheEnd;
			//alert("incremeted to " + runID + "length of index at the end: " + indexAtTheEnd.toString().length);
		}

	} else {
		var indexAtTheEnd = "0";
		runID = runID + indexAtTheEnd;

		while (!runIDIsUnique(runID)) {
			indexAtTheEnd++;
			runID = runID + indexAtTheEnd;
		}
	}

	document.getElementById('runIDInput').value = runID;
	return runID;
}

function runIDIsUnique(runID) {
	for (var i = 0; i < allRuns.length; i++) {
		if (allRuns[i].length == runID.length
				&& allRuns[i].indexOf(runID) != -1)
			return false;
	}
	return true;
}

function getAllRuns() {
	var allRunIDs = [];
	allDPNames = getAllDataProducts();

	for (var i = 0; i < allDPNames.length; i++) {
		if (allDPNames[i].indexOf(".") != -1) {
			var runID = allDPNames[i];
			while (runID.indexOf(".") != -1)
				runID = runID.substring(runID.indexOf(".") + 1, runID.length);
			allRunIDs.push(runID);
		}
	}
	return allRunIDs;
}

function isDigit(character) {
	for (var i = 0; i < 10; i++) {
		if (character.indexOf(i) == 0)
			return true;
	}
	return false;
}

function editWorkflow(workflowTextFromSidebar) {
	var wfName = workflowTextFromSidebar.substring(workflowTextFromSidebar
			.indexOf(">") + 1, workflowTextFromSidebar.indexOf("</span>"));
	var wfName = wfName.trim();
	displayWorkflowOnCanvas(wfName);
}


function viewDataProductValue(dataName) {
	
	var params = "action=getData&dataName=" + dataName.trim();
	function responseHandler(argArray, prettyHTMLFromServlet) {
		var input = dataName.substr(dataName.lastIndexOf("/") + 1, dataName.length);
		showHTMLinPopUp('value of ' + input, prettyHTMLFromServlet);

	}
	ajaxCall(params, responseHandler, null);
}


function showHTMLinPopUp(title, htmlString) {
	ModalPopups.Alert("jsAlert1", title,
			"<div style='padding:5px;text-align:center;'>" + htmlString
					+ "</div>", {
				okButtonText : "Close"
			});
}

function displayWorkflowOnCanvas(jsonMetadata) {
	var params = "action=getWorkflowDiagram&wfPath=" + jsonMetadata.dataPath;
	
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

	
	function responseHandler(argArray, responseFromServer) {
		if ((responseFromServer.indexOf("null") != 0)) {
			var xmlText = responseFromServer;
			var mxGraphText = xmlText;

			//alert(mxGraphText);

			while (mxGraphText.indexOf('^') != -1)
				mxGraphText = mxGraphText.replace('^', '&');

			var model = new mxGraphModel();
			var mxGraphXMLDoc = mxUtils.parseXml(mxGraphText);
			var root = mxGraphXMLDoc.documentElement;
			var dec = new mxCodec(root);
			dec.decode(root, graphG.getModel());
			currentWorkflowName = jsonMetadata.dataName;

			// Added by aravind to handle the mxgraph enlargement

			editorG.minFitScale = null;

			//editorG.execute("fit");

			editorG.execute("refresh");
		}
		focusOnItemInTree(jsonMetadata.dataName);
	}
	
}

function oneLevelUp() {
	displayWorkflowOnCanvas(nestedHistory.pop());
}