/**
 * low-level code that invokes mxGraph API to manipulate visual element of webbench (data products,
 * workflow boxes, ports, output stubs etc. ) 
 * 
 * @author  Aravind Mohan, Andrey Kashlev.
 */

function makePortDraggable(direction) {
	var imgAndText;
	var imgForCanvasInput = '<img src="images/icons48/inputPort.png" width="24" height="24">';
	var imgForCanvasOutput = '<img src="images/icons48/outputPort.png" width="24" height="24" style="padding-left:0px;">';
	// this section creates labels, which are html div elements that will be
	// displayed on canvas, each time user drags-and-drops input or output port
	// from the sidebar to the canvas.
	
	var labelOnCanvas = "";
	if(direction.indexOf("InputPort") != -1){
		imgAndText = '<div id="someid" typeOfPort="Integer" '
			+ 'style="float:left;display:inline;font-size:11pt;width:25px;direction:rtl;padding-right:5px;">somename</div>';
		imgAndText = imgAndText + imgForCanvasInput;
		labelOnCanvas = '<div>' + imgAndText + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</div>';
	}
	else{
		imgAndText = '<span style="display:inline-block; vertical-align:middle;font-size:12pt;padding-right:5px;">'+imgForCanvasOutput + '</span>';
		imgAndText = imgAndText + '<span id="someid" typeOfPort="Integer" style="font-size:11pt;">somename</span>';
		labelOnCanvas = '<div style="width:30px;">' + imgAndText + '</div>';
	}
	
	var graph = graphG;
	var funct = function(graph, evt, cell, x, y) {
		var parent = graph.getDefaultParent();
		var model = graph.getModel();
		var v1 = null;
		model.beginUpdate();
		try {
			var labelOnCanvas2 = labelOnCanvas;
			var id;
			if(labelOnCanvas.indexOf("inputPort") != -1){
				id = "i" + numberOfInputPorts;
				numberOfInputPorts++;
			}
			else {
				id = "o" + numberOfOutputPorts;
				numberOfOutputPorts++;
			}
			
			labelOnCanvas2 = labelOnCanvas2.replace("someid", id);						
			labelOnCanvas2 = labelOnCanvas2.replace("somename", id);
			//portIdsToTypes[id] = "Integer";
			//portIdsToNames[id] = id;
			
			
			v1 = graph.insertVertex(parent, null, labelOnCanvas2, x, y, 24, 24, "opacity=0");
			v1.setConnectable(true);
			graph.getTooltipForCell = function(v1)
		    {
		     var label = this.convertValueToString(v1);
		     if(label.indexOf("inputPort") != -1 || label.indexOf("outputPort") != -1)
		    	 return 'double click to change port name/type';				     
		     return '';
		    };
			
		} finally {
			model.endUpdate();
		}
		graph.setSelectionCell(v1);				
	};

	var dragElt = document.createElement('div'); 
	dragElt.style.border = 'dashed black 1px';
	dragElt.style.width = '24px';
	dragElt.style.height = '24px';
	dragElt.style.direction = 'rtl';
	var portInTree;
	if(labelOnCanvas.indexOf("inputPort") != -1)
		portInTree = document.getElementById('InputPort');
	else
		portInTree = document.getElementById('OutputPort');
	var ds = mxUtils.makeDraggable(portInTree, graph, funct, dragElt, 0, 0, true,
			true);
	ds.setGuidesEnabled(true);
};

function storeWorkflowConstructInfo(userid, wfname, wftaskid){
    var e1 = document.getElementById("selOConstructType");
    var selectedConsType = e1.options[e1.selectedIndex].value;
    var key = document.getElementById("userInputForKey").value;
    
    var params = "action=storeConstructInfo&name=" + wfname.trim() + "&userid=" + userid.trim()
        + "&taskid=" + wftaskid.trim() 
        + "&constructtype=" + selectedConsType.trim()
        + "&key=" + key.trim();
    
    function responseHandler(argArray, responseFromServer) {
        updateStatus("Workflow Construct Applied successfully");
    }
    ajaxCall(params, responseHandler, null);
    
}

function configureTask(workflowName) {
	var graph = graphG;
	var cellID = graph.getSelectionCell().getId();
	var taskID = parseInt(cellID, 10);
	taskID = taskID - 2;
	workflowName = workflowName + taskID;
    if (currentWorkflowName == "Untitled Workflow")
    		alert("Please save your workflow first before applying a construct...")
    	else{
    		$(function() {
        		var paramUserID = $("#usrId").val();
    		var paramWorkfowName = currentWorkflowName;
    		
            $("#WFConfDiv").data('paramConstruct', {
    			userID : paramUserID,
    			wfName : paramWorkfowName
    		}).dialog(
                    {
                        dialogClass : 'DynamicDialogStyle',
                        autoOpen : true,
                        modal : true,
                        buttons : {
                            "Configure" : function() {
                                $("#WFConfDiv").dialog('close');
                                storeWorkflowConstructInfo(
        							$(this).data("paramConstruct").userID,
        							$(this).data("paramConstruct").wfName,
        							workflowName); 	
                            		alert("Added construct to " + $(this).data("paramConstruct").wfName + " successfully.");
                        }
                        },
                        height : 180,
                        width : 300,
                        title : 'Configure Task '
                    });

        });
    	}
 }

function makeWorkflowDraggable(jsonMetadata){
	var blockWidth = 50;
	var blockHeight = 60;
	var graph = graphG;
	var functToDragExperiment = function(graph, evt, cell, x, y) {	
		displayWorkflowOnCanvas(jsonMetadata);
	};	
	
	var workflowInTree = document.getElementById(jsonMetadata.dataPath);
	// scrollableSidebar.appendChild(wfName);
	var dragElt = document.createElement('div');
	dragElt.style.border = 'dashed black 1px';
	dragElt.style.width = blockWidth;
	dragElt.style.height = blockHeight;
	var ds = mxUtils.makeDraggable(workflowInTree, graph, functToDragExperiment, dragElt, 0, 0, true, true);
	ds.setGuidesEnabled(true);
	//document.body.appendChild(workflowInTree);
}


function makeTaskDraggable(jsonMetadata){
	var blockWidth = 50;
	var blockHeight = 60;
	var graph = graphG;
	
	
	var functToDragTask = function(graph, evt, cell, x, y) {
			blockWidth = 50;
			blockHeight = 60;
			// alert(JSON.stringify(portsInfo));
			var parent = graph.getDefaultParent();
			var model = graph.getModel();
			var v1 = null;
			model.beginUpdate();
			
			// variables that determine positioning of ports, e.g. if there
			// are 20 input ports, we need to compute their coordinates
			// accordingly:
			var portSize = 8; // e.g. each port image is 8x8 pixels. Port
								// here refers to port shown on the workflow
								// component box
			// after box is dragged-and-dropped from the sidebar to the
			// canvas.
			var minRatio = 0.15; 
			var maxRatio = 0.85;
			// i.e. port should be no closer than 0.15 of component box
			// height to its top corner,
			// and no closer than 0.15 of box height to its bottom corner
			var arrayOfInputPortIDs = [];
			for(var i = 0; i < jsonMetadata.inputPorts.length; i++){
				arrayOfInputPortIDs[i] = jsonMetadata.inputPorts[i].portname;
			}
			
			
			var arrayOfOutputPortIDs = [];
			for(var i = 0; i < jsonMetadata.outputPorts.length; i++){
				arrayOfOutputPortIDs[i] = jsonMetadata.outputPorts[i].portname;
			}
			
			
			//var arrayOfInputPortIDs = ["i1", "i2"];
			
			//var arrayOfOutputPortIDs = ["o1","o2"];
			
			var numberOfInputs = arrayOfInputPortIDs.length;
			var numberOfOutputs = arrayOfOutputPortIDs.length;
	
			var spacingBetweenInputPorts = (blockHeight*maxRatio - portSize - blockHeight*minRatio - portSize*numberOfInputs)/(numberOfInputs-1);
			
			var spacingRatio = 1.1;
			while(spacingBetweenInputPorts < portSize*spacingRatio){
				blockHeight++;
				spacingBetweenInputPorts = (blockHeight*maxRatio - blockHeight*minRatio - portSize*numberOfInputs)/(numberOfInputs-1);			
			}
			
			var spacingBetweenOutputPorts = (blockHeight*maxRatio - blockHeight*minRatio - portSize*numberOfOutputs)/(numberOfOutputs-1);
			while(spacingBetweenOutputPorts < portSize*spacingRatio){
				blockHeight++;
				spacingBetweenOutputPorts = (blockHeight*maxRatio - blockHeight*minRatio - portSize*numberOfOutputs)/(numberOfOutputs-1);			
			}
			blockHeight = blockHeight + portSize;
			// blockWidth = blockHeight*1.1;
			
			var inputPortsStep = (maxRatio - minRatio)/(numberOfInputs-1);
			var outputPortsStep = (maxRatio - minRatio)/(numberOfOutputs-1);
			var Ycoordinates = [];
			var currentYcoordinate = minRatio;
			var label = '<div style="visibility:hidden;width:0px;height:0px;">workflowComponent ' + jsonMetadata.dataDescription + '</div>';
			try {
			v1 = graph.insertVertex(parent, null, label, x, y, blockWidth, blockHeight);
			v1.setConnectable(false);
			
			var nameOfWorkflow = '<p style="font-size:11pt;">' + jsonMetadata.dataDescription + '</p>';
			
			
			var nameAboveBox = graph.insertVertex(v1, null, nameOfWorkflow, blockWidth/2, blockHeight - 60, null, null, "opacity=0");
			nameAboveBox.geometry.offset = new mxPoint(-5, +85);
			
			
			if(numberOfInputs == 1)
				currentYcoordinate = 0.5;
			for(var i=0; i<numberOfInputs; i++){
				Ycoordinates.push(currentYcoordinate);
				
				var portID = arrayOfInputPortIDs[i];
				//var hint = jsonMetadata.interface[portID].portName;
				//portHint = '<div>' + jsonMetadata.interface[portID].portName + '</div>';
				var portHint = '<div>' + portID + '<div style="visibility:hidden;width:0px;height:0px;">' 
				+ portID + '</div></div>';
				var port = graph.insertVertex(v1, null, portHint, 0, currentYcoordinate, portSize, portSize, 
						'port;image=editors/images/overlays/port.png;align=right;imageAlign=right;spacingRight=18',true);
				port.geometry.offset = new mxPoint(-(portSize/2), -(portSize/2));
				
				currentYcoordinate = currentYcoordinate + inputPortsStep;
			}
			
			Ycoordinates = [];
			currentYcoordinate = minRatio;
			
			if(numberOfOutputs == 1)
				currentYcoordinate = 0.5;
			
			for(var i=0; i<numberOfOutputs; i++){
				Ycoordinates.push(currentYcoordinate);
				
				var portID = arrayOfOutputPortIDs[i];
				//var hint = jsonMetadata.interface[portID].portName;
				var portHint = '<div>' + portID + '<div style="visibility:hidden;width:0px;height:0px;">' 
				+ portID + '</div></div>';
				var port = graph.insertVertex( v1, null, portHint, 1, currentYcoordinate, portSize, portSize,
						'port;image=editors/images/overlays/port.png;spacingLeft=18', true);
				port.geometry.offset = new mxPoint(-(portSize/2), -(portSize/2));
				
				currentYcoordinate = currentYcoordinate + outputPortsStep;
			}
			
			} finally {
				model.endUpdate();
			}
	
			graph.setSelectionCell(v1);
		};

	var workflowInTree = document.getElementById(jsonMetadata.dataName);
	
	// scrollableSidebar.appendChild(wfName);
	var dragElt = document.createElement('div');
	dragElt.style.border = 'dashed black 1px';
	dragElt.style.width = blockWidth;
	dragElt.style.height = blockHeight;
	var ds;
	ds = mxUtils.makeDraggable(workflowInTree, graph, functToDragTask, dragElt, 0, 0, true, true);
	
	ds.setGuidesEnabled(true);
}






function makeDropboxDraggable(jsonDPMedatata) {
	
	var graph = graphG;
	
	var funct = function(graph, evt, cell, x, y) {
		
		var parent = graph.getDefaultParent();
		
		var model = graph.getModel();
		
		var v1 = null;
		var v2 = null;
		model.beginUpdate();
		try {			
			
			var label = '<div style="visibility:hidden;width:0px;height:0px;">dataProduct ' + jsonDPMedatata['dataName'] +
			' dataType ' + jsonDPMedatata['dataType'] + '</div>';
			
			var dpNameBelowBox = jsonDPMedatata['dataName'];
			dpNameBelowBox = dpNameBelowBox.substring(dpNameBelowBox.lastIndexOf("/")+1, dpNameBelowBox.length);
			
			
			v1 = graph.insertVertex(parent, null, label, x, y, 80, 60, "fillColor=#14DC4F;gradientColor=#14DC4F;opacity=100");
			v1.setConnectable(false);
			var nameOfDP = '<div style="width:40px;height:10px;"><p style="font-size:11pt;height:8px;">' + dpNameBelowBox + '</p></div>';
			
			var nameAboveBox = graph.insertVertex(v1, null, nameOfDP, 40,0, null, null, "opacity=0");
			
			nameAboveBox.isCellSelectable = false;
			
			nameAboveBox.geometry.offset = new mxPoint(-25, +75);
			
			//display description string on top of data product box:
			var dataDescriptionString = jsonDPMedatata.dataDescription;
			
			console.log(jsonDPMedatata);
			
			
			var htmlInsideDPBox = null;
			htmlInsideDPBox = '<div style="background-color:#14DC4F;font-size:10pt;text-align:left;horizontal-align:left;">' + 
			jsonDPMedatata['dataType'] ;
			
			htmlInsideDPBox = htmlInsideDPBox + 
			'<br><a href="#"style="background:none;text-decoration : underline;" onclick=\'viewDataProductValue(' + JSON.stringify(jsonDPMedatata['dataName']) + ')\'>value</a>';
				
			v2 = graph.insertVertex(v1, null, htmlInsideDPBox, 40, 25, null, null, "opacity=0");
			v2.setConnectable(false);
			
			var port = graph.insertVertex(v1, null, 'Port', 1, 0.5, 8, 8,
					'port;image=editors/images/overlays/port.png;spacingLeft=18', true);
			port.geometry.offset = new mxPoint(-4, -4);
			
			
	
		} finally {
			model.endUpdate();
		}
		graph.setSelectionCell(v1);
	};

	var dpInTree = document.getElementById(jsonDPMedatata['dataName']);
	
	var dragElt = document.createElement('div'); 
	dragElt.style.border = 'dashed black 1px';
	dragElt.style.width = '80px';
	dragElt.style.height = '60px';

	var ds = mxUtils.makeDraggable(dpInTree, graph, funct, dragElt, 0, 0, true,
			true);
	ds.setGuidesEnabled(true);
};


function makeOutputStubDraggable(){
	var graph = graphG;
	var nameOfDataProduct = 'outputDP' + numberOfOutputDataProducts;
	
	var funct = function(graph, evt, cell, x, y) {
		var parent = graph.getDefaultParent();
		var model = graph.getModel();
		var v1 = null;
		var v2 = null;
		model.beginUpdate();
		try {	
			nameOfDataProduct = 'outputDP' + numberOfOutputDataProducts;
			numberOfOutputDataProducts++;
			var label = '<div style="visibility:hidden;width:0px;height:0px;">dataProduct ' + nameOfDataProduct + 
			' stubForOutputDP ' + '</div>';
			v1 = graph.insertVertex(parent, null, label, x, y, 80, 60, "fillColor=#EDE579;gradientColor=#EDE579;opacity=100");
			v1.setConnectable(false);
			var nameOfDP = '<div style=""><p style="font-size:11pt;height:10px;">' + nameOfDataProduct + '</p></div>';
			var nameAboveBox = graph.insertVertex(v1, null, nameOfDP, 40, 0, null, null, "opacity=0");
			nameAboveBox.isCellSelectable = false;
			nameAboveBox.geometry.offset = new mxPoint(0, +85);
			
			var port = graph.insertVertex(v1, null, 'Port', 0, 0.5, 8, 8,
					'port;image=editors/images/overlays/port.png;spacingLeft=18', true);
			port.geometry.offset = new mxPoint(-4, -4);
	
		} finally {
			model.endUpdate();
		}
		graph.setSelectionCell(v1);
	};

	var dpInTree = document.getElementById(nameOfDataProduct);

	var dragElt = document.createElement('div'); 
	dragElt.style.border = 'dashed black 1px';
	dragElt.style.width = '80px';
	dragElt.style.height = '60px';

	var ds = mxUtils.makeDraggable(dpInTree, graph, funct, dragElt, 0, 0, true,
			true);
	ds.setGuidesEnabled(true);
}

function addOutputDataProduct(outputDPName, x, y) {
	var graph = graphG;
		var parent = graph.getDefaultParent();
		var model = graph.getModel();
		var v1 = null;
		model.beginUpdate();
		try {			
			var label = '<div id="' + outputDPName + '"style="visibility:hidden;height:0px;">dataProduct ' + outputDPName + '</div>';
			v1 = graph.insertVertex(parent, null, label, x, y, 80, 60, "fillColor=#14DC4F;gradientColor=#14DC4F;opacity=100");
			v1.setConnectable(false);
			var nameOfDP = '<div style=""><p style="font-size:11pt;height:10px;">' + outputDPName + '</p></div>';
			var nameAboveBox = graph.insertVertex(v1, null, nameOfDP, 40, 0, null, null, "opacity=0");
			nameAboveBox.isCellSelectable = false;
			nameAboveBox.geometry.offset = new mxPoint(0, -15);
			
			var port = graph.insertVertex(v1, null, 'Port', 0, 0.5, 8, 8,
					'port;image=editors/images/overlays/port.png;spacingLeft=18', true);
			port.geometry.offset = new mxPoint(-4, -4);

			
		} finally {
			model.endUpdate();
		}
		graph.setSelectionCell(v1);
		
		return port;
};

function addDataProductsToDanglingOutputs(){
	
	var danglingPortIDs = getDanglingPors();
	
	for(var i=0; i<danglingPortIDs.length; i++){
		var workflowComponent = graphG.getModel().getCell(danglingPortIDs[i]).getParent();
		var componentName = workflowComponent.getValue();
		componentName = componentName.substring(componentName.indexOf("workflowComponent") + 18, componentName.length - 6);
		
		var portName = graphG.getModel().getCell(danglingPortIDs[i]).getValue();
		portName = portName.substring(5, portName.length);
		portName = portName.substring(0, portName.indexOf("<div style="));
		var dataProductName = componentName + workflowComponent.getId() + "." + portName;
		var mxGeometry = workflowComponent.getGeometry();
		var distanceToNewDataProduct = 30;
		
		var x = mxGeometry.x + mxGeometry.width + distanceToNewDataProduct;
		var dpNameLength = getWidthOfDPnameAboveBox(dataProductName);
		x = x + dpNameLength/2 - 40;
		var newDPport =
		addOutputDataProduct(dataProductName, x, mxGeometry.y + mxGeometry.height/2 - 30);
		
		var model = graphG.getModel().beginUpdate();
		try {
		 var newEdge = graphG.insertEdge(graphG.getDefaultParent(), null, '', graphG.getModel().getCell(danglingPortIDs[i]), newDPport);
		} 
		finally {
			graphG.getModel().endUpdate();
		}
	}
}

function getWidthOfDPnameAboveBox(dataProductName){
	return dataProductName.length*7;
}

function getAllCells(){
	return getAllCellsFromGraph(new Array(), graphG.getModel().getChildCells(graphG.getDefaultParent(), true, true));
}

function getAllCellsFromGraph(cellsArray, cellsToBeAdded) {
	for ( var i = 0; i < cellsToBeAdded.length; i++) {
		cellsArray.push(cellsToBeAdded[i]);
		if (cellsToBeAdded[i].isVertex() && !cellsToBeAdded[i].isEdge()) {
			var children = graphG.getModel().getChildCells(cellsToBeAdded[i]);
			getAllCellsFromGraph(cellsArray, children);
		}
	}
	return cellsArray;
}

function getAllEdges(){
	var arrayOfEdges = [];
	var allMxCells = getAllCells();
	for(var j=0; j<allMxCells.length; j++){
		if(allMxCells[j].isEdge())
			arrayOfEdges.push(allMxCells[j]);
	}
	return arrayOfEdges;
}

function getDanglingPors(){
	var danglingPortIDs = [];
	var allMxCells = getAllCells();
	
	for(var i=0; i<allMxCells.length; i++){
		var mxGeometry = allMxCells[i].getGeometry();
		if(mxGeometry.x == "1" && allMxCells[i].getParent().value.indexOf("workflowComponent") != -1){
			var portID = allMxCells[i].getId();
			var allEdges = getAllEdges();
			
			if(allEdges.length == 0)
				if(!contains(danglingPortIDs, portID))
					danglingPortIDs.push(portID);
			
			for(var j=0; j<allEdges.length; j++){
					if(findEdgeConnectingToThisMxCell(portID) == null){
						if(!contains(danglingPortIDs, portID))
							danglingPortIDs.push(portID);
					}
			}
		}
	}
	return danglingPortIDs;
}

function contains(array, element){
	for(var i=0; i<array.length; i++){
		if(array[i] == element)
			return true;
	}
	return false;
}

function findEdgeConnectingToThisMxCell(mxCellID){
	var allMxCells = getAllCells();
	
	for(var j=0; j<allMxCells.length; j++){
		if(allMxCells[j].isEdge()){
			if(allMxCells[j].source.getId() == mxCellID || allMxCells[j].target.getId() == mxCellID)
				return allMxCells[j];
		}
	}
	return null;
}

function updatePort(newName, newType){
	
	var allMxCells = getAllCells();
	
	for(var i=0; i<allMxCells.length; i++){
		if(graphG.getSelectionModel().isSelected(allMxCells[i])){
			var value = allMxCells[i].getValue();
			var newValue = value;
			if(value.indexOf("span") == -1){
				//it's input port
				var beforeName = value.substring(0, value.indexOf("direction:rtl;padding-right:5px;\">")+34);
				var afterName = value.substring(value.indexOf("</div><img src=\"images/icons48/inputPort.png"), value.length);
				newValue = beforeName + newName + afterName;
				
				var beforeType = newValue.substring(0, newValue.indexOf("typeOfPort=")+12);
				var afterType = newValue.substring(newValue.indexOf("\" style=\"float:left;display:inline"), newValue.length);
				
				newValue = beforeType + newType + afterType;
			}
			else {
				//it's output port
				var beforeName = value.substring(0, value.indexOf("style=\"font-size:11pt;\">") + 24);
				var afterName = value.substring(value.indexOf("</span></div>"), value.length);
				newValue = beforeName + newName + afterName;
				
				var beforeType = newValue.substring(0, newValue.indexOf("typeOfPort=\"") + 12);
				var afterType = newValue.substring(newValue.indexOf("\" style=\"font-size:11pt;"), newValue.length);
				newValue = beforeType + newType + afterType;
			}
			
			var model = graphG.getModel().beginUpdate();
			try {
				allMxCells[i].setValue(newValue);
			} 
			finally {
				graphG.getModel().endUpdate();
				graphG.refresh(allMxCells[i]);
			}
		}
	}
}

function updateDP(newName){
	
	var allMxCells = getAllCells();
	
	for(var i=0; i<allMxCells.length; i++) {
		if(graphG.getSelectionModel().isSelected(allMxCells[i])){
			var value = allMxCells[i].getValue();
			var newValue = value;
			
			var oldName = value.substring(value.indexOf("dataProduct") + 12, value.indexOf("stub") -1);
			newValue = newValue.replace(oldName, newName);
			var children = graphG.getModel().getChildCells(allMxCells[i]);
			for(var j=0; j<children.length; j++){
				var valueOfCurrChild = children[j].getValue();
				if(valueOfCurrChild.indexOf('p style="font-size:11pt;height:10px;">' + oldName) != -1){
					var newValueForTitleAboveBox = valueOfCurrChild.replace(oldName, newName);
					var model = graphG.getModel().beginUpdate();
					try {
						children[j].setValue(newValueForTitleAboveBox);
					} 
					finally {
						graphG.getModel().endUpdate();
						graphG.refresh(children[j]);
					}
					
				}
			}
			var model = graphG.getModel().beginUpdate();
			try {
				allMxCells[i].setValue(newValue);
			} 
			finally {
				graphG.getModel().endUpdate();
				graphG.refresh(allMxCells[i]);
			}
		}
	}
}

function getSelectedPortNameAndType(){
var allMxCells = getAllCells();
	
	for(var i=0; i<allMxCells.length; i++){
		if(graphG.getSelectionModel().isSelected(allMxCells[i])){
			var value = allMxCells[i].getValue();
			var portName;
			var portType;
			if(value.indexOf("span") == -1){
				portName = value.substring(value.indexOf("direction:rtl;padding-right:5px;\">")+34,
						value.indexOf("</div><img src=\"images/icons48/inputPort.png"));
				
				portType = value.substring(value.indexOf("typeOfPort=")+12, value.indexOf("\" style=\"float:left;display:inline"));
				}
			else
				{
				portName = value.substring(value.indexOf("style=\"font-size:11pt;\">") + 24, value.indexOf("</span></div>"));
				
				portType = value.substring(value.indexOf("typeOfPort=\"") + 12,
						value.indexOf("\" style=\"font-size:11pt;"));
				}
		}
	}
	var resultArray = [];
	resultArray.push(portName);
	resultArray.push(portType);
	
	return resultArray; 
}

function getSelectedDPNameAndType(){
	var result = [];
	var allMxCells = getAllCells();
		
		for(var i=0; i<allMxCells.length; i++){
			if(graphG.getSelectionModel().isSelected(allMxCells[i])){
				var value = allMxCells[i].getValue();
				var name = value.substring(value.indexOf("dataProduct") + 12, value.indexOf("stub") -1);
				var type = "stub";
				result.push(name);
				result.push(type);
				return result;
			}
		}
		
		return result; 
	}

function selectDataProduct(dataName){
	var dataNameTrimmed = dataName.trim();
	
	var allMxCells = getAllCells();
	for(var i=0; i<allMxCells.length; i++){
		var value = allMxCells[i].getValue();
		if(value.indexOf("dataProduct") != -1){
			var name = value.substring(value.indexOf("dataProduct") + 12, value.indexOf("dataType") -1);
			name = name.trim();
			if(name.indexOf(dataNameTrimmed) == 0 && name.length == dataNameTrimmed.length){
				graphG.getSelectionModel().setCell(allMxCells[i]);
			}
		}
	}
	
}

function getAllStubNames(){
	var allStubNames = [];
	var allMxCells = getAllCells();
	
	for(var i=0; i<allMxCells.length; i++){
		var value = allMxCells[i].getValue();
		if(value.indexOf("stubForOutputDP") != -1){
			var name = value.substring(value.indexOf("dataProduct") + 12, value.indexOf("stubForOutputDP") -1);
			allStubNames.push(name);
		}
	}
	return allStubNames;
}

function clearAllStubsContents(){
	var allStubNames = getAllStubNames();
	
	var model = graphG.getModel();
	model.beginUpdate();
	
	try {
			for(var i=0; i<allStubNames.length; i++){
				var stubCell = findCellRepresentingStub(allStubNames[i]);
				var childrenOfThisStub = graphG.getModel().getChildCells(stubCell);
				for(var j=0; j<childrenOfThisStub.length; j++){
					if(childrenOfThisStub[j].getValue().indexOf('<p style="font-size:11pt;height:10px;">') == -1 && 
							childrenOfThisStub[j].getValue().indexOf('Port') != 0){
						model.remove(childrenOfThisStub[j]);
					}
				}
			}
		
	} finally {
		model.endUpdate();
	}
	
}

function fillStubWithNewDP(stubName){

	var stubCell = findCellRepresentingStub(stubName);
	var model = graphG.getModel();
	var v2 = null;
	model.beginUpdate();
	try {
			//remove all children except for port:
			var childrenOfThisStub = graphG.getModel().getChildCells(stubCell);
			for(var i=0; i<childrenOfThisStub.length; i++){
				if(childrenOfThisStub[i].getValue().indexOf('<p style="font-size:11pt;height:10px;">') == -1 && 
						childrenOfThisStub[i].getValue().indexOf('Port') != 0)
					model.remove(childrenOfThisStub[i]);
			}
			// code add here.
			console.log(stubName);
			var htmlInsideDPBox = null;
			htmlInsideDPBox = '<div style="background-color:#EDE579;font-size:10pt;text-align:left;horizontal-align:left;">' + 
			stubName;
			var result = "/DATAVIEW-OUTPUT/"+ stubName +".txt";
			htmlInsideDPBox = htmlInsideDPBox + 
			'<br><a href="#" style="background:none;text-decoration : underline;" onclick=\'viewDataProductValue(' + JSON.stringify(result) + ')\'>value</a>';	
			v2 = graphG.insertVertex(stubCell, null, htmlInsideDPBox, 40, 25, null, null, "opacity=0");
			v2.setConnectable(false);
	} finally {
		model.endUpdate();
	}
	//addDataProductToTree(metadataForThisDP.dataName, metadataForThisDP.dataName);
	//makeDPDraggable(metadataForThisDP);
	//focusOnItemInTree(metadataForThisDP.dataName);
}

function findCellRepresentingStub(stubName){
var allMxCells = getAllCells();
	
	for(var i=0; i<allMxCells.length; i++){
		var value = allMxCells[i].getValue();
		if(value.indexOf('<div style="visibility:hidden;width:0px;height:0px;">dataProduct') != -1 && value.indexOf(stubName) != -1){
			return allMxCells[i];
		}
	}
	return null;
}


var formatXml = this.formatXml = function (xml) {
    var reg = /(>)(<)(\/*)/g;
    var wsexp = / *(.*) +\n/g;
    var contexp = /(<.+>)(.+\n)/g;
    xml = xml.replace(reg, '$1\n$2$3').replace(wsexp, '$1\n').replace(contexp, '$1\n$2');
    var pad = 0;
    var formatted = '';
    var lines = xml.split('\n');
    var indent = 0;
    var lastType = 'other';
    // 4 types of tags - single, closing, opening, other (text, doctype, comment) - 4*4 = 16 transitions 
    var transitions = {
        'single->single'    : 0,
        'single->closing'   : -1,
        'single->opening'   : 0,
        'single->other'     : 0,
        'closing->single'   : 0,
        'closing->closing'  : -1,
        'closing->opening'  : 0,
        'closing->other'    : 0,
        'opening->single'   : 1,
        'opening->closing'  : 0, 
        'opening->opening'  : 1,
        'opening->other'    : 1,
        'other->single'     : 0,
        'other->closing'    : -1,
        'other->opening'    : 0,
        'other->other'      : 0
    };

    for (var i=0; i < lines.length; i++) {
        var ln = lines[i];
        var single = Boolean(ln.match(/<.+\/>/)); // is this line a single tag? ex. <br />
        var closing = Boolean(ln.match(/<\/.+>/)); // is this a closing tag? ex. </a>
        var opening = Boolean(ln.match(/<[^!].*>/)); // is this even a tag (that's not <!something>)
        var type = single ? 'single' : closing ? 'closing' : opening ? 'opening' : 'other';
        var fromTo = lastType + '->' + type;
        lastType = type;
        var padding = '';

        indent += transitions[fromTo];
        for (var j = 0; j < indent; j++) {
            padding += '    ';
        }

        formatted += padding + ln + '\n';
    }

    return formatted;
};