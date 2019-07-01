<!-- 
 * The following functions are the main functions that are used to 
 * setup the webench. 
 * @author  Aravind Mohan
-->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<script type='text/javascript'>
	window.mod_pagespeed_start = Number(new Date());
</script>
<title>DATAVIEW</title>
<script type="text/javascript" src="./viewJS/dropdown/menuh.js"></script>
<link rel="stylesheet" href="./viewJS/dropdown/menu.css" type="text/css"
	media="screen" />
<!--  <link rel="STYLESHEET" type="text/css"
	href="../../../dhtmlxGrid/codebase/skins/dhtmlxgrid_dhx_skyblue.css">
-->

<link rel="STYLESHEET" type="text/css"
	href="./viewJS/dhtmlxTree/codebase/dhtmlxtreeview.css">

<!--  This section of script include is for opening the pop ups -->
<script type="text/javascript" src="./Script/jquery-1.7.1.min.js"></script>
<script type="text/javascript"
	src="./Script/jquery-ui-1.8.17.custom.min.js"></script>
<script type="text/javascript" src="./Script/DemoScript.js"></script>
<link href="./Style/jquery-ui-1.8.17.custom.css" rel="Stylesheet"
	type="text/css" />



<script type="text/javascript"
	src="./src/js/modalpopups_files/ModalPopups.js" language="javascript"></script>
<style type="text/css">
body {
	background-color: #FFFFFF;
}

H1 {
	font-size: 18px
}

H2 {
	font-size: 16px
}

.boxed {
	border: 1px solid green;
	background-color: #00008B;
	color: #FFFFE0;
	width: 240px;
	text-align: left;
}

#menu {
	width: 100%;
	list-style-type: none;
	margin: 0;
	padding: 0;
	padding-top: 16px;
	padding-bottom: 6px;
	background: url('images/dataview-banner.jpg') bottom right no-repeat;
	/*background: url('./Web_Files/dataviewlogo.png') bottom left no-repeat;*/
}

#menu li {
	display: inline;
	padding: 5px 5px 5px 5px
}

#menu a:link, #menu a:visited {
	color: black;
	background-color: #CEF5E1;
	text-align: right;
	padding: 2px;
	text-decoration: none;
}

#menu a:hover, #menu a:active {
	background-color: #7A991A;
}

.DynamicDialogStyle {
	background-color: #F7FAFE;
	font-size: small;
}
</style>

<!-- Sets the basepath for the library if not in same directory -->
<script type="text/javascript">
	mxBasePath = 'src';
</script>

<!-- Loads and initializes the library -->
<script type="text/javascript" src="src/js/mxClient.js"></script>

<!-- Example code -->
<script type="text/javascript">
	String.prototype.count = function(s1) {
		return (this.length - this.replace(new RegExp(s1, "g"), '').length)
				/ s1.length;
	};
	String.prototype.trim = function() {
		return this.replace(/^\s+|\s+$/g, "");
	};
	// Program starts here. Creates a sample graph in the
	// DOM node with the specified ID. This function is invoked
	// from the onLoad event handler of the document (see below).
	var numberOfInputPorts = 0;
	var numberOfOutputPorts = 0;
	var allowedTypes = [ "Integer", "Boolean", "String" ];
	var currentWorkflowName = "Untitled Workflow";
	var isWorkflowShared = false;
	var currentWorkflowNameNotConfirmed = "undefined";
	var graphG;
	var sidebarLeft;
	var sidebarRight;
	var editorG;
	var numberOfOutputDataProducts = 0;

	var nestedHistory = []; //contains names
	var specsNestedHistory = []; //contains mxgraph representations of workflows

	function addWorkflow() {

		var xmlTextOfEmptyGraph = '<mxGraphModel> <root> <mxCell id="0"/> <mxCell id="1" parent="0"/> </root></mxGraphModel>';
		var model = new mxGraphModel();

		var req = mxUtils.parseXml(xmlTextOfEmptyGraph);
		var root = req.documentElement;
		var dec = new mxCodec(root);
		dec.decode(root, graphG.getModel());

		currentWorkflowName = "Untitled Workflow";
		newWorkflow();
	}

	function createCustomDialogWindow(cellContent) {
		//alert("inner: " + cellContent);

		var result = cellContent;
		//if it's a port:
		if (result.indexOf("inputPort.png") != -1
				|| result.indexOf("outputPort.png") != -1) {
			var portHTML = result;
			var currentPortId = portHTML.substring(portHTML.indexOf("id=") + 4,
					portHTML.indexOf("id=") + 24);
			//currentId = currentPortId.substring(0,currentPortId.indexOf("\""));
			updatePortInfo();
		} else if (cellContent.indexOf("dataProduct") != -1) {
			//alert("it's data product:\n" + cellContent);
			var result = cellContent;
			if (cellContent.indexOf("stubForOutputDP") != -1)
				updateDPInfo(null);
		} else if (cellContent.indexOf("workflowComponent") != -1) {
			//it's a workflow component
			//alert("it's workflow component" + cellContent);
			var wfName = cellContent.substring(cellContent
					.indexOf("workflowComponent") + 18, cellContent
					.indexOf("</div>"));
			nestedHistory.push(currentWorkflowName);

			var enc = new mxCodec(mxUtils.createXmlDocument());
			var node = enc.encode(graphG.getModel());
			var currentMxGraphSource = mxUtils.getPrettyXml(node);
			specsNestedHistory.push(currentMxGraphSource);

			displayWorkflowOnCanvas(wfName);
		}
	}

	function replaceAll(txt, replace, with_this) {
		return txt.replace(new RegExp(replace, 'g'), with_this);
	}

	function updateStatus(newStatusMessage) {
		
		var txtInThePopUp = newStatusMessage;
		var imgSrc;

		if (newStatusMessage.indexOf("ERROR:") != -1)
			imgSrc = "editors/images/overlays/error.png";
		else if (newStatusMessage.indexOf("success") != -1)
			imgSrc = "editors/images/overlays/check.png";

		var contentOfStatusContainer = "<span style=\"display:inline-block; vertical-align:middle;font-size:10pt;padding-right:5px;\">"
				+ "<img src=\"" + imgSrc + "\" />" + "</span><span>";

		var visiblePart = newStatusMessage.substring(0, 67);

		if (visiblePart.indexOf("ERROR:") == 0)
			visiblePart = visiblePart.replace("ERROR:", "");

		while (txtInThePopUp.indexOf("\'") != -1)
			txtInThePopUp = txtInThePopUp.replace("\'", "");

		txtInThePopUp = txtInThePopUp.split('ERROR:').join("\\nerror => ");

		txtInThePopUp = txtInThePopUp.substring(0, txtInThePopUp
				.lastIndexOf("\n") - 1);

		contentOfStatusContainer = contentOfStatusContainer + visiblePart;

		if (newStatusMessage.length > 67)
			contentOfStatusContainer = contentOfStatusContainer
					+ "<a href = \"#\" onclick=\"alert(\'" + txtInThePopUp
					+ "\')\" style=\"color:white\">Read more</a>";

		contentOfStatusContainer = contentOfStatusContainer + "</span>";

		var statusMessage = document.getElementById('statusMessage');
		statusMessage.innerHTML = contentOfStatusContainer;

	}

	function addSpace(scrollableSidebar, height) {
		var smallSpace = document.createElement('div');
		smallSpace.style.width = height;
		smallSpace.style.height = height;
		scrollableSidebar.appendChild(smallSpace);
	}

	//function editWorkflow(wfName){
	//	alert("editing workflow: " + wfName);
	//}

	function createMenu() {
		var divMenuContainer = document.createElement('div');
		divMenuContainer.setAttribute("id", "menuContainer");
		document.body.appendChild(divMenuContainer);

		document.getElementById("menuContainer").innerHTML = '<div id="mB">\r\n';
		bar.writeBar();
		var initHTML = document.getElementById("menuContainer").innerHTML;
		document.getElementById("menuContainer").innerHTML = initHTML
				+ '\r\n<\/div>\r\n\r\n';
		bar.writeDrop();
		iMenu();
	}

	function newPrimitiveWorkflow() {
		alert("new primitive workflow stub");
	}

	function newCompositeWorkflow() {
		alert("new composite workflow stub");
	}

	function logout() {
	window.location.href = "login.jsp";

	}
	function viewMxGraph() {
		var textarea = document.createElement('textarea');
		textarea.style.width = '400px';
		textarea.style.height = '400px';
		var enc = new mxCodec(mxUtils.createXmlDocument());
		var node = enc.encode(graphG.getModel());
		textarea.value = mxUtils.getPrettyXml(node);
		showModalWindow(graphG, 'XML', textarea, 410, 440);
	}

	function printPreview() {
		var preview = new mxPrintPreview(graphG);
		preview.open();
	}

	function help() {
		var aboutURL = "https://sites.google.com/site/swfwiki/wf-engine";
		window.open(aboutURL, '_blank');
		window.open(aboutURL);
	}

	function aboutVIEW() {
		var aboutURL = "http://www.viewsystem.org";
		window.open(aboutURL, '_blank');
		window.open(aboutURL);
	}
	
	// Load users information in the Workflow Share Dialog
 	
</script>

<link rel="STYLESHEET" type="text/css"
	href="./viewJS/dhtmlxTree/codebase/dhtmlxtree.css">
</head>

<!-- Page passes the container for the graph to the grogram -->
<body
	onload="main(
		document.getElementById('banner'),
			document.getElementById('graphContainer'),
			document.getElementById('outlineContainer'),
		 	document.getElementById('toolbarContainer'),
		 	document.getElementById('sidebarContainerLeft'),
			document.getElementById('statusContainer'), 
			document.getElementById('sidebarContainerRight'));">

	<%
		String userID = request.getAttribute("userId").toString();
		
	%>
	<input type="hidden" name="usrId" id="usrId" value="<%=userID%>" />
	<input type="hidden" name="runIDInput" id="runIDInput" value="" />

	<div id="bannerid"
		style="margin: 0 auto; text-align: right; width: 1000px;">
		<table width="100%" border="0">
			<tr>
				<th>
					<ul id="menu">
						<li><a href="https://github.com/shiyonglu/DATAVIEW">Documentation</a></li>
						<li><a href="#" onclick="connectDropbox();return false;">Dropbox</a></li>
						<li><a href="#" onclick="cloudVMConfigure();return false;">Cloud</a></li>
						<li><a href="#" onclick="stopAvailableVMs();return false;"> ResetVM </a></li>
						<!-- 
						<li><a
							href="downloadmain.jsp?fileName=dataview.war&filePath=/home/amohan/Software">Download</a></li>
							-->
						<li><a
							href="https://github.com/shiyonglu/DATAVIEW">Download</a></li>
						<li><a href="login.jsp">Logout</a></li>
					</ul>
				</th>

			</tr>
		</table>

	</div>

	<!--  
	<script src="./viewJS/dhtmlxTree/codebase/dhtmlxcommon.js"></script>
	-->
	
	<script src="./viewJS/dhtmlxTree/codebase/dhtmlxtree.js"></script>
	<script type="text/javascript" src="./viewJS/fromMxGraph.js"></script>
	<script type="text/javascript" src="./viewJS/initWebbench.js"></script>
	<script type="text/javascript" src="./viewJS/useWebbench.js"></script>
	<script type="text/javascript" src="./viewJS/mxGraphRelatedCode.js"></script>
	 
	<script type="text/javascript" src="./viewJS/main.js"></script>
	 
	<script type="text/javascript" src="./viewJS/popupsAndDialogs.js"></script>
	<script src="./viewJS/treeScript.js"></script>

	<!-- Creates a container for the splash screen -->
	<div id="splash"
		style="position: absolute; top: 0px; left: 0px; width: 100%; height: 100%; background: white; z-index: 1;">
		<center id="splash" style="padding-top: 230px;">
			<img src="editors/images/loading.gif">
		</center>
	</div>



	<!-- Creates a container for the sidebar -->

	<div id="sidebarContainerLeft"
		style="position: absolute; overflow: hidden; top: 50px; right: 0px; bottom: 36px; max-width: 220px; width: 220px; padding-top: 0px; padding-left: 0px; border-style: solid; border-color: #D3D3D3; border-radius: 25px; -moz-border-radius: 25px; background-size: 240px;">

		<div class="boxed">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Workflow
			Repository</div>
		<!-- 
		<table cellpadding="0" cellspacing="0">
			<tr>
				<td><input type="text" id="stextR" width="120px"
					maxlength="200" /></td>
				<td align="left"><input type="image" id="searchR"
					src="images/search.jpg" alt="Submit" width="22" height="22"
					onClick="searchRightSidebar(document.getElementById('stextR').value)" />

				</td>

				<td align="left"><input type="image" id="addL"
					src="images/add.jpg" alt="Submit" width="22" height="22"
					onClick="registerPrimtiveWorkflow()" /> <br /></td>
			</tr>
		</table>
 		-->

	</div>

	<!-- Creates a container for the toolboox -->
	<div id="sidebarContainerRight"
		style="position: absolute; overflow: hidden; top: 50px; left: 0px; bottom: 36px; max-width: 220px; width: 220px; padding-top: 0px; padding-left: 0px; border-style: solid; border-color: #D3D3D3; border-radius: 25px; -moz-border-radius: 25px; background-size: 240px;">
		<div class="boxed">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Dataset
			History</div>
	
	</div>





	<!-- Creates a container for the graph -->
	<div id="graphContainer"
		style="position: absolute; overflow: auto; top: 50px; left: 230px; bottom: 36px; right: 230px; border-style: solid; border-color: #D3D3D3; border-radius: 25px; -moz-border-radius: 25px; background-size: 240px;">
		<div id="toolbarContainer"
			style="position: absolute; white-space: nowrap; overflow: hidden; top: 0px; left: 0px; max-height: 24px; height: 36px; right: 0px; padding: 0px; background-color: #00008B;">


		</div>
	</div>



	<!-- Creates a container for the outline -->
	<div id="outlineContainer"
		style="position: absolute; overflow: hidden; top: 120px; right: 240px; width: 100px; height: 70px; background: transparent; border-style: solid; border-color: black;">
	</div>

	<!-- Creates a container for the sidebar -->
	<div id="statusContainer"
		style="text-align: right; position: absolute; overflow: hidden; bottom: 0px; left: 0px; max-height: 24px; height: 36px; right: 0px; color: black; padding: 6px; background-color: #DDF7F6; border-radius: 25px; -moz-border-radius: 25px;">
		<div id="statusMessage"
			style="font-size: 10pt; float: left; width: 500px; padding-left: 10px; overflow: hidden; text-align: left; height: 20px;">
		</div>

	</div>

	
	<div id="DataProductFileDiv" style="overflow: 'hidden'; display: none;">
		<table cellpadding="1px" cellspacing="1px" bgcolor="#B8E6E6"
			width="400px" class="tableBorder" align="center">
			<tr>
				<td><label for="txtDPFileName" class="label">Data
						Product Name: </label></td>
				<td align="left"><input type="text" id="txtDPFileName"
					name="txtDPName" maxlength="40" /></td>
			</tr>
			<tr>
				<td colspan="2"><input type="file" size="60"
					id="fileDataProduct"></td>

			</tr>

		</table>


	</div>
	<div id="WFShareDiv" style="overflow: 'hidden'; display: none;">
		<table cellpadding="2px" cellspacing="1px" bgcolor="#B8E6E6"
			width="420px" class="tableBorder" align="center">
			<tr>
				<td class="label" align="left"><B>Share workflow with: </B></td>
				<td align="left"><select name="selUerId"
					onChange="loadUsers();" id="selOUserId" style="width: 200px">
						<option value="All">All</option>
				</select></td>
			</tr>

		</table>
	</div>

	<div id="VMConfigDiv" style="overflow: 'hidden'; display: none;">
		<table cellpadding="2px" cellspacing="1px" bgcolor="#B8E6E6"
			width="420px" class="tableBorder" align="center">
			<tr>
				<td class="label" align="left"><B>Access Key ID: </B></td>
				<td align="left"><input type="text" id="vmconfig_accesskey"
					name="vmconfig_accesskey" maxlength="120" size="35"></td>
			</tr>
			<tr>
				<td class="label" align="left"><B>Secret Key: </B></td>
				<td align="left"><input type="text" id="vmconfig_secretkey"
					name="vmconfig_secretkey" maxlength="120" size="35"></td>
			</tr>
			<tr>
				<td class="label" align="left"><B>Instance Type: </B></td>
				<td align="left"><select id="vmconfig_instancetype"
					name="vmconfig_instancetype" style="width: 100px">
						<option value="t2.micro" selected>t2.micro</option>
				</select></td>
			</tr>
		</table>
	</div>

	<div id="WorkflowRunConfigDiv"
		style="overflow: 'hidden'; display: none;">
		<table cellpadding="2px" cellspacing="1px" bgcolor="#B8E6E6"
			width="300px" class="tableBorder" align="center">
			<tr>
				<td class="label" align="left"><B>Environment: </B></td>
				<td align="left"><select id="wfrunconfig_executiontype"
					name="wfrunconfig_executiontype" style="width: 100px"
					>
						<option value="DATAVIEW-Server" selected>DATAVIEW-Server</option>
						<option value="EC2-Cloud">EC2-Cloud</option>
				</select></td>
			</tr>
			
		</table>
	</div>



	<div id="newPrimitiveWFDiv" style="overflow: 'hidden'; display: none;">

		<table cellpadding="2px" cellspacing="1px" bgcolor="#F4F5F7"
			width="400px" class="tableBorder" align="center">
			<tr>
				<td><label class="label">Primtive Workflow Type: </label></td>
				<td align="left"><select name="selOPrimType" id="selOPrimType"
					style="width: 200px">
						<option value="CommandLine">Command Line</option>
						<option value="WebService">Web Service</option>
				</select></td>
			</tr>
		</table>
	</div>
	<div id="WFWebServiceDiv" style="overflow: 'hidden'; display: none;">
		<table cellpadding="2px" cellspacing="1px" bgcolor="#F4F5F7"
			width="550px" class="tableBorder" align="center">
			<tr>
				<td><label for="txtWSDLAddress" class="label">WSDL Address: </label></td>
				<td><input type="text" id="txtWSDLAddress" size="50" /></td>
			</tr>
			<tr>
				<td><label class="label">Operation Name: </label></td>
				<td align="left"><select name="selWSOperation"
					id="selOWSOperation" style="width: 200px">
						<option value="None">None</option>
				</select></td>
			</tr>
		</table>
	</div>
	
	<!-- adding dropbox popup -->
	<div id="DropboxDiv" style = "overflow:'hidden';display:none;">
		<table cellpadding="2px" cellspacing="1px" bgcolor="#B8E6E6" 
			width="420px" class="tableBorder" align="center">
			<tr>
				<td class="label" align="left"><B>Token:</B></td>
				<td align="left">
					<input id='tokenKeyId'></input>
				</td>
			</tr>	
		</table>
		<br>
		<p>Hint: First-time user please refer to the following steps to start:<br>
			Step 1. Sign in to your Dropbox account <a href="http://www.dropbox.com/login" target="_blank">Here</a>.<br>
			Step 2. Create your Dropbox App <a href="http://www.dropbox.com/login?cont=https%3A%2F%2Fwww.dropbox.com%2Fdevelopers%2Fapps%2Fcreate" target="_blank">Here</a>.<br>
			Step 3. After creating the app, record the App Key, App Secret and Access Token generated, and fill them into the above textboxes.<br>
			Step 4. Click on Submit button to access your dropbox files.
		</p>
	</div>
	
</body>
</html>
