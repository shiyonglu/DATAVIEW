// Drop Bown Menu - Head Script
// copyright Stephen Chapman, 4th March 2005, 5th February 2006
// you may copy this menu provided that you retain the copyright notice
var mapLink = '#';
var mapName = 'Site Map';
var fix = 1;
var delay = 200000;
var modd = 0;
var bar = new menuBar();

/*
var viewButtonHTML 
	= '<button type="button" style="margin-left: 0px; margin-top:2px; height:24px;'
		+ ' "> ' 
	+' <img src="./images/viewLogo/viewLogo.png" style="width:56px; margin-top:-2px; height:14px"/></button>';


*/

//background:#d7eaff;border-radius:5px;border-width: 1px; - in case if other color of button needs to be used.
//bar.addMenu(viewButtonHTML);
//bar.addMenu('<img src="./images/viewLogo/viewLogo.png" style="width:62px; height:15px"/>');


/*
bar.addItem('#', '<div onclick="newCompositeWorkflow()" actionName="newWorkflow" style="margin-left:10px;">New</div>');
bar.addItem("#", '<div onclick="executeAction(this)" actionName="save" style="margin-left:10px;">Save</div>');
bar.addItem("#", '<div onclick="executeAction(this)" actionName="saveAs" style="margin-left:10px;">Save As</div>');
bar.addItem("#", '<div onclick="viewSWL()" style="margin-left:10px;">View Workflow Specification</div>');
bar.addItem("#", '<div onclick="viewMxGraph()" style="margin-left:10px;">View mxGraph source</div>');
bar.addItem("#", '<div onclick="printPreview()" style="margin-left:10px;">Print Preview</div>');
bar.addItem("#", '<div onclick="print()" style="margin-left:10px;">Print... </div>');
bar.addItem("#", '<div onclick="help()" style="margin-left:10px;">Help and Tutorials</div>');
bar.addItem("#", '<div onclick="aboutVIEW()" style="margin-left:10px;">About VIEW</div>');
bar.addItem("#", '<div onclick="logout()" style="margin-left:10px;">Logout</div>');
*/


// bar.addMenu('Menu 2');
// bar.addItem('entry2a.htm', 'Entry 2a');
// bar.addItem('entry2b.htm', 'Entry 2b');

/*
var AddButtonHTML = '<button style="position:absolute; width:80px; height:24px; margin-top:1px; font-size:12px;"> '
+ '<span style="display:inline; vertical-align:middle;"> '
+ '<img src="images/plus.png"'
+' style="width:16px; height:16px;display:inline;vertical-align: middle; margin-right: 2px; "/>'
+ '</span><span style="display:inline; vertical-align:middle;height:5px;">&nbsp;Add...</span></button>';

bar.addMenu(AddButtonHTML);
*/


bar.addItem('#', '<div onclick="newDataProduct()" style="">New Data Product</div>');
bar.addItem('#', '<div onclick="newPrimitiveWorkflow()" style="">New Primitive Workflow</div>');

// do not change anything below this line
var blc = 'black';
var blh = 'orange';
var bla = 'orange';
var lc = 'orange';
var lh = 'orange';
var la = 'orange';
function menuBar() {
	this.jj = -1;
	this.kk = 0;
	this.mO = new Array();
	this.addMenu = addMenu;
	this.addItem = addItem;
	this.writeBar = writeBar;
	this.writeDrop = writeDrop;
}
function addMenu(main) {
	this.mO[++this.jj] = new Object();
	this.mO[this.jj].main = main;
	this.kk = 0;
	this.mO[this.jj].link = new Array();
	this.mO[this.jj].name = new Array();
}
function addItem(link, name) {
	this.mO[this.jj].link[this.kk] = link;
	this.mO[this.jj].name[this.kk++] = name;
}
function writeBar() {
	var spacingBetweenButtons = '<span >';
	for(var i=0; i<113; i++){
		spacingBetweenButtons = spacingBetweenButtons + '&nbsp;';
	}
	spacingBetweenButtons = spacingBetweenButtons + '</span>';
	spacingBetweenButtons = '';
	
	for ( var i = 1; i <= this.mO.length; i++) {
		var initialHTML = document.getElementById("menuContainer").innerHTML;
		initialHTML = initialHTML + '<span id="navMenu' + i + '" class="mH">'
				+ this.mO[i - 1].main + '<\/span>' + spacingBetweenButtons;
		document.getElementById("menuContainer").innerHTML = initialHTML;
	}
	
}
function writeDrop() {
	for ( var i = 1; i <= this.mO.length; i++) {
		var initialHTML = document.getElementById("menuContainer").innerHTML;
		
		initialHTML = initialHTML + '<div id="dropMenu' + i + '" class="mD">\r\n';
		for ( var h = 0; h < this.mO[i - 1].link.length; h++) {
			initialHTML =initialHTML 
			+ '<a class="mL" href="' + this.mO[i - 1].link[h]
					+ '">' + this.mO[i - 1].name[h] + '<\/a>\r\n';
		}
		document.getElementById("menuContainer").innerHTML = initialHTML + '<\/div>\r\n';
	}
}
if (fix)
	window.onscroll = sMenu;
window.onload = iMenu;
var onm = null;
var ponm = null;
var podm = null;
var ndm = bar.mO.length;
function posY() {
	return typeof window.pageYOffset != 'undefined' ? window.pageYOffset
			: document.documentElement.scrollTop ? document.documentElement.scrollTop
					: document.body.scrollTop ? document.body.scrollTop : 0;
}
function sMenu() {
	//document.getElementById('mB').style.top = posY() + 'px';
	for (i = 1; i <= ndm; i++) {
		menuName = 'dropMenu' + i;
		odm = document.getElementById(menuName);
		if (onm) {
			var yPos = onm.offsetParent.offsetTop
					+ onm.offsetParent.offsetHeight;
			//odm.style.top = yPos + 'px';
		}
	}
}
function iMenu() {
	if (document.getElementById) {
		document.onclick = mHide;
		for (i = 1; i <= ndm; i++) {
			menuName = 'dropMenu' + i;
			navName = 'navMenu' + i;
			odm = document.getElementById(menuName);
			onm = document.getElementById(navName);
			odm.style.visibility = 'hidden';
			// onm.onmouseover = mHov;
			onm.onclick = mHov;
			onm.onmouseout = mOut;
			// onm.onmouseout = mHide;
		}
		onm = null;
	}
	return;
}
function mHov(e) {
	if (modd)
		clearTimeout(modd);
	document.onclick = null;
	honm = document.getElementById(this.id);
	if (honm != onm) {
		honm.style.color = lh;
		//honm.style.backgroundColor = blh;
	}
	menuName = 'drop' + this.id.substring(3, this.id.length);
	odm = document.getElementById(menuName);
	if (podm == odm) {
		mHide();
		return;
	}
	if (podm != null)
		mHide();
	onm = document.getElementById(this.id);
	if ((ponm != onm) || (podm == null)) {
		onm.style.color = la;
		//onm.style.backgroundColor = bla;
	}
	if (odm) {
		xPos = onm.offsetParent.offsetLeft + onm.offsetLeft;
		yPos = onm.offsetParent.offsetTop + onm.offsetParent.offsetHeight;
		var shiftToTheRight = 0;
		//alert(menuName);
		if(menuName.indexOf("dropMenu1") != -1)
			shiftToTheRight = 10;
			
		if(menuName.indexOf("dropMenu2") != -1)
			shiftToTheRight = 565;
		shiftToTheRight = 5;
		odm.style.left = xPos + shiftToTheRight + 'px';
		//odm.style.top = yPos + 'px';
		odm.style.visibility = 'visible';
		// odm.onmouseover = omov;
		odm.onclick = omov;
		odm.onmouseout = omot;
		// odm.onmouseout = mHide;
		podm = odm;
		ponm = onm;
	}
}
function omov() {
	if (modd)
		clearTimeout(modd);
}
function omot() {
	modd = setTimeout('mHide()', delay);
}
function mOut(e) {
	modd = setTimeout('mHide()', delay);
	document.onclick = mHide;
	oonm = document.getElementById(this.id);
	if (oonm != onm) {
		oonm.style.color = lc;
		//oonm.style.backgroundColor = blc;
	}
}
function mHide() {
	document.onclick = null;
	if (podm) {
		podm.style.visibility = 'hidden';
		podm = null;
		ponm.style.color = lc;
		//ponm.style.backgroundColor = blc;
	}
	onm = null;
}
if (fix) {
	var ag = navigator.userAgent.toLowerCase();
	var isG = (ag.indexOf('gecko') != -1);
	var isR = 0;
	if (isG) {
		t = ag.split('rv:');
		isR = parseFloat(t[1]);
	}
	if (isR)
		setInterval('sMenu()', 50);
}
