/**
 * The following functions are used to create and manage all the 
 * popup windows that originate from the webench. 
 * @author  Aravind Mohan, Andrey Kashlev.
*/

function updatePortInfo() {
	var currentNameAndType = getSelectedPortNameAndType();
	var oldName = currentNameAndType[0];
	var oldType = currentNameAndType[1];
	var optionsInsideSelect = "";
	for ( var i = 0; i < allowedTypes.length; i++) {
		optionsInsideSelect = optionsInsideSelect + "<option value=\""
				+ allowedTypes[i] + "\"";
		if (allowedTypes[i] == oldType)
			optionsInsideSelect = optionsInsideSelect + "selected=\"selected\"";

		optionsInsideSelect = optionsInsideSelect + ">" + allowedTypes[i]
				+ "</option>";
	}

	ModalPopups
			.Custom(
					"idCustom1",
					"Enter port information",
					"<div style='padding: 25px;'>"
							+ "<table>"
							+ "<tr><td>Port Name</td><td><input type=text value= '"
							+ oldName
							+ "'id='portName' style='width:250px;'></td></tr>"
							+ "<tr><td>Port Type</td><td>"
							+ "<select name=\"alltypes\" id= \"alltypes\" style=\"font-size:11pt;\">"
							+ optionsInsideSelect + "</select>" + "</td></tr>"
							+ "</table>" + "</div>", {
						width : 500,
						buttons : "ok,cancel",
						okButtonText : "Save",
						cancelButtonText : "Cancel",
						onOk : "updatePortInfoSave()"
					});
	ModalPopups.GetCustomControl("portName").focus();
}

function updatePortInfoSave() {
	var custom1Name = ModalPopups.GetCustomControl("portName");
	var typeSelector = ModalPopups.GetCustomControl("alltypes");

	ModalPopups.Close("idCustom1");
	var selectedType = typeSelector.options[typeSelector.selectedIndex].text;
	var newName = custom1Name.value;
	updatePort(newName, selectedType);
}

function updateDPInfoSave() {
	var custom1Name = ModalPopups.GetCustomControl("dpName");

	ModalPopups.Close("dpUpdatePopupId");
	var newName = custom1Name.value;
	updateDP(newName);
}

function updateDPInfo() {

	var currentNameAndType = getSelectedDPNameAndType();
	var oldName = currentNameAndType[0];
	var oldType = currentNameAndType[1];
	var optionsInsideSelect = "";

	for ( var i = 0; i < allowedTypes.length; i++) {
		optionsInsideSelect = optionsInsideSelect + "<option value=\""
				+ allowedTypes[i] + "\"";

		if (allowedTypes[i] == oldType)
			optionsInsideSelect = optionsInsideSelect + "selected=\"selected\"";

		optionsInsideSelect = optionsInsideSelect + ">" + allowedTypes[i]
				+ "</option>";
	}
	ModalPopups
			.Custom(
					"dpUpdatePopupId",
					"Enter Data Product information",
					"<div style='padding: 25px;'>"
							+ "<table>"
							+ "<tr><td>Data Product Name</td><td><input type=text value= '"
							+ oldName
							+ "'id='dpName' style='width:250px;'></td></tr>"
							+ "</table>" + "</div>", {
						width : 500,
						buttons : "ok,cancel",
						okButtonText : "Save",
						cancelButtonText : "Cancel",
						onOk : "updateDPInfoSave()"
					});
	ModalPopups.GetCustomControl("dpName").focus();
}

function updateDataProduct(dataName) {
	selectDataProduct(dataName.innerHTML);
	updateDPInfo();
}

function confirmOverwritingWF() {
	ModalPopups
			.Confirm(
					"idConfirm1",
					"",
					"<div style='padding: 5px;'>Workflow with this name already exists in the repository, overwrite?<br/><br/></div>",
					{
						yesButtonText : "Yes",
						noButtonText : "No",
						onYes : "confirmOverwritingWFYes()",
						onNo : "confirmOverwritingWFNo()"
					});
}

function confirmOverwritingWFYes() {
	currentWorkflowName = currentWorkflowNameNotConfirmed;
	var editor = editorG;
	var enc = new mxCodec(mxUtils.createXmlDocument());
	var node = enc.encode(editor.graph.getModel());
	//alert("editor.graph.getModel():" + editor.graph.getModel());
	var diagramXML = mxUtils.getPrettyXml(node);
	diagramXML = replaceAll(diagramXML, '&', '^');

	if (window.XMLHttpRequest) {
		xmlhttp = new XMLHttpRequest();
	} else {
		xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
	}
	var url = "./Mediator?";

	var params = "action=overwriteAndSave&name=" + currentWorkflowName
			+ "&diagram=" + diagramXML;
	////////////////////////////////////////////////////////////////////////////////
	function responseHandler(argArray, responseFromServer) {
		var newStatusMessage = responseFromServer;
		updateStatus(newStatusMessage);

		var isCorrect = 0;
		var isExperiment = 0;

		if (newStatusMessage.indexOf("success") == 0)
			isCorrect = 1;

		if (diagramXML.indexOf("dataProduct") != -1)
			isExperiment = 1;

		addWFToSidebarTreeAndMakeDraggable(currentWorkflowName, isCorrect,
				isExperiment, false);
	}
	ajaxCall(params, responseHandler, null);
	ModalPopups.Close("idConfirm1");
}

function confirmOverwritingWFNo() {
	ModalPopups.Cancel("idConfirm1");
}

function shareDetail(userid, wfName){
	//alert(userid);
	//alert(wfName);
	var shareWith = document.getElementById("selOUserId").value;
	//alert(shareWith);
	
	// Write logic to update table from here 
	
	var params = "action=loadSharingInfo&name=" + wfName
	+ "&sharedBy=" + userid 
	+ "&sharedTo=" + shareWith;

function responseHandler(argArray,
	responseFromServer) {
	updateStatus("Workflow shared successfully");
}
ajaxCall(params, responseHandler, null);

}
function shareWorkflow() {
	// Load user information into the dialog window for sharing workflows -- added by aravind on 01/29/2013
	var params = "action=getAllUsers";
	function responseHandler(argArray, jsonMetadataFromRepositoryAsString){
		var select = document.getElementById("selOUserId");
		var jsonMetadataFromRepository = eval('(' + jsonMetadataFromRepositoryAsString + ')');
		var lsOfUsers = jsonMetadataFromRepository['userlist'];
		for(var i=0; i<getAllKeysFromJSONMap(lsOfUsers).length; i++){
			select.options[select.options.length] = new Option(lsOfUsers[i].emailaddress,lsOfUsers[i].emailaddress);
		}
	}
		
	ajaxCall(params, responseHandler, null);
	
  $(function() {
		 	var paramUserID = $("#usrId").val();
		 	var paramWorkfowName = currentWorkflowName;
		    $("#WFShareDiv").data('paramShare', {userID:paramUserID, wfName:paramWorkfowName}).dialog(
		    		{
		    	        dialogClass: 'DynamicDialogStyle',
		    	        autoOpen: true,
		    	        modal: true,
		    	        buttons: { "Share": function () {shareDetail($(this).data("paramShare").userID, $(this).data("paramShare").wfName);}},
		    	        height:140,
		    	        width: 460,
		    	        title: 'Share Workflow '

		    	    }		
		    );
		  });
	
	
   
   
   
}

function connectDropbox(){
	  $(function() {
		 	var paramUserID = $("#usrId").val();
		 	var tKeyId =  $("#tokenKeyId").val('');
		 	
		 	var paramWorkfowName = currentWorkflowName;
		    $("#DropboxDiv").data('paramShare', {userID:paramUserID}).dialog(
		    		{
		    	        dialogClass: 'DynamicDialogStyle',
		    	        autoOpen: true,
		    	        modal: true,
		    	        buttons: { "Submit": function () {dropboxLoad($(this).data("paramShare").userID)}},
		    	        height:330,
		    	        width: 550,
		    	        title: 'Dropbox Login'

		    	    }		
		    );
		  });
}

function dropboxLoad(userId){
	var token =  $("#tokenKeyId").val();
	
	console.log("userid:----"+userId);
	console.log("token---" + token);

	// Write logic to change dropbox key 
	
	var params = "action=loadDropboxKey&userId=" + userId
	+ "&token=" + token;

	function responseHandler(argArray,
		responseFromServer) {
		updateStatus("Dropbox key pluged successfully");
		$("#DropboxDiv").dialog('close');
	}
	ajaxCall(params, responseHandler, null);
	window.location.reload();
}

/*** code added on 01-20-2016 by aravind and mahdi for cloud vm configuration 
 * 
 */
function cloudVMConfigureDetail(userid){
	var accessKey = document.getElementById("vmconfig_accesskey").value;
	var secretKey = document.getElementById("vmconfig_secretkey").value;
	//var instanceType = document.getElementById("vmconfig_instancetype").value;
	var key = encodeURIComponent(secretKey)
	// Write logic to store cloud vm settings table
	var params = "action=loadCloudSettings&userID=" + userid
	+ "&accessKey=" + accessKey 
	+ "&secretKey=" + key;
	//+ "&instanceType=" + instanceType;

	function responseHandler(argArray,
			responseFromServer) {
		 if (responseFromServer.indexOf("failure") >= 0 )
			 updateStatus("ERROR: Cloud settings was not created correctly.");
		 else
			 updateStatus("Cloud settings created successfully");
		
	}
ajaxCall(params, responseHandler, null);
	
}


function cloudVMConfigure() {
  $(function() {
		 	var paramUserID = $("#usrId").val();
		    $("#VMConfigDiv").data('paramVMConfig',{userID:paramUserID}).dialog(
		    		{
		    	        dialogClass: 'DynamicDialogStyle',
		    	        autoOpen: true,
		    	        modal: true,
		    	        buttons: { "Submit": function () {
		    	        	cloudVMConfigureDetail($(this).data("paramVMConfig").userID);
		    	        	$("#VMConfigDiv").dialog("close");	
		    	        }},
		    	        height:200,
		    	        width: 460,
		    	        title: 'Cloud VM Configuration:'

		    	    }		
		    );
		  });
}

function stopAvailableVMs(){
	$(function() {
		var paramUserID = $("#usrId").val();
		var params = "action=stopVMs&userID=" + paramUserID;
		function responseHandler(argArray,
				responseFromServer) {
			 updateStatus("ERROR: Please insert key values first");
			 if (responseFromServer.indexOf("empty") >= 0 )
				 updateStatus("ERROR: Please insert Amazon key values first");
			 else
				 updateStatus("All the existing VMs are terminated successfully");
			
		}
	ajaxCall(params, responseHandler, null);
	});	
}

function stopAvailableVM(){
	
	var paramUserID = $("#usrId").val();
	$.ajax({
		type : "POST",
		data : {"userid":paramUserID},
		url	 : "stopVMServlet",
		success : callback

	});
	
}

function callback(data){
	// $("#div").html(data);
	alert("Success");
}





function registerWebService(){
		document.getElementById("txtWSDLAddress").value = "";
		$("#selOWSOperation").empty();
		$(function() {
		
			$("#WFWebServiceDiv").dialog(
					{
						dialogClass : 'DynamicDialogStyle',
						autoOpen : true,
						modal : true,
						buttons : {
							 "Parse": function () {
								 	$("#selOWSOperation").empty();
								 	var wsdl = document.getElementById("txtWSDLAddress").value;
				    	        	var params = "action=parseWSDL&wsdl=" + wsdl;
				    	        	function responseHandler(argArray, response) {
				    	        		var select = document.getElementById("selOWSOperation");
				    	        		var opNames = response.split(",");
				    	        		for(var i=0; i<opNames.length; i++){
				    	        			select.options[select.options.length] = new Option(
				    	        					opNames[i], opNames[i]);
				    	        		}
				    	        		
				    	        		
				    	        	}
				    	        	ajaxCall(params, responseHandler, null);
				    	        	
				    	        	
				    	        	},
				    	        	"Register": function() {
				    	        		var selectedOperation = document.getElementById("selOWSOperation").value;
				    	        		var params = "action=createWSBasedPWorkflow&wsdl=" + document.getElementById("txtWSDLAddress").value 
				    	        		+ "&opName=" + selectedOperation;
				    	        		function responseHandler2(argArray, response2) {
				    	        			
				    	        			var newStatusMessage = response2;
				    	        			

											if (newStatusMessage
													.indexOf("cannot register workflow") != -1
													&& newStatusMessage
															.indexOf("because it's already in the database") != -1) {
												updateStatus(newStatusMessage);
											}
											else{
												var parama = document.getElementById("selOWSOperation").value;
												parama = parama.trim();
					    	        			var paramb = 1;
					    	        			var paramc = 0;
					    	        			addWFToSidebarTreeAndMakeDraggable(parama, paramb, paramc, false);
					    	        			updateStatus(newStatusMessage);
											}
												
				    	        			
				    	        		}
				    	        	
				    	        		ajaxCall(params, responseHandler2, null);
				    	        		$("#WFWebServiceDiv").dialog("close");
				    	                }
				    	        	
						},
						height : 160,
						width : 580,
						title : 'Register Web Service '

					});
		});
}
function registerCommandLine(){
	alert("Development in progress. Please check later...")
}
function registerPrimtiveWorkflow() {
	$(function() {
	
		$("#newPrimitiveWFDiv").dialog(
				{
					dialogClass : 'DynamicDialogStyle',
					autoOpen : true,
					modal : true,
					buttons : {
						 "Create": function () {
							 var primitiveWFtype = document.getElementById("selOPrimType").value;
							 if (primitiveWFtype == "WebService")
								 registerWebService();
							 else if (primitiveWFtype  == "CommandLine")
								 registerCommandLine();
						 }			    	        	
					},
					height : 140,
					width : 480,
					title : 'Register Primtive Workflow '

				});
	});



}
function addDataProduct() {

	ModalPopups
			.Custom(
					"addDPPopupId",
					"Register New Data Product",
					"<div id=\"newDPDialogDiv\" style='padding: 25px;'>"
							+ "<table>"
							+ "<tr><td>Data Product Type</td><td>"
							+ "<select name=\"addDP\" id= \"addDPID\" style=\"font-size:11pt;\"><option value = \"Scalar\">Scalar</option><option value = \"Relational\">Relational</option>><option value = \"File\">File</option>"
							+ "</select>" + "</td></tr>" + "</table>" + '<br>',
					{
						width : 300,
						buttons : "ok,cancel",
						okButtonText : "Register",
						cancelButtonText : "Cancel",
						onOk : "routeDPRegistration()"
					});

}

function newDataProduct() {
	var defaultName = "";
	var defaultType = "Integer";
	var optionsInsideSelect = "";

	for ( var i = 0; i < allowedTypes.length; i++) {
		optionsInsideSelect = optionsInsideSelect + "<option value=\""
				+ allowedTypes[i] + "\"";
		if (allowedTypes[i] == defaultType)
			optionsInsideSelect = optionsInsideSelect + "selected=\"selected\"";
		optionsInsideSelect = optionsInsideSelect + ">" + allowedTypes[i]
				+ "</option>";
	}
	ModalPopups
			.Custom(
					"newDPPopupId",
					"Register New Data Product",
					"<div id=\"newDPDialogDiv\" style='padding: 25px;'>"
							+ "<table>"
							+ "<tr><td>Data Product Name</td><td><input type=text value= '"
							+ defaultName
							+ "'id='newDPName' style='width:250px;'></td></tr>"
							+ "<tr><td>Data Product Type</td><td>"
							+ "<select onchange=\"updateNewDPFormBasedOnType()\" name=\"alltypes\" id= \"possibleNewDPTypes\" style=\"font-size:11pt;\">"
							+ optionsInsideSelect + "</select>" + "</td></tr>"
							//+ "<tr><td>Data Product Value</td><td>    <b>some value</b>    </td></tr>"
							+ "</table>"
							+ '<br><div id="changeablePartOfForm"></div></div>',
					{
						width : 500,
						buttons : "ok,cancel",
						okButtonText : "Register",
						cancelButtonText : "Cancel",
						onOk : "registerNewDPBasedOnFormFilledOut()"
					});
	ModalPopups.GetCustomControl("newDPName").focus();
	updateNewDPFormBasedOnType();

}

function updateNewDPFormBasedOnType() {
	var typeSelector = ModalPopups.GetCustomControl("possibleNewDPTypes");
	var selectedType = typeSelector.options[typeSelector.selectedIndex].text;
	var changeablePartOfForm = document.getElementById("changeablePartOfForm");
	if (selectedType.indexOf("Integer") != -1) {
		changeablePartOfForm.innerHTML = '<table><tr><td>Integer value:</td><td id="newIntDPValue"><input type="text" '
				+ ' style="width:250px"></td>' + '</tr></table>';
	} else if (selectedType.indexOf("File") != -1) {
		changeablePartOfForm.innerHTML = '<table><tr><td>File path:</td><td><input type=text id="newFileDPPath" style="width:250px"></td>'
				+ '</tr></table>';
	}

}

function routeDPRegistration() {
	var selectedDP = document.getElementById('addDPID').value;
	if (selectedDP == "Scalar")
		newDataProduct();
	else if (selectedDP == "File")
		newFileDataProduct();
	else
		newRelationalDataProduct();

}

function registerNewDPBasedOnFormFilledOut() {

	var custom1Name = ModalPopups.GetCustomControl("newDPName");
	var typeSelector = ModalPopups.GetCustomControl("possibleNewDPTypes");

	var newDataName = custom1Name.value;
	var newDataType = typeSelector.options[typeSelector.selectedIndex].text;
	var value = null;
	if (newDataType.indexOf("Integer") != -1
			|| newDataType.indexOf("Boolean") != -1
			|| newDataType.indexOf("String") != -1) {
		var newIntValue = ModalPopups.GetCustomControl("newIntDPValue");
		var inputBox = newIntValue.childNodes[0];
		value = inputBox.value;
	}

	ModalPopups.Close("newDPPopupId");
	var params = "action=registerDataProduct&name=" + newDataName + "&type="
			+ newDataType + "&value=" + value;

	function responseHandler(argArray, responseFromServer) {
		var newStatusMessage = responseFromServer;
		updateStatus(newStatusMessage);
		addDPToSidebarTreeAndMakeDraggable(newDataName);
	}
	ajaxCall(params, responseHandler, null);
}

function displayTextInPopupWindow(text) {
	if (window.innerWidth) {
		var width = 800;
		var height = 800;
		LeftPosition = (window.innerWidth - width) / 2;
		TopPosition = ((window.innerHeight - height) / 4) - 50;
	} else {
		LeftPosition = (parseInt(window.screen.width) - width) / 2;
		TopPosition = ((parseInt(window.screen.height) - height) / 2) - 50;
	}
	attr = 'resizable=no,scrollbars=yes,width=' + width + ',height=' + height
			+ ',screenX=300,screenY=200,left=' + LeftPosition + ',top='
			+ TopPosition + '';
	popWin = open('', 'new_window', attr);

	popWin.document.write(text);
}

//*****************************************************Mahdi*********************************************************************
//var langArray = [
//                 {value: "INTEGER", text: "Integer"},
//                 {value: "TEXT", text: "String"},
//                 {value: "TINYINT(1)", text: "Boolean (0/1)"},
//                 {value: "FLOAT", text: "Decimal"},
//                 {value: "DATE", text: "Date/Time"}
//             ];

var mysqlTypes = [ {
	value : "TEXT",
	text : "String"
}, {
	value : "TINYINT(1)",
	text : "Boolean (0 or 1)"
},
//                 {value: "BIGINT", text: "Long"},
{
	value : "INTEGER",
	text : "Integer"
}, {
	value : "FLOAT",
	text : "Float"
}, {
	value : "DOUBLE",
	text : "Double"
}, {
	value : "DECIMAL",
	text : "Decimal"
}, {
	value : "DATE",
	text : "Date/Time"
} ];

function Selection() {
	var newSelect = document.createElement('select');
	var index = 0;
	for (element in mysqlTypes) {

		var opt = document.createElement("option");
		opt.setAttribute('value', mysqlTypes[index].value);
		opt.appendChild(document.createTextNode(mysqlTypes[index].text));
		// append it to the select element
		newSelect.appendChild(opt);
		index++;
	}
}
function newFileDataProduct(){
	ModalPopups.Close("addDPPopupId");
	$(function() {
		
		$("#DataProductFileDiv").dialog(
				{
					dialogClass : 'DynamicDialogStyle',
					autoOpen : true,
					modal : true,
					buttons : {
						 "Register": function () {
							    var sampleFile = document.getElementById("fileDataProduct").files[0];
						 	    var dataproductname = document.getElementById("txtDPFileName").value;
							 	var formdata = new FormData();
						 		formdata.append("fileDataProduct", sampleFile);
						 		formdata.append("dataproductname", dataproductname);	
						 		var xhr = new XMLHttpRequest();
						 		
						 		var environ = window.location.host;
						 	    var baseurl = "";
						 		if (environ === "localhost") 
						 	    		baseurl = window.location.protocol + "//" + window.location.host + "/" + window.location.pathname;
						 	    else 
						 	    		baseurl = window.location.protocol + "//" + window.location.host + "/"
						 	    		+ window.location.pathname;
						 		baseurl = baseurl.replace("UserLogin", "FileUploader");
						 	    	xhr.open("POST",baseurl, true);
						 		xhr.send(formdata);
						 		
						 		xhr.onload = function(e) {
						 				if (this.status == 200) {
						 				   alert(this.responseText);
						 				}
						 		};	        	
						 		
						 		
		    	                }
			    	        	
					},
					height : 140,
					width : 460,
					title : 'Register File Data Product '

				});
	});

	
	
	
}
function newRelationalDataProduct() {
	var defaultName = "";
	var defaultType = "Relation";

	var newSelect = document.createElement('select');
	var index = 0;
	for (element in mysqlTypes) {

		var opt = document.createElement("option");
		opt.setAttribute('value', mysqlTypes[index].value);
		opt.appendChild(document.createTextNode(mysqlTypes[index].text));
		// append it to the select element
		newSelect.appendChild(opt);
		index++;
	}

	ModalPopups
			.Custom(
					"newDPPopupId",
					"Register New Relational Data Product",
					"<div id=\"newDPDialogDiv\">"
							+ "<TABLE id= NameTable ><tr><td><b>Data Product Name:<b></td><td><input type=text id= newDPName></td></tr></TABLE>"

							+ "<BR><INPUT type=button value='+Add Column' onclick=\"updateColumn('metaTable')\"'/>"
							+ "<div style='overflow:auto; width:100%; height:100%'>"
							+ "<TABLE id= metaTable width=150px border=1> <caption>Columns Name and Type</caption>"
							+ "<TR><TD><INPUT type=text/></TD></TR>"
							+ "<TR><TD><SELECT>"
							+ newSelect.innerHTML
							+ "</SELECT></TD>"
							+ "</TR></TABLE></div>"

							+ "<BR><INPUT type=button value='+Add Row' onclick=\"addRow('dataTable')\"'/>"
							+ "<div style='overflow-y:auto; overflow-x:auto; width:100%; height:100%'>"
							+ "<TABLE id= dataTable width=150px border=1><caption>Data Value</caption>"
							+ "<TR><TD><INPUT type=text/></TD></TR>"
							+ "</TABLE></div>"

							+ "<br><INPUT type=button value='Register' onclick=\"registerNewRelationalDPBasedOnFormFilledOut('NameTable','metaTable','dataTable')\"'/>"
							+ "<INPUT type=button value='Cancel' onclick=\"CancelPopup()\"'/>",
					{
						width : 1200,
						heigth : 3000,
						buttons : "",
					//okButtonText : "OK",
					//cancelButtonText : "Cancel",
					//onOk : "registerNewRelationalDPBasedOnFormFilledOut1()"
					});
	ModalPopups.GetCustomControl("newDPName").focus();
	updateNewDPFormBasedOnType();

}
function CancelPopup() {
	ModalPopups.Close("newDPPopupId");
}

function addRow(tableID) {
	var table = document.getElementById(tableID);
	var rowCount = table.rows.length;
	var row = table.insertRow(rowCount);
	var colCount = table.rows[0].cells.length;
	for ( var i = 0; i < colCount; i++) {
		var cell = row.insertCell(i);
		var element = document.createElement("input");
		element.type = "text";
		cell.appendChild(element);
	}
}

function updateColumn(tableID) {
	add2Columns(tableID);
	addColumn("dataTable");
}
function addColumn(tableID) {
	var table = document.getElementById(tableID);
	var rowCount = table.rows.length;
	for ( var i = 0; i < rowCount; i++) {
		var cell = table.rows[i].insertCell(1);
		var element = document.createElement("input");
		element.type = "text";
		cell.appendChild(element);
	}

}
function add2Columns(tableID) {

	var table = document.getElementById(tableID);

	var cell = table.rows[0].insertCell(0);
	var element = document.createElement("input");
	element.type = "text";
	cell.appendChild(element);

	var cell = table.rows[1].insertCell(0);
	var newSelect = document.createElement('select');
	var index = 0;
	for (element in mysqlTypes) {
		var opt = document.createElement("option");
		opt.setAttribute('value', mysqlTypes[index].value);
		opt.appendChild(document.createTextNode(mysqlTypes[index].text));
		// append it to the select element
		newSelect.appendChild(opt);
		index++;
	}
	cell.appendChild(newSelect);
}
function deleteColumn(tableID) {
	var allRows = document.getElementById(tableID).rows;
	for ( var i = 0; i < allRows.length; i++) {
		if (allRows[i].cells.length > 1) {
			allRows[i].deleteCell(-1);
		}
	}
}

function registerNewRelationalDPBasedOnFormFilledOut(NametableID, matatableID,
		tableID) {
	try {
		var newDataName = "";
		var newDataType = "Relation";
		var data = "";
		//**********************DP Name***************************************
		var tableName = document.getElementById(NametableID);
		var newDataName = tableName.rows[0].cells[1].childNodes[0].value;
		//*********************Meta Table(Table Columns Name and TYpe)**********
		var metatable = document.getElementById(matatableID);
		var colname = "";
		var datatype = "";
		var metacolCount = metatable.rows[0].cells.length;
		for ( var k = 0; k < metacolCount; k++) {
			colname += metatable.rows[0].cells[k].childNodes[0].value + ",";
			datatype += metatable.rows[1].cells[k].childNodes[0].value + ",";
		}
		data += colname + "!";
		data += datatype + "!";

		//*********************Data Table**************************************
		var table = document.getElementById(tableID);
		var rowCount = table.rows.length;
		var colCount = table.rows[0].cells.length;
		for ( var i = 0; i < rowCount; i++) {
			var row = table.rows[i];
			var value = "";
			for ( var j = 0; j < colCount; j++) {
				value += row.cells[j].childNodes[0].value + ",";
			}
			data += value + "!";
		}
		//alert(newDataName+","+newDataType+","+data);
		ModalPopups.Close("newDPPopupId");
		var params = "action=registerDataProduct&name=" + newDataName
				+ "&type=" + newDataType + "&value=" + data;
		function responseHandler(argArray, responseFromServer) {
			var newStatusMessage = responseFromServer;
			updateStatus(newStatusMessage);
			addDPToSidebarTreeAndMakeDraggable(newDataName);
		}
		ajaxCall(params, responseHandler, null);
	} catch (e) {
		alert(e);
	}
}

function newCollectionalDataProduct() {
	var defaultName = "";
	var defaultType = "Collection";
	var optionsInsideSelect = "";

	for ( var i = 0; i < allowedTypes.length; i++) {
		optionsInsideSelect = optionsInsideSelect + "<option value=\""
				+ allowedTypes[i] + "\"";
		if (allowedTypes[i] == defaultType)
			optionsInsideSelect = optionsInsideSelect + "selected=\"selected\"";
		optionsInsideSelect = optionsInsideSelect + ">" + allowedTypes[i]
				+ "</option>";
	}
	ModalPopups
			.Custom(
					"newDPPopupId",
					"Register New Collectional Data Product",
					"<div id=\"newDPDialogDiv\">"
							+ "<TABLE id= NameTable ><tr><td><b>Data Product Name:<b></td><td><input type=text id= newDPName></td></tr></TABLE>"

							+ "<BR><INPUT type=button value='+Add Keys' onclick=\"updateColumn('metaTable')\"'/>"

							+ "<div style='overflow:auto; width:100%; height:100%'>"
							+ "<TABLE id= metaTable width=150px border=1> <caption>Keys Name and Type</caption>"
							+ "<TR><TD><INPUT type=text value='Relation Name' readonly='readonly' ID='NodeID'></TD></TR>"
							+ "<TR><TD ><INPUT type=text value='String' readonly='readonly' ID='TEXT'></TD>"
							+ "</TR></TABLE></div>"

							+ "<BR><INPUT type=button value='+Add Row' onclick=\"addRow('dataTable')\"'/>"
							+ "<div style='overflow:auto; width:100%; height:100%'>"
							+ "<TABLE id= dataTable width=150px border=1><caption>Data Value</caption>"
							+ "<TR><TD><INPUT type=text/></TD></TR>"
							+ "</TABLE></div>"
							+ "<br><INPUT type=button value='Register' onclick=\"registerNewCollectionalDPBasedOnFormFilledOut('NameTable','metaTable','dataTable')\"'/>"
							+ "<INPUT type=button value='Cancel' onclick=\"CancelPopup()\"'/>",
					{
						width : 1200,
						heigth : 1500,
						buttons : "",
					//okButtonText : "OK",
					//cancelButtonText : "Cancel",
					//onOk : "registerNewRelationalDPBasedOnFormFilledOut1()"
					});
	ModalPopups.GetCustomControl("newDPName").focus();
	updateNewDPFormBasedOnType();

}

function registerNewCollectionalDPBasedOnFormFilledOut(NametableID,
		matatableID, tableID) {
	try {
		var newDataName = "";
		var newDataType = "Collection";
		var data = "";
		//**********************DP Name***************************************
		var tableName = document.getElementById(NametableID);
		var newDataName = tableName.rows[0].cells[1].childNodes[0].value;
		//*********************Meta Table(Table Columns Name and TYpe)**********
		var metatable = document.getElementById(matatableID);
		var colname = "";
		var datatype = "";
		var metacolCount = metatable.rows[0].cells.length;
		for ( var k = 0; k < metacolCount; k++) {
			var cell = metatable.rows[0].cells[k].childNodes[0].value;
			if (cell == "Relation Name") {
				colname += "NODEID" + ",";
				datatype += "TEXT" + ",";
			} else {
				colname += metatable.rows[0].cells[k].childNodes[0].value + ",";
				datatype += metatable.rows[1].cells[k].childNodes[0].value
						+ ",";
			}
		}
		data += colname + "!";
		data += datatype + "!";

		//*********************Data Table**************************************
		var table = document.getElementById(tableID);
		var rowCount = table.rows.length;
		var colCount = table.rows[0].cells.length;
		for ( var i = 0; i < rowCount; i++) {
			var row = table.rows[i];
			var value = "";
			for ( var j = 0; j < colCount; j++) {
				value += row.cells[j].childNodes[0].value + ",";
			}
			data += value + "!";
		}
		//alert(newDataName+","+newDataType+","+data);
		ModalPopups.Close("newDPPopupId");
		var params = "action=registerjDataProduct&name=" + newDataName
				+ "&type=" + newDataType + "&value=" + data;
		function responseHandler(argArray, responseFromServer) {
			var newStatusMessage = responseFromServer;
			updateStatus(newStatusMessage);
			addDPToSidebarTreeAndMakeDraggable(newDataName);
		}
		ajaxCall(params, responseHandler, null);
	} catch (e) {
		alert(e);
	}
}

//*******************************************************************************************************************************

function updateNewDPFormBasedOnType() {
	var typeSelector = ModalPopups.GetCustomControl("possibleNewDPTypes");
	var selectedType = typeSelector.options[typeSelector.selectedIndex].text;
	var changeablePartOfForm = document.getElementById("changeablePartOfForm");
	if (selectedType.indexOf("Integer") != -1
			|| selectedType.indexOf("Boolean") != -1
			|| selectedType.indexOf("String") != -1) {
		changeablePartOfForm.innerHTML = '<table><tr><td>Value:</td><td id="newIntDPValue"><input type="text" '
				+ ' style="width:250px"></td>' + '</tr></table>';
	} else if (selectedType.indexOf("File") != -1) {
		changeablePartOfForm.innerHTML = '<table><tr><td>File path:</td><td><input type=text id="newFileDPPath" style="width:250px"></td>'
				+ '</tr></table>';
	}

}

function registerNewDPBasedOnFormFilledOut() {

	var custom1Name = ModalPopups.GetCustomControl("newDPName");
	var typeSelector = ModalPopups.GetCustomControl("possibleNewDPTypes");

	var newDataName = custom1Name.value;
	var newDataType = typeSelector.options[typeSelector.selectedIndex].text;
	var value = null;
	if (newDataType.indexOf("Integer") != -1
			|| newDataType.indexOf("Boolean") != -1
			|| newDataType.indexOf("String") != -1) {
		var newIntValue = ModalPopups.GetCustomControl("newIntDPValue");
		var inputBox = newIntValue.childNodes[0];
		value = inputBox.value;
		value = replaceAll(value, "'", "\'");
		//alert("value now iss; " + value);
	}

	ModalPopups.Close("newDPPopupId");
	var params = "action=registerDataProduct&name=" + newDataName + "&type="
			+ newDataType + "&value=" + value;

	function responseHandler(argArray, responseFromServer) {
		var newStatusMessage = responseFromServer;
		updateStatus(newStatusMessage);
		addDPToSidebarTreeAndMakeDraggable(newDataName);
	}
	ajaxCall(params, responseHandler, null);

}
