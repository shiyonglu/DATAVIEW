/**
 * The following functions are used to interact with the workflow engine
 * and setup the webbench runtime platform.
 * @author  Aravind Mohan, Andrey Kashlev.
*/

function main(banner, container, outline, toolbar, sidebarL, status, sidebarR) {
	// Checks if the browser is supported
	if (!mxClient.isBrowserSupported()) {
		// Displays an error message if the browser is not supported.
		mxUtils.error('Browser is not supported!', 200, false);
	} else {
		// Assigns some global constants for general behaviour, eg. minimum
		// size (in pixels) of the active region for triggering creation of
		// new connections, the portion (100%) of the cell area to be used
		// for triggering new connections, as well as some fading options for
		// windows and the rubberband selection.
		mxConstants.MIN_HOTSPOT_SIZE = 16;
		mxConstants.DEFAULT_HOTSPOT = 1;

		// Enables guides
		mxGraphHandler.prototype.guidesEnabled = true;

		// Alt disables guides
		mxGuide.prototype.isEnabledForEvent = function(evt) {
			return !mxEvent.isAltDown(evt);
		};

		// Enables snapping waypoints to terminals
		mxEdgeHandler.prototype.snapToTerminals = true;

		// Workaround for Internet Explorer ignoring certain CSS directives
		if (mxClient.IS_IE) {
			new mxDivResizer(container);
			new mxDivResizer(outline);
			new mxDivResizer(toolbar);
			new mxDivResizer(sidebarL);
			new mxDivResizer(status);
			new mxDivResizer(sidebarR);
		}

		
		// Creates a wrapper editor with a graph inside the given container.
		// The editor is used to create certain functionality for the
		// graph, such as the rubberband selection, but most parts
		// of the UI are custom in this example.
		var editor = new mxEditor();
		var graph = editor.graph;
		graphG = graph;
		sidebarLeft = sidebarL;
		sidebarRight = sidebarR;
		var containerG = container;

		var model = graph.getModel();

		// Disable highlight of cells when dragging from toolbar
		graph.setDropEnabled(false);

		// Uses the port icon while connections are previewed
		graph.connectionHandler.getConnectImage = function(state) {
			return new mxImage(state.style[mxConstants.STYLE_IMAGE], 16, 16);
		};

		// Centers the port icon on the target port
		graph.connectionHandler.targetConnectImage = true;

		// Does not allow dangling edges
		graph.setAllowDanglingEdges(false);

		// Sets the graph container and configures the editor
		editor.setGraphContainer(container);
		var config = mxUtils.load('editors/config/keyhandler-commons.xml')
				.getDocumentElement();
		editor.configure(config);

		// Defines the default group to be used for grouping. The
		// default group is a field in the mxEditor instance that
		// is supposed to be a cell which is cloned for new cells.
		// The groupBorderSize is used to define the spacing between
		// the children of a group and the group bounds.
		var group = new mxCell('Group', new mxGeometry(), 'group');
		group.setVertex(true);
		group.setConnectable(false);
		editor.defaultGroup = group;
		editor.groupBorderSize = 20;

		// Disables drag-and-drop into non-swimlanes.
		graph.isValidDropTarget = function(cell, cells, evt) {
			return this.isSwimlane(cell);
		};

		// Disables drilling into non-swimlanes.
		graph.isValidRoot = function(cell) {
			return this.isValidDropTarget(cell);
		}

		// Does not allow selection of locked cells
		graph.isCellSelectable = function(cell) {
			return !this.isCellLocked(cell);
		};

		graph.isCellMovable = function(cell) {

			if ((cell.getValue().indexOf("inputPort")) != -1
					|| (cell.getValue().indexOf("outputPort")) != -1
					|| cell.getValue().indexOf("port") != -1)
				return true;

			if (graphG.getModel().getChildCells(cell).length == 0)
				return false;

			return true;
		};

		// Returns a shorter label if the cell is collapsed and no
		// label for expanded groups
		mxGraph.foldingEnabled = false;

		graph.getLabel = function(cell) {
			var tmp = mxGraph.prototype.getLabel.apply(this, arguments); // "supercall"

			if (this.isCellLocked(cell)) {
				// Returns an empty label but makes sure an HTML
				// element is created for the label (for event
				// processing wrt the parent label)
				return '';
			}
			//else if (this.isCellCollapsed(cell))
			//{
			//	var index = tmp.indexOf('</h1>');
			//	
			//	if (index > 0)
			//	{
			//		tmp = tmp.substring(0, index+5);
			//	}
			//}

			return tmp;
		}

		// Disables HTML labels for swimlanes to avoid conflict
		// for the event processing on the child cells. HTML
		// labels consume events before underlying cells get the
		// chance to process those events.
		//
		// NOTE: Use of HTML labels is only recommended if the specific
		// features of such labels are required, such as special label
		// styles or interactive form fields. Otherwise non-HTML labels
		// should be used by not overidding the following function.
		// See also: configureStylesheet.
		graph.isHtmlLabel = function(cell) {
			return !this.isSwimlane(cell);
		}

		// To disable the folding icon, use the following code:
		graph.isCellFoldable = function(cell) {
			return false;
		}

		// Shows a "modal" window when double clicking a vertex.
		graph.dblClick = function(evt, cell) {
			// Do not fire a DOUBLE_CLICK event here as mxEditor will
			// consume the event and start the in-place editor.
			if (this.isEnabled() && !mxEvent.isConsumed(evt) && cell != null
					&& this.isCellEditable(cell)) {
				if (this.model.isEdge(cell) || !this.isHtmlLabel(cell)) {
					this.startEditingAtCell(cell);
				} else {
					//var content = document.createElement('div');
					createCustomDialogWindow(this.convertValueToString(cell));
					//alert("inner: " + this.convertValueToString(cell));
				}
			}

			// Disables any default behaviour for the double click
			mxEvent.consume(evt);
		};

		// Enables new connections
		graph.setConnectable(true);

		// Adds all required styles to the graph (see below)
		configureStylesheet(graph);

		// Creates a new DIV that is used as a toolbar and adds
		// toolbar buttons.
		var spacer = document.createElement('div');
		spacer.style.display = 'inline';
		spacer.style.padding = '8px';

		var spacerS = document.createElement('div');
		spacerS.style.display = 'inline';
		spacerS.style.padding = '2px';

		editorG = editor;

		editor.addAction(
						'saveAs',
						function(editor, cell) {

							var oldName = currentWorkflowName;
							var wfName = prompt("Please enter workflow name",
									oldName);
							if (wfName == null)
								return;
							currentWorkflowName = wfName;
							var enc = new mxCodec(mxUtils.createXmlDocument());
							var node = enc.encode(editor.graph.getModel());
							var diagramXML = mxUtils.getPrettyXml(node);
							diagramXML = replaceAll(diagramXML, '&', '^');
							//console.log(diagramXML);
							if (window.XMLHttpRequest) {
								xmlhttp = new XMLHttpRequest();
							} else {
								xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
							}
							var url = "./Mediator?";

							var params = "action=saveAs&name=" + wfName
									+ "&diagram=" + diagramXML;

							function responseHandler(argArray,
									responseFromServer) {
									addWFToSidebarTreeAndMakeDraggable(wfName, true);
								
							}
							ajaxCall(params, responseHandler, null);

						});

		
		editor
				.addAction(
						'newWorkflow',
						function(editor, cell) {

							var xmlTextOfEmptyGraph = '<mxGraphModel> <root> <mxCell id="0"/> <mxCell id="1" parent="0"/> </root></mxGraphModel>';
							var model = new mxGraphModel();
							var req = mxUtils.parseXml(xmlTextOfEmptyGraph);
							var root = req.documentElement;
							var dec = new mxCodec(root);
							dec.decode(root, graphG.getModel());

							currentWorkflowName = "Untitled Workflow";

						});

		editor.addAction('save', function(editor, cell) {
			saveAndRun(false);

		});

		editor.addAction('run', function(editor, cell) {
			console.log("run the workflow");
			saveAndRun(true);
		});

		editor.addAction('oneLevelUp', function(editor, cell) {
			oneLevelUp();
		});

	
		

		

		// adding toolbar buttons:

		// code added by aravind: To add space between the toolbar items. 
		toolbar.appendChild(spacer.cloneNode(true));
		toolbar.appendChild(spacer.cloneNode(true));
		toolbar.appendChild(spacer.cloneNode(true));

		/*
		
		toolbar.appendChild(spacer.cloneNode(true));
		toolbar.appendChild(spacer.cloneNode(true));
		toolbar.appendChild(spacer.cloneNode(true));
		toolbar.appendChild(spacer.cloneNode(true));
		toolbar.appendChild(spacer.cloneNode(true));
		toolbar.appendChild(spacer.cloneNode(true));
		
		 */

		addToolbarButton(editor, toolbar, 'newWorkflow', '', 'New',
				'images/new.png', false);
		toolbar.appendChild(spacerS.cloneNode(true));

		addToolbarButton(editor, toolbar, 'save', '', 'Save',
				'images/save.png', false);
		toolbar.appendChild(spacer.cloneNode(true));

		addToolbarButton(editor, toolbar, 'delete', '', 'Delete',
				'images/delete2.png', false);
		toolbar.appendChild(spacerS.cloneNode(true));

		addToolbarButton(editor, toolbar, 'cut', '', 'Cut', 'images/cut.png',
				false);
		toolbar.appendChild(spacerS.cloneNode(true));

		addToolbarButton(editor, toolbar, 'copy', '', 'Copy',
				'images/copy.png', false);
		toolbar.appendChild(spacerS.cloneNode(true));

		addToolbarButton(editor, toolbar, 'paste', '', 'Paste',
				'images/paste.png', false);
		toolbar.appendChild(spacer.cloneNode(true));

		addToolbarButton(editor, toolbar, 'undo', '', 'Undo',
				'images/undo.png', false);
		toolbar.appendChild(spacerS.cloneNode(true));

		addToolbarButton(editor, toolbar, 'redo', '', 'Redu',
				'images/redo.png', false);
		toolbar.appendChild(spacerS.cloneNode(true));

		addToolbarButton(editor, toolbar, 'oneLevelUp', '', 'One Level Up',
				'images/OneLevelUp.png', false);
		toolbar.appendChild(spacer.cloneNode(true));

		addToolbarButton(editor, toolbar, 'run', ' Run', null,
				'images/Run.png', false);
		toolbar.appendChild(spacer.cloneNode(true));
		/*
		addToolbarButton(editor, toolbar, 'share', 'Share', null,
				'images/sharing.jpg', false);
		toolbar.appendChild(spacer.cloneNode(true));
		*/
		
		addToolbarButton(editor, status, 'zoomIn', '', null,
				'images/zoom_in.png', true);

		addToolbarButton(editor, status, 'zoomOut', '', null,
				'images/zoom_out.png', true);
		addToolbarButton(editor, status, 'actualSize', '', null,
				'images/view_1_1.png', true);
		addToolbarButton(editor, status, 'fit', '', null,
				'images/fit_to_size.png', true);
		status.appendChild(spacer.cloneNode(true));

		/*
		// adding checkbox for saving intermediate results:
		var saveIntermResultsText = document.createElement('span');
		saveIntermResultsText.setAttribute('style',
				'display:inline; font-size:11pt;color:black;');
		saveIntermResultsText.innerHTML = "Save intermediate results:";
		status.appendChild(saveIntermResultsText);

		var saveIntermediateCheckBox = document.createElement('span');
		saveIntermediateCheckBox.setAttribute('style',
				'display:inline; vertical-align:middle');
		saveIntermediateCheckBox.innerHTML = '<input type="checkbox" id="saveIntermediate"/>';

		status.appendChild(saveIntermediateCheckBox);
		status.appendChild(spacer.cloneNode(true));



		var runIDText = document.createElement('span');
		runIDText.setAttribute('style',
				'display:inline; font-size:11pt;color:black;');
		runIDText.innerHTML = "Run ID: ";
		status.appendChild(runIDText);

		var runIDInputBox = document.createElement('span');
		runIDInputBox.setAttribute('style',
				'display:inline; vertical-align:middle');
		runIDInputBox.innerHTML = '<input type="text" id="runIDInput" style="width:100px; height:25px;"/>';

		status.appendChild(runIDInputBox);
		status.appendChild(spacerS.cloneNode(true));
		*/
		
		//addToolbarButton(editor, toolbar, 'export', '', 'Export', 'images/export1.png');
		//toolbar.appendChild(spacerS.cloneNode(true));

		//addToolbarButton(editor, toolbar, 'testByAndrey', 'Test', 'Test', 'images/export1.png');

		// Creates the outline (navigator, overview) for moving
		// around the graph in the top, right corner of the window.
		var outln = new mxOutline(graph, outline);

		// Fades-out the splash screen after the UI has been loaded.
		var splash = document.getElementById('splash');

		if (splash != null) {
			try {
				mxEvent.release(splash);
				mxEffects.fadeOut(splash, 100, true);
			} catch (e) {
				// mxUtils is not available (library not loaded)
				splash.parentNode.removeChild(splash);
			}
		}
	}
	// registers data products and workflows with the engine (hard-coded for testing purposes) as well as initial configuration
	//of engine, such as db name, access credentials, schema urls etc. from config file. Once finished, it calls refreshSidebar function:
	//initializeDataProductsAndWorkflows(graph, sidebar);
	
	var params = "action=initializeUserFolder";

	function responseHandler(argArray, someObject) {
		createTreeInTheSidebar();
		generateUniqueRunID();
	}
	ajaxCall(params, responseHandler, null);


	
	/*
	var myobject = {
		ValueA : 'Text A',
		ValueB : 'Text B',
		ValueC : 'Text C'
	};

	var select = document.getElementById("selOUserId");
	for (index in myobject) {
		select.options[select.options.length] = new Option(myobject[index],
				index);
	}
	*/

};
function captureOutputs() {
	editorG.execute("captureOutputs");
}

function saveAndRun(runAfterSaving) {
	
	if (runAfterSaving)
		clearAllStubsContents();
	var wfName;
	var firstTimePressed = false;
	if (currentWorkflowName.indexOf("Untitled Workflow") != -1) {
		firstTimePressed = true;
		wfName = prompt("Please enter workflow name", currentWorkflowName);
		currentWorkflowName = wfName; 
	} else
		wfName = currentWorkflowName;

	if (wfName == null)
		return;
	var enc = new mxCodec(mxUtils.createXmlDocument());
	var node = enc.encode(editorG.graph.getModel());
	var diagramXML = mxUtils.getPrettyXml(node);
	diagramXML = replaceAll(diagramXML, '&', '^');
	//console.log(diagramXML);
	if (window.XMLHttpRequest) {
		xmlhttp = new XMLHttpRequest();
	} else {
		xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
	}
	var url = "./Mediator?";
	var actionName = "";
	if (firstTimePressed)
		actionName = "saveAs";
	else
		actionName = "overwriteAndSave";

	var params = "action=" + actionName + "&name=" + wfName + "&diagram="
			+ diagramXML;

	function responseHandler(argArray, responseFromServer) {
	
		addWFToSidebarTreeAndMakeDraggable(wfName,runAfterSaving);	
		
	}
	ajaxCall(params, responseHandler, null);
	
	
}

function executeAction(divObject) {
	var divObjectStr = divObject.parentNode.innerHTML;
	var actionRealName = divObjectStr.substring(divObjectStr
			.indexOf("actionname=") + 12,
			divObjectStr.indexOf("style=\"marg") - 2);
	editorG.execute(actionRealName);
}

function createRunningWorkflowSplash() {
	var runningWorkflowSplashBackground = document.createElement('div');

	var splashStyle = "position: absolute; top: 0px; left: 0px; width: 100%; "
			+ "height: 100%; background: white; z-index: 1;";
	runningWorkflowSplashBackground.setAttribute('style', splashStyle);
	runningWorkflowSplashBackground.setAttribute('id',
			'wfRunningSplashBackground');

	document.body.appendChild(runningWorkflowSplashBackground);

	mxUtils.setOpacity(runningWorkflowSplashBackground, 80);

	var runningWorkflowSplashMsg = document.createElement('div');
	var splashMsgStyle = "position: absolute; top: 0px; left: 0px; width: 100%; "
			+ "height: 100%; z-index: 1;";

	runningWorkflowSplashMsg.setAttribute('style', splashMsgStyle);
	runningWorkflowSplashMsg.setAttribute('id', 'wfRunningSplashMsg');
	runningWorkflowSplashMsg.innerHTML = '<center style="padding-top: 230px;">'
			+ '<p style="font-size:12pt;background:white; width:200px;">running the workflow ... </p>'
			+ '<img src="images/runningWorkflow.gif">' + '</center>';

	document.body.appendChild(runningWorkflowSplashMsg);
}

function removeRunningWorkflowSplash() {
	var splashBackground = document.getElementById("wfRunningSplashBackground");
	var splashMsg = document.getElementById("wfRunningSplashMsg");
	document.body.removeChild(splashBackground);
	document.body.removeChild(splashMsg);

}