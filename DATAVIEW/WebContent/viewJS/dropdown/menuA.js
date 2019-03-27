// Drop Bown Menu - Body Script
// copyright Stephen Chapman, 4th March 2005
// you may copy this calculator provided that you retain the copyright notice

var mapLink = '#';
var mapName = 'Site Map';

// do not change anything below this line
if (document.getElementById) {
	document.getElementById("menuContainer").innerHTML = '<div id="mB">\r\n';
	bar.writeBar();
	var initHTML = document.getElementById("menuContainer").innerHTML;
	document.getElementById("menuContainer").innerHTML = initHTML + '\r\n<\/div>\r\n\r\n';
	bar.writeDrop();
	//alert("true!");
} else
	document.writeln('<div id="mB"><a class="mO" href="' + mapLink + '">'
			+ mapName + '<\/a><\/div>');
//alert("document.all[0].innerHTML: " + document.all[0].innerHTML);