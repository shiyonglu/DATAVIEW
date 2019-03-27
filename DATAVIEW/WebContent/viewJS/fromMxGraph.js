/**
 * Built in function that came with mxGraph library Installation.  
 */

function addToolbarButton(editor, toolbar, action, label, hint, image, isTransparent) {
		var button = document.createElement('button');
		button.style.fontSize = '12';
		if (image != null) {
			var img = document.createElement('img');
			img.setAttribute('src', image);
			img.style.width = '16px';
			img.style.height = '16px';
			img.style.verticalAlign = 'middle';
			img.style.marginRight = '2px';
			button.appendChild(img);
		}
		
		if (isTransparent) {
			button.style.background = 'transparent';
			button.style.color = '#FFFFFF';
			button.style.border = 'none';
		}
		
		mxEvent.addListener(button, 'click', function(evt) {
			editor.execute(action);
		});
		mxUtils.write(button, label);
		button.title=hint;
		toolbar.appendChild(button);
	};


	function showModalWindow(graph, title, content, width, height) {
		var background = document.createElement('div');
		background.style.position = 'absolute';
		background.style.left = '0px';
		background.style.top = '0px';
		background.style.right = '0px';
		background.style.bottom = '0px';
		background.style.background = 'white';
		mxUtils.setOpacity(background, 50);
		document.body.appendChild(background);

		if (mxClient.IS_IE) {
			new mxDivResizer(background);
		}

		var x = Math.max(0, document.body.scrollWidth / 2 - width / 2);
		var y = Math
				.max(
						10,
						(document.body.scrollHeight || document.documentElement.scrollHeight)
								/ 2 - height * 2 / 3);
		var wnd = new mxWindow(title, content, x, y, width, height, false, true);
		wnd.setClosable(true);

		// Fades the background out after after the window has been closed
		wnd.addListener(mxEvent.DESTROY, function(evt) {
			graph.setEnabled(true);
			mxEffects.fadeOut(background, 50, true, 10, 30, true);
		});

		graph.setEnabled(false);
		graph.tooltipHandler.hide();
		wnd.setVisible(true);
	};
	
	function configureStylesheet(graph) {
		var style = new Object();
		style[mxConstants.STYLE_SHAPE] = mxConstants.SHAPE_RECTANGLE;
		style[mxConstants.STYLE_PERIMETER] = mxPerimeter.RectanglePerimeter;
		style[mxConstants.STYLE_ALIGN] = mxConstants.ALIGN_CENTER;
		style[mxConstants.STYLE_VERTICAL_ALIGN] = mxConstants.ALIGN_MIDDLE;
		style[mxConstants.STYLE_GRADIENTCOLOR] = '#41B9F5';
		style[mxConstants.STYLE_FILLCOLOR] = '#8CCDF5';
		style[mxConstants.STYLE_STROKECOLOR] = '#1B78C8';
		style[mxConstants.STYLE_FONTCOLOR] = '#000000';
		style[mxConstants.STYLE_ROUNDED] = false;
		style[mxConstants.STYLE_OPACITY] = '100';
		style[mxConstants.STYLE_FONTSIZE] = '11';
		style[mxConstants.STYLE_FONTSTYLE] = 0;
		style[mxConstants.STYLE_IMAGE_WIDTH] = '48';
		style[mxConstants.STYLE_IMAGE_HEIGHT] = '48';
		graph.getStylesheet().putDefaultVertexStyle(style);

		style = new Object();
		style[mxConstants.STYLE_SHAPE] = mxConstants.SHAPE_SWIMLANE;
		style[mxConstants.STYLE_PERIMETER] = mxPerimeter.RectanglePerimeter;
		style[mxConstants.STYLE_ALIGN] = mxConstants.ALIGN_CENTER;
		style[mxConstants.STYLE_VERTICAL_ALIGN] = mxConstants.ALIGN_TOP;
		style[mxConstants.STYLE_FILLCOLOR] = '#FF9103';
		style[mxConstants.STYLE_GRADIENTCOLOR] = '#F8C48B';
		style[mxConstants.STYLE_STROKECOLOR] = '#E86A00';
		style[mxConstants.STYLE_FONTCOLOR] = '#000000';
		style[mxConstants.STYLE_ROUNDED] = false;
		style[mxConstants.STYLE_OPACITY] = '80';
		style[mxConstants.STYLE_STARTSIZE] = '30';
		style[mxConstants.STYLE_FONTSIZE] = '16';
		style[mxConstants.STYLE_FONTSTYLE] = 1;
		graph.getStylesheet().putCellStyle('group', style);

		style = new Object();
		style[mxConstants.STYLE_SHAPE] = mxConstants.SHAPE_IMAGE;
		style[mxConstants.STYLE_FONTCOLOR] = '#774400';
		style[mxConstants.STYLE_PERIMETER] = mxPerimeter.RectanglePerimeter;
		style[mxConstants.STYLE_PERIMETER_SPACING] = '0';
		style[mxConstants.STYLE_ALIGN] = mxConstants.ALIGN_LEFT;
		style[mxConstants.STYLE_VERTICAL_ALIGN] = mxConstants.ALIGN_MIDDLE;
		style[mxConstants.STYLE_FONTSIZE] = '10';
		style[mxConstants.STYLE_FONTSTYLE] = 2;
		style[mxConstants.STYLE_IMAGE_WIDTH] = '16';
		style[mxConstants.STYLE_IMAGE_HEIGHT] = '16';
		graph.getStylesheet().putCellStyle('port', style);

		style = graph.getStylesheet().getDefaultEdgeStyle();
		style[mxConstants.STYLE_LABEL_BACKGROUNDCOLOR] = '#000000';
		style[mxConstants.STYLE_STROKECOLOR] = '#000000';
		style[mxConstants.STYLE_STROKEWIDTH] = '2';
		style[mxConstants.STYLE_ROUNDED] = false;
		//style[mxConstants.STYLE_EDGE] = mxEdgeStyle.EntityRelation;
		style[mxConstants.STYLE_EDGE] = mxEdgeStyle.ElbowConnector;
	};