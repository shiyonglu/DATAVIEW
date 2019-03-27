<!-- 
 * The following functions are used to set up the user access for webench.
 * @author  Aravind Mohan
-->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
  pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head>
<%
  String labelMessage = (String)request.getAttribute("statusMsg");
  String labelUserExist = (String)request.getAttribute("userExist");
%>
<meta http-equiv="Content-Type" content="text/html; charset=windows-1252">
    <title>DATAVIEW</title><meta content="Big Data Research Laboratory">
<meta  content="New Page">
<meta  content="Big Data Research Laboratory">
<meta  content="http://bigdataworkflow.weebly.com/uploads/5/1/2/7/51275705/2706906.png">
<meta  content="http://bigdataworkflow.weebly.com/new-page.html">

<!--    
<link rel="stylesheet" type="text/css" href="http://bigdataworkflow.weebly.com/cdn2.editmysite.com/css/sites.css?buildTime=1449797482" />
--> 
<!--
<link rel="stylesheet" type="text/css" href="http://bigdataworkflow.weebly.com/cdn1.editmysite.com/editor/libraries/fancybox/fancybox.css?1449797482" />
-->
<link rel="stylesheet" type="text/css" href="./Web_Files/main_style.css" title="wsite-theme-css">
<link href="./Web_Files/css" rel="stylesheet" type="text/css">

<style type="text/css">
.wsite-elements.wsite-not-footer div.paragraph, .wsite-elements.wsite-not-footer p, .wsite-elements.wsite-not-footer .product-block .product-title, .wsite-elements.wsite-not-footer .product-description, .wsite-elements.wsite-not-footer .wsite-form-field label, .wsite-elements.wsite-not-footer .wsite-form-field label, #wsite-content div.paragraph, #wsite-content p, #wsite-content .product-block .product-title, #wsite-content .product-description, #wsite-content .wsite-form-field label, #wsite-content .wsite-form-field label, .blog-sidebar div.paragraph, .blog-sidebar p, .blog-sidebar .wsite-form-field label, .blog-sidebar .wsite-form-field label {}
#wsite-content div.paragraph, #wsite-content p, #wsite-content .product-block .product-title, #wsite-content .product-description, #wsite-content .wsite-form-field label, #wsite-content .wsite-form-field label, .blog-sidebar div.paragraph, .blog-sidebar p, .blog-sidebar .wsite-form-field label, .blog-sidebar .wsite-form-field label {}
.wsite-elements.wsite-footer div.paragraph, .wsite-elements.wsite-footer p, .wsite-elements.wsite-footer .product-block .product-title, .wsite-elements.wsite-footer .product-description, .wsite-elements.wsite-footer .wsite-form-field label, .wsite-elements.wsite-footer .wsite-form-field label{}
.wsite-elements.wsite-not-footer h2, .wsite-elements.wsite-not-footer .product-long .product-title, .wsite-elements.wsite-not-footer .product-large .product-title, .wsite-elements.wsite-not-footer .product-small .product-title, #wsite-content h2, #wsite-content .product-long .product-title, #wsite-content .product-large .product-title, #wsite-content .product-small .product-title, .blog-sidebar h2 {}
#wsite-content h2, #wsite-content .product-long .product-title, #wsite-content .product-large .product-title, #wsite-content .product-small .product-title, .blog-sidebar h2 {}
.wsite-elements.wsite-footer h2, .wsite-elements.wsite-footer .product-long .product-title, .wsite-elements.wsite-footer .product-large .product-title, .wsite-elements.wsite-footer .product-small .product-title{}
#wsite-title {}
.wsite-menu-default a {}
.wsite-menu a {}
.wsite-image div, .wsite-caption {}
.galleryCaptionInnerText {}
.fancybox-title {}
.wslide-caption-text {}
.wsite-phone {}
.wsite-headline {}
.wsite-headline-paragraph {}
.wsite-button-inner {}
.wsite-not-footer blockquote {}
.wsite-footer blockquote {}
.blog-header h2 a {}
#wsite-content h2.wsite-product-title {}
.wsite-product .wsite-product-price a {}
</style>

    <script src="./Web_Files/quant.js" async="" type="text/javascript"></script><script type="text/javascript" async="" src="./Web_Files/ga.js"></script><script><!--
var STATIC_BASE = '//cdn1.editmysite.com/';
var STYLE_PREFIX = 'wsite';
//-->
</script><style type="text/css"></style>
<script src='https://ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js'></script>
<link href="http://code.jquery.com/ui/1.10.4/themes/ui-lightness/jquery-ui.css" rel="stylesheet">
<script src="http://code.jquery.com/jquery-1.10.2.js"></script>
<script src="http://code.jquery.com/ui/1.10.4/jquery-ui.js"></script>


<script>
function downloadFun() {
    alert("To download DATAVIEW, please sign in first. If you do not have an account for DATAVIEW, please sign up for free."); } 
</script>

<script src="./Web_Files/main.js"></script><script type="text/javascript">_W.configDomain = "www.weebly.com";</script><script type="text/javascript" src="./Web_Files/ftl.js"></script><script>_W.relinquish && _W.relinquish()</script>
<script>
        $(function() {
            $( "#dialog-1" ).dialog({
               autoOpen: false,
               height: 700,
               width: 500,
               title: "Sign up for DATAVIEW"
            });
            $( "#opener" ).click(function() {
               $( "#dialog-1" ).dialog( "open" );
            });
         });


   function showDialog(){
        $("#dialog-1").dialog("open");
}
</script>
<script type="text/javascript"><!--
  
  
  (function(jQuery){
    function initFlyouts(){
      initPublishedFlyoutMenus(
        [{"id":"683068889534902185","title":"Home","url":"index.html","target":""},{"id":"889730839970818043","title":"Projects","url":"projects.html","target":""},{"id":"437858188312795895","title":"Publications","url":"http:\/\/www.cs.wayne.edu\/~shiyong\/pubs.htm","target":"_blank"},{"id":"469551346302134232","title":"Alumni","url":"alumni.html","target":""},{"id":"389786541452826855","title":"Wiki","url":"https:\/\/sites.google.com\/site\/swfwikicopy\/","target":"_blank"},{"id":"161242430389340639","title":"Contact","url":"contact.html","target":""},{"id":"995752295821370213","title":"New Page","url":"new-page.html","target":""}],
        "995752295821370213",
        '',
        'active',
        false,
        {"navigation\/item":"<li {{#id}}id=\"{{id}}\"{{\/id}}\n\tclass=\"wsite-menu-item-wrap\"\n\t>\n\t<a href=\"{{url}}\"\n\t\t{{#target}}target=\"{{target}}\"{{\/target}}\n\t\tclass=\"wsite-menu-item\"\n\t\t{{#membership_required}}\n\t\t\tdata-membership-required=\"{{.}}\"\n\t\t{{\/membership_required}}\n\t\t>\n\t\t{{{title_html}}}\n\t<\/a>\n\t{{#has_children}}{{> navigation\/flyout\/list}}{{\/has_children}}\n<\/li>\n","navigation\/flyout\/list":"<div class=\"wsite-menu-wrap\" style=\"display:none\">\n\t<ul class=\"wsite-menu\">\n\t\t{{#children}}{{> navigation\/flyout\/item}}{{\/children}}\n\t<\/ul>\n<\/div>\n","navigation\/flyout\/item":"<li {{#id}}id=\"{{id}}\"{{\/id}}\n\tclass=\"wsite-menu-subitem-wrap {{#is_current}}wsite-nav-current{{\/is_current}}\"\n\t>\n\t<a href=\"{{url}}\"\n\t\t{{#target}}target=\"{{target}}\"{{\/target}}\n\t\tclass=\"wsite-menu-subitem\"\n\t\t>\n\t\t<span class=\"wsite-menu-title\">\n\t\t\t{{{title_html}}}\n\t\t<\/span>{{#has_children}}<span class=\"wsite-menu-arrow\">&gt;<\/span>{{\/has_children}}\n\t<\/a>\n\t{{#has_children}}{{> navigation\/flyout\/list}}{{\/has_children}}\n<\/li>\n"},
        {}
      )
    }
    if (jQuery) {
      jQuery(document).ready(function() { jQuery(initFlyouts); });
    }else{
      if (Prototype.Browser.IE) window.onload = initFlyouts;
      else document.observe('dom:loaded', initFlyouts);
    }
  })(window._W && _W.jQuery)
//-->
</script>
<script type="text/javascript">
function validateForm()
{
var varEmail = document.forms["frmMain"]["txtEmailId"].value;
var varPass = document.forms["frmMain"]["txtPasswd"].value;

if (varEmail==null ||varEmail==""){
  alert("Please enter the email address to login...");
  return false;
}
else if (varPass==null ||varPass==""){
  alert("Please enter the password to login...");
  return false;
}
else if (varEmail == "dataview" && varPass == "dataview1234"){
	  window.location="changeDBSetting.jsp";
	  return false;
}

}
function validateSignupForm(){
	var varPass = document.forms["signupForm"]["input-password"].value;
	var varRePass = document.forms["signupForm"]["input-repeat-password"].value;
	if (varPass!=varRePass){
		alert("Password does not match...");
		return false;
	}
}
function noenter() {
  return !(window.event && window.event.keyCode == 13); }

</script>

  <link href="./Web_Files/sites.css" rel="stylesheet"></head>
  <body class="wsite-theme-light no-header-page  wsite-page-new-page">
  <form id="frmMain" method="post" action="UserLogin" onsubmit="return validateForm()">
  <div id="header-wrap">
  <div id="page">
    <div id="header-container">
      <table id="header">
        <tbody><tr>
          <td id="logo"><span class="wsite-logo">

  <a href="http://www.cs.wayne.edu/">
    <img src="./Web_Files/dataviewlogo.png">
  </a>

</span></td>
          <td id="header-right">
            <table>
              <tbody><tr>
                <td class="phone-number"><span class="wsite-text wsite-phone">
  <span>&nbsp;</span>
  <input type="button" style="background-color:#F4F5F7;color:#8F8F8F" id="btnSubmit1" value="Download DATAVIEW" onclick="return downloadFun();">
</span></td>
                <td class="social"></td>
              </tr>
            </tbody></table>
            <div class="search"></div>
          </td>
        </tr>
      </tbody></table>
      <div id="topnav">
        <ul class="wsite-menu-default">
            <li id="active" class="wsite-menu-item-wrap   wsite-nav-1" style="position: relative;">
              <a href="http://dataview.org/" class="wsite-menu-item" style="position: relative;">
                Home
              </a>
              
            </li>
            <li id="pg889730839970818043" class="wsite-menu-item-wrap   wsite-nav-2" style="position: relative;">
              <a href="http://bigdataworkflow.weebly.com/projects.html" class="wsite-menu-item" style="position: relative;">
                Projects
              </a>
              
            </li>
            <li id="pg437858188312795895" class="wsite-menu-item-wrap   wsite-nav-3" style="position: relative;">
              <a href="http://www.cs.wayne.edu/~shiyong/pubs.htm" target="_blank" class="wsite-menu-item" style="position: relative;">
                Publications
              </a>
              
            </li>
            <li id="pg469551346302134232" class="wsite-menu-item-wrap   wsite-nav-4" style="position: relative;">
              <a href="http://bigdataworkflow.weebly.com/alumni.html" class="wsite-menu-item" style="position: relative;">
                Alumni
              </a>
              
            </li>
            <li id="pg389786541452826855" class="wsite-menu-item-wrap   wsite-nav-5" style="position: relative;">
              <a href="https://sites.google.com/site/swfwikicopy/" target="_blank" class="wsite-menu-item" style="position: relative;">
                Wiki
              </a>
              
            </li>
            <li id="pg161242430389340639" class="wsite-menu-item-wrap   wsite-nav-6" style="position: relative;">
              <a href="http://bigdataworkflow.weebly.com/contact.html" class="wsite-menu-item" style="position: relative;">
                Contact
              </a>
              
            </li>
        </ul>
        <div style="clear:both"></div>
      </div>
    </div>
  </div>
</div>
<div id="banner-wrap">
  <div id="container">
    <div id="banner-bot">
      <div id="banner-top">
        <div id="banner-mid">
          <div id="banner">
            <div class="wsite-header"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
<div id="main-wrap">
  <div id="page">
    <div id="main">
      <div id="content"><div id="wsite-content" class="wsite-elements wsite-not-footer">
<div><div class="wsite-multicol"><div class="wsite-multicol-table-wrap" style="margin:0 -15px;">
  <table class="wsite-multicol-table">
    <tbody class="wsite-multicol-tbody">
      <tr class="wsite-multicol-tr">
        <td class="wsite-multicol-col" style="width:50%; padding:0 15px;">
          
            

<h2 class="wsite-content-title" style="text-align:left;">DATAVIEW - from data, to insight, to value<br><br><font color="#a1a1a1" size="3">"Deposit, access, analyze, visualize, and share your data ... All in the Cloud."</font><br><br><br></h2>


          
        </td>       <td class="wsite-multicol-col" style="width:50%; padding:0 15px;">
          
            

<div><div id="533165323902803737" align="left" style="width: 100%; overflow-y: hidden;" class="wcustomhtml"><table cellpadding="2px" cellspacing="1px" bgcolor="#F4F5F7" width="400px" class="tableBorder" align="right">
      <tbody><tr>
                               <!--
        <td colspan="2" bgcolor="#0066FF">&nbsp;</td>
                               --> 
        <td colspan="2" bgcolor="#F4F5F7">&nbsp;</td>
      </tr>
      
      
      <tr>
        <td class="label" align="left" width="40%">Email:</td>
        <td align="left" width="60%"><input type="text" id="txtEmailId" name="txtEmailId" maxlength="40" onkeypress="return noenter()"></td>
      </tr>
      <tr>
        <td class="label" align="left">Password:</td>
        <td align="left"><input type="password" id="txtPasswd" name="txtPasswd" maxlength="40" onkeypress="return noenter()"></td>
      </tr>
      <tr>
                                 <td></td><td></td>
      </tr>

                        <tr>
        <td></td><td align="left"><input type="submit" style="background-color:#0066FF;color:#FFFFFF" id="btnSubmit2" value="      Sign in     " onclick="return validateForm();"></td>
      </tr>
     
<tr>
 <th align="left" colspan="2"><font size="1">Don't have an account? <a href="#" onclick = "showDialog()"; return false;> Sign up for free </a></font></th>
</tr>
<tr>
 <th align="left" colspan="2"><font size="1"><a href="forgotPassword.jsp">Forgot password?</a></font></th>
</tr>
<%if(null !=labelMessage && labelMessage.length()>0){ %>
      <tr>
        <td align="center" class="label"><%=labelMessage %>
        </td>
      </tr>
      <%} %>
    </tbody></table>

</div>


</div>


          
        </td>     </tr>
    </tbody>
  </table>
</div></div></div></div>
</div>
    </div>
  </div>
</div>
<div id="footer-wrap">
</div>
<div id="footer-wrap">
  <div id="page">
    <div id="footer">

<link rel="stylesheet" type="text/css" href="./Web_Files/css(1)">

<style>
  /* @license
   * MyFonts Webfont Build ID 2520135, 2013-04-02T23:23:33-0400
   *
   * The fonts listed in this notice are subject to the End User License
   * Agreement(s) entered into by the website owner. All other parties are
   * explicitly restricted from using the Licensed Webfonts(s).
   *
   * You may obtain a valid license at the URLs below.
   *
   * Webfont: Proxima Nova S Semibold by Mark Simonson
   * URL: http://www.myfonts.com/fonts/marksimonson/proxima-nova/s-semibold/
   *
   * Webfont: Proxima Nova A Light by Mark Simonson
   * URL: http://www.myfonts.com/fonts/marksimonson/proxima-nova/a-light/
   *
   * Webfont: Proxima Nova A Semibold by Mark Simonson
   * URL: http://www.myfonts.com/fonts/marksimonson/proxima-nova/a-semibold/
   *
   * Webfont: Proxima Nova S Light by Mark Simonson
   * URL: http://www.myfonts.com/fonts/marksimonson/proxima-nova/s-light/
   *
   * Webfont: Proxima Nova Light by Mark Simonson
   * URL: http://www.myfonts.com/fonts/marksimonson/proxima-nova/light/
   *
   * Webfont: Proxima Nova Semibold by Mark Simonson
   * URL: http://www.myfonts.com/fonts/marksimonson/proxima-nova/semibold/
   *
   *
   * License: http://www.myfonts.com/viewlicense?type=web&buildid=2520135
   * Webfonts copyright: Copyright (c) Mark Simonson, 2005. All rights reserved.
   *
   * © 2013 MyFonts Inc
  */

  @font-face {
    font-family: 'ProximaNova';
    src: url('//cdn2.editmysite.com/fonts/Proxima-Light/267447_4_0.eot');
    src: url('//cdn2.editmysite.com/fonts/Proxima-Light/267447_4_0.eot?#iefix') format('embedded-opentype'),
         url('//cdn2.editmysite.com/fonts/Proxima-Light/267447_4_0.woff') format('woff'),
         url('//cdn2.editmysite.com/fonts/Proxima-Light/267447_4_0.ttf') format('truetype');
  }

  @font-face {
    font-family: 'ProximaNova';
    font-weight: bold;
    src: url('//cdn2.editmysite.com/fonts/Proxima-Semibold/267447_5_0.eot');
    src: url('//cdn2.editmysite.com/fonts/Proxima-Semibold/267447_5_0.eot?#iefix') format('embedded-opentype'),
         url('//cdn2.editmysite.com/fonts/Proxima-Semibold/267447_5_0.woff') format('woff'),
         url('//cdn2.editmysite.com/fonts/Proxima-Semibold/267447_5_0.ttf') format('truetype');
  }

  @font-face {
    font-family: 'ProximaNova-Semibold';
    src: url('//cdn2.editmysite.com/fonts/Proxima-Semibold/267447_5_0.eot');
    src: url('//cdn2.editmysite.com/fonts/Proxima-Semibold/267447_5_0.eot?#iefix') format('embedded-opentype'),
    url('//cdn2.editmysite.com/fonts/Proxima-Semibold/267447_5_0.woff') format('woff'),
    url('//cdn2.editmysite.com/fonts/Proxima-Semibold/267447_5_0.ttf') format('truetype');
  }

  @font-face {
    font-family: 'wicons';
    src: url(//cdn2.editmysite.com/fonts/wIcons/wicons.eot?buildTime=1449797482);
    src: url(//cdn2.editmysite.com/fonts/wIcons/wicons.eot?buildTime=1449797482#iefix) format('embedded-opentype'),
         url(//cdn2.editmysite.com/fonts/wIcons/wicons.woff?buildTime=1449797482) format('woff'),
         url(//cdn2.editmysite.com/fonts/wIcons/wicons.ttf?buildTime=1449797482) format('truetype'),
         url(//cdn2.editmysite.com/fonts/wIcons/wicons.svg?buildTime=1449797482#wicons) format('svg');
    font-weight: normal;
    font-style: normal;
  }
  /* Hack to smooth out font rendering on Chrome for Windows */
  @media screen and (-webkit-min-device-pixel-ratio: 0) {
    @font-face {
      font-family: 'wicons';
      src: url(//cdn2.editmysite.com/fonts/wIcons/wicons.svg?buildTime=1449797482#wicons) format('svg');
    }
  }

  @font-face {
    font-family: 'wsocial';
    src: url(//cdn2.editmysite.com/fonts/wSocial/wsocial.eot?buildTime=1449797482);
    src: url(//cdn2.editmysite.com/fonts/wSocial/wsocial.eot?buildTime=1449797482#iefix) format('embedded-opentype'),
         url(//cdn2.editmysite.com/fonts/wSocial/wsocial.woff?buildTime=1449797482) format('woff'),
         url(//cdn2.editmysite.com/fonts/wSocial/wsocial.ttf?buildTime=1449797482) format('truetype'),
         url(//cdn2.editmysite.com/fonts/wSocial/wsocial.svg?buildTime=1449797482#wsocial) format('svg');
    font-weight: normal;
    font-style: normal;
  }
  /* Hack to smooth out font rendering on Chrome for Windows */
  @media screen and (-webkit-min-device-pixel-ratio: 0) {
    @font-face {
      font-family: 'wsocial';
      src: url(//cdn2.editmysite.com/fonts/wSocial/wsocial.svg?buildTime=1449797482#wsocial) format('svg');
    }
  }
</style>

<script type="text/javascript" src="./Web_Files/footerSignup.js"></script>
<script type="text/javascript">
  var script;
  if (typeof Weebly == 'undefined') {
    Weebly = {};
  }
  if (!Weebly.jQuery) {
    script = document.createElement('script');
    script.onload = function() {
      Weebly.jQuery = jQuery.noConflict(true);
      Weebly.footer.setupContainer(Weebly.jQuery, 'cdn2.editmysite.com', '1449797482');
    };
    script.src = 'https://ajax.googleapis.com/ajax/libs/jquery/1.8.3/jquery.min.js';
    document.head.appendChild(script);
  } else {
    Weebly.footer.setupContainer(Weebly.jQuery, 'cdn2.editmysite.com', '1449797482');
  }
</script><div id="weebly-footer-signup-container" class="light" style="height: 58px; position: relative; left: -259.461px; top: 45.5937px; width: 1419px;">
  <div class="signup-container-header">
    <div class="start-free">Create a <a class="link" href="http://www.weebly.com/?utm_source=internal&utm_medium=footer&utm_campaign=3" target="_blank">free website</a></div>
    <div class="expand-icon"></div>
    <div class="powered-by"><span>Powered by</span> <a class="link weebly-icon" href="http://www.weebly.com/?utm_source=internal&utm_medium=footer&utm_campaign=3" target="_blank" rel="nofollow"></a></div>

    <div class="short-text">
      <span>Create your own free website</span><span class="go-icon"></span>
    </div>
  </div>
  <div class="signup-container-content">
    <h2 class="headline">Start your own free website</h2>
    <div class="description">A surprisingly easy drag &amp; drop site creator. <a class="thin-underline" href="http://www.weebly.com/?utm_source=internal&utm_medium=footer&utm_campaign=3" target="_blank" rel="nofollow">Learn more.</a></div>
    <iframe id="weebly-footer-signup-iframe" frameborder="0" src="./Web_Files/footer_signup.html" allowtransparency="true"></iframe>
  </div>
</div>
</div>
  </div>
</div>

    <script type="text/javascript">

  var _gaq = _gaq || [];
  _gaq.push(['_setAccount', 'UA-7870337-1']);
  _gaq.push(['_setDomainName', 'none']);
  _gaq.push(['_setAllowLinker', true]);
  _gaq.push(['_trackPageview']);

  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();

</script>

<!-- Quantcast Tag -->
<script type="text/javascript">
var _qevents = _qevents || [];

(function() {
var elem = document.createElement('script');
elem.src = (document.location.protocol == "https:" ? "https://secure" : "http://edge") + ".quantserve.com/quant.js";
elem.async = true;
elem.type = "text/javascript";
var scpt = document.getElementsByTagName('script')[0];
scpt.parentNode.insertBefore(elem, scpt);
})();

_qevents.push({
qacct:"p-0dYLvhSGGqUWo",
labels:"l0,u51275705.u51275705s632811976679423967"
});
</script>

<noscript>
&lt;div style="display:none;"&gt;
&lt;img src="//pixel.quantserve.com/pixel/p-0dYLvhSGGqUWo.gif?labels=l0,u51275705.u51275705s632811976679423967" border="0" height="1" width="1" alt="Quantcast"/&gt;
&lt;/div&gt;
</noscript>
<!-- End Quantcast tag -->


<script>

  (function(jQuery) {
    try {
      if (jQuery) {
        jQuery('div.blog-social div.fb-like').attr('class', 'blog-social-item blog-fb-like');
        var $commentFrame = jQuery('#commentArea iframe');
        if ($commentFrame.length > 0) {
          var frameHeight = jQuery($commentFrame[0].contentWindow.document).height() + 50;
          $commentFrame.css('min-height', frameHeight + 'px');
        }
        if (jQuery('.product-button').length > 0){
          jQuery(document).ready(function(){
            jQuery('.product-button').parent().each(function(index, product){
              if(jQuery(product).attr('target') == 'paypal'){
                if (!jQuery(product).find('> [name="bn"]').length){
                  jQuery('<input>').attr({
                    type: 'hidden',
                    name: 'bn',
                    value: 'DragAndDropBuil_SP_EC'
                  }).appendTo(product);
                }
              }
            });
          });
        }
      }
      else {
        // Prototype
        $$('div.blog-social div.fb-like').each(function(div) {
          div.className = 'blog-social-item blog-fb-like';
        });
        $$('#commentArea iframe').each(function(iframe) {
          iframe.style.minHeight = '410px';
        });
      }
    }
    catch(ex) {}
  })(window._W && _W.jQuery);

</script>
<div id="eu-cookie" class="notification" style="display: none;">
  <span id="eu-cookie-content"></span>
  <span id="eu-cookie-close">&#10005;</span>
</div>

<script type="text/javascript">
  (function($) {
    var cookie_location = '';

    // this cookie_content should already be pre-encoded from CookieSettingView.js
    var cookie_content = '' || _W.tl('By using this site you consent to the use of cookies. Cookies can be managed in your browser or device settings.');

    if (!_W.getCookie('632811976679423967_cookie_policy') || (_W.getCookie('632811976679423967_cookie_policy') !== cookie_content)){
      if (cookie_location === 'top' || cookie_location === 'bottom'){
        $('#eu-cookie-content').text(decodeURIComponent(cookie_content));
        (cookie_location === 'top') ? animateFromTop() : animateFromBottom();
        $('#eu-cookie').show();
      }
    }

    $('#eu-cookie-close').click(function(){
      _W.setCookie('632811976679423967_cookie_policy', cookie_content, 100000);
      $('#eu-cookie').hide();
    });

    function animateFromTop(){
      $('#eu-cookie').css({
        'top': '0px',
        'animation': 'reveal-top 1 2s'
      });
    }

    function animateFromBottom(){
      $('#eu-cookie').css({
        'bottom': '0px',
        'animation': 'reveal-bottom 1 2s'
      });
    }
  })(window._W && _W.jQuery);

</script>
<div id="wsite-menus"></div>
</form>
<div id="dialog-1" title="Sign up for DATAVIEW">
<form id="signupForm" accept-charset="utf-8" action="UserReg"
            class="simform" 
            method="post" onsubmit="return validateSignupForm()">
            <div class="wsite-form-container" style="margin-top: 10px;">
              <ul class="formlist">
                <h2 class="wsite-content-title" style="text-align: left;">Sign
                  up</h2>

                <div>
                  <div class="wsite-form-field" style="margin: 5px 0px 5px 0px;">
                    <label class="wsite-form-label" for="input-name">Name
                      <span class="form-required">*</span>
                    </label>
                    <div class="wsite-form-input-container">
                      <input
                        class="wsite-form-input wsite-input wsite-input-width-370px"
                        type="text" name="input-name" size = "35" id="input-name" />
                    </div>
                  </div>
                </div>

                <div>
                  <div class="wsite-form-field" style="margin: 5px 0px 5px 0px;">
                    <label class="wsite-form-label" for="input-email">Email
                      <span class="form-required">*</span>
                    </label>
                    <div class="wsite-form-input-container">
                      <input
                        class="wsite-form-input wsite-input wsite-input-width-570px"
                        name="input-email" id="input-email" type="email" size="35" required="" />
                    </div>
                  </div>
                </div>

                <div>
                  <div class="wsite-form-field" style="margin: 5px 0px 5px 0px;">
                    <label class="wsite-form-label" for="input-organization">Organization
                      <span class="form-required">*</span>
                    </label>
                    <div class="wsite-form-input-container">
                      <input
                        class="wsite-form-input wsite-input wsite-input-width-370px"
                        type="text" name="input-organization" size="35" id="input-organization" required="" />
                    </div>
                  </div>
                </div>
                
                <div>
                  <div class="wsite-form-field" style="margin: 5px 0px 5px 0px;">
                    <label class="wsite-form-label" for="input-title">Job title
                      <span class="form-required">*</span>
                    </label>
                    <div class="wsite-form-input-container">
                      <input
                        class="wsite-form-input wsite-input wsite-input-width-370px"
                        type="text" name="input-title" size="35" id="input-title" required="" />
                    </div>
                  </div>
                </div>

                <div>
                  <div class="wsite-form-field" style="margin: 5px 0px 0px 0px;">
                    <label class="wsite-form-label" for="input-522685071405717675">Country
                      <span class="form-required">*</span>
                    </label>
                    <div class="wsite-form-radio-container">
                      <select name='country' class='form-select'>
                        <option value="United States">United States</option>
                        <option value="Afganistan">Afghanistan</option>
                        <option value="Albania">Albania</option>
                        <option value="Algeria">Algeria</option>
                        <option value="American Samoa">American Samoa</option>
                        <option value="Andorra">Andorra</option>
                        <option value="Angola">Angola</option>
                        <option value="Anguilla">Anguilla</option>
                        <option value="Antigua &amp; Barbuda">Antigua &amp;
                          Barbuda</option>
                        <option value="Argentina">Argentina</option>
                        <option value="Armenia">Armenia</option>
                        <option value="Aruba">Aruba</option>
                        <option value="Australia">Australia</option>
                        <option value="Austria">Austria</option>
                        <option value="Azerbaijan">Azerbaijan</option>
                        <option value="Bahamas">Bahamas</option>
                        <option value="Bahrain">Bahrain</option>
                        <option value="Bangladesh">Bangladesh</option>
                        <option value="Barbados">Barbados</option>
                        <option value="Belarus">Belarus</option>
                        <option value="Belgium">Belgium</option>
                        <option value="Belize">Belize</option>
                        <option value="Benin">Benin</option>
                        <option value="Bermuda">Bermuda</option>
                        <option value="Bhutan">Bhutan</option>
                        <option value="Bolivia">Bolivia</option>
                        <option value="Bonaire">Bonaire</option>
                        <option value="Bosnia &amp; Herzegovina">Bosnia
                          &amp; Herzegovina</option>
                        <option value="Botswana">Botswana</option>
                        <option value="Brazil">Brazil</option>
                        <option value="British Indian Ocean Ter">British
                          Indian Ocean Ter</option>
                        <option value="Brunei">Brunei</option>
                        <option value="Bulgaria">Bulgaria</option>
                        <option value="Burkina Faso">Burkina Faso</option>
                        <option value="Burundi">Burundi</option>
                        <option value="Cambodia">Cambodia</option>
                        <option value="Cameroon">Cameroon</option>
                        <option value="Canada">Canada</option>
                        <option value="Canary Islands">Canary Islands</option>
                        <option value="Cape Verde">Cape Verde</option>
                        <option value="Cayman Islands">Cayman Islands</option>
                        <option value="Central African Republic">Central
                          African Republic</option>
                        <option value="Chad">Chad</option>
                        <option value="Channel Islands">Channel Islands</option>
                        <option value="Chile">Chile</option>
                        <option value="China">China</option>
                        <option value="Christmas Island">Christmas Island</option>
                        <option value="Cocos Island">Cocos Island</option>
                        <option value="Colombia">Colombia</option>
                        <option value="Comoros">Comoros</option>
                        <option value="Congo">Congo</option>
                        <option value="Cook Islands">Cook Islands</option>
                        <option value="Costa Rica">Costa Rica</option>
                        <option value="Cote DIvoire">Cote D'Ivoire</option>
                        <option value="Croatia">Croatia</option>
                        <option value="Cuba">Cuba</option>
                        <option value="Curaco">Curacao</option>
                        <option value="Cyprus">Cyprus</option>
                        <option value="Czech Republic">Czech Republic</option>
                        <option value="Denmark">Denmark</option>
                        <option value="Djibouti">Djibouti</option>
                        <option value="Dominica">Dominica</option>
                        <option value="Dominican Republic">Dominican
                          Republic</option>
                        <option value="East Timor">East Timor</option>
                        <option value="Ecuador">Ecuador</option>
                        <option value="Egypt">Egypt</option>
                        <option value="El Salvador">El Salvador</option>
                        <option value="Equatorial Guinea">Equatorial Guinea</option>
                        <option value="Eritrea">Eritrea</option>
                        <option value="Estonia">Estonia</option>
                        <option value="Ethiopia">Ethiopia</option>
                        <option value="Falkland Islands">Falkland Islands</option>
                        <option value="Faroe Islands">Faroe Islands</option>
                        <option value="Fiji">Fiji</option>
                        <option value="Finland">Finland</option>
                        <option value="France">France</option>
                        <option value="French Guiana">French Guiana</option>
                        <option value="French Polynesia">French Polynesia</option>
                        <option value="French Southern Ter">French Southern
                          Ter</option>
                        <option value="Gabon">Gabon</option>
                        <option value="Gambia">Gambia</option>
                        <option value="Georgia">Georgia</option>
                        <option value="Germany">Germany</option>
                        <option value="Ghana">Ghana</option>
                        <option value="Gibraltar">Gibraltar</option>
                        <option value="Great Britain">Great Britain</option>
                        <option value="Greece">Greece</option>
                        <option value="Greenland">Greenland</option>
                        <option value="Grenada">Grenada</option>
                        <option value="Guadeloupe">Guadeloupe</option>
                        <option value="Guam">Guam</option>
                        <option value="Guatemala">Guatemala</option>
                        <option value="Guinea">Guinea</option>
                        <option value="Guyana">Guyana</option>
                        <option value="Haiti">Haiti</option>
                        <option value="Hawaii">Hawaii</option>
                        <option value="Honduras">Honduras</option>
                        <option value="Hong Kong">Hong Kong</option>
                        <option value="Hungary">Hungary</option>
                        <option value="Iceland">Iceland</option>
                        <option value="India">India</option>
                        <option value="Indonesia">Indonesia</option>
                        <option value="Iran">Iran</option>
                        <option value="Iraq">Iraq</option>
                        <option value="Ireland">Ireland</option>
                        <option value="Isle of Man">Isle of Man</option>
                        <option value="Israel">Israel</option>
                        <option value="Italy">Italy</option>
                        <option value="Jamaica">Jamaica</option>
                        <option value="Japan">Japan</option>
                        <option value="Jordan">Jordan</option>
                        <option value="Kazakhstan">Kazakhstan</option>
                        <option value="Kenya">Kenya</option>
                        <option value="Kiribati">Kiribati</option>
                        <option value="Korea North">Korea North</option>
                        <option value="Korea Sout">Korea South</option>
                        <option value="Kuwait">Kuwait</option>
                        <option value="Kyrgyzstan">Kyrgyzstan</option>
                        <option value="Laos">Laos</option>
                        <option value="Latvia">Latvia</option>
                        <option value="Lebanon">Lebanon</option>
                        <option value="Lesotho">Lesotho</option>
                        <option value="Liberia">Liberia</option>
                        <option value="Libya">Libya</option>
                        <option value="Liechtenstein">Liechtenstein</option>
                        <option value="Lithuania">Lithuania</option>
                        <option value="Luxembourg">Luxembourg</option>
                        <option value="Macau">Macau</option>
                        <option value="Macedonia">Macedonia</option>
                        <option value="Madagascar">Madagascar</option>
                        <option value="Malaysia">Malaysia</option>
                        <option value="Malawi">Malawi</option>
                        <option value="Maldives">Maldives</option>
                        <option value="Mali">Mali</option>
                        <option value="Malta">Malta</option>
                        <option value="Marshall Islands">Marshall Islands</option>
                        <option value="Martinique">Martinique</option>
                        <option value="Mauritania">Mauritania</option>
                        <option value="Mauritius">Mauritius</option>
                        <option value="Mayotte">Mayotte</option>
                        <option value="Mexico">Mexico</option>
                        <option value="Midway Islands">Midway Islands</option>
                        <option value="Moldova">Moldova</option>
                        <option value="Monaco">Monaco</option>
                        <option value="Mongolia">Mongolia</option>
                        <option value="Montserrat">Montserrat</option>
                        <option value="Morocco">Morocco</option>
                        <option value="Mozambique">Mozambique</option>
                        <option value="Myanmar">Myanmar</option>
                        <option value="Nambia">Nambia</option>
                        <option value="Nauru">Nauru</option>
                        <option value="Nepal">Nepal</option>
                        <option value="Netherland Antilles">Netherland
                          Antilles</option>
                        <option value="Netherlands">Netherlands (Holland,
                          Europe)</option>
                        <option value="Nevis">Nevis</option>
                        <option value="New Caledonia">New Caledonia</option>
                        <option value="New Zealand">New Zealand</option>
                        <option value="Nicaragua">Nicaragua</option>
                        <option value="Niger">Niger</option>
                        <option value="Nigeria">Nigeria</option>
                        <option value="Niue">Niue</option>
                        <option value="Norfolk Island">Norfolk Island</option>
                        <option value="Norway">Norway</option>
                        <option value="Oman">Oman</option>
                        <option value="Pakistan">Pakistan</option>
                        <option value="Palau Island">Palau Island</option>
                        <option value="Palestine">Palestine</option>
                        <option value="Panama">Panama</option>
                        <option value="Papua New Guinea">Papua New Guinea</option>
                        <option value="Paraguay">Paraguay</option>
                        <option value="Peru">Peru</option>
                        <option value="Phillipines">Philippines</option>
                        <option value="Pitcairn Island">Pitcairn Island</option>
                        <option value="Poland">Poland</option>
                        <option value="Portugal">Portugal</option>
                        <option value="Puerto Rico">Puerto Rico</option>
                        <option value="Qatar">Qatar</option>
                        <option value="Republic of Montenegro">Republic of
                          Montenegro</option>
                        <option value="Republic of Serbia">Republic of
                          Serbia</option>
                        <option value="Reunion">Reunion</option>
                        <option value="Romania">Romania</option>
                        <option value="Russia">Russia</option>
                        <option value="Rwanda">Rwanda</option>
                        <option value="St Barthelemy">St Barthelemy</option>
                        <option value="St Eustatius">St Eustatius</option>
                        <option value="St Helena">St Helena</option>
                        <option value="St Kitts-Nevis">St Kitts-Nevis</option>
                        <option value="St Lucia">St Lucia</option>
                        <option value="St Maarten">St Maarten</option>
                        <option value="St Pierre &amp; Miquelon">St Pierre
                          &amp; Miquelon</option>
                        <option value="St Vincent &amp; Grenadines">St
                          Vincent &amp; Grenadines</option>
                        <option value="Saipan">Saipan</option>
                        <option value="Samoa">Samoa</option>
                        <option value="Samoa American">Samoa American</option>
                        <option value="San Marino">San Marino</option>
                        <option value="Sao Tome &amp; Principe">Sao Tome
                          &amp; Principe</option>
                        <option value="Saudi Arabia">Saudi Arabia</option>
                        <option value="Senegal">Senegal</option>
                        <option value="Serbia">Serbia</option>
                        <option value="Seychelles">Seychelles</option>
                        <option value="Sierra Leone">Sierra Leone</option>
                        <option value="Singapore">Singapore</option>
                        <option value="Slovakia">Slovakia</option>
                        <option value="Slovenia">Slovenia</option>
                        <option value="Solomon Islands">Solomon Islands</option>
                        <option value="Somalia">Somalia</option>
                        <option value="South Africa">South Africa</option>
                        <option value="Spain">Spain</option>
                        <option value="Sri Lanka">Sri Lanka</option>
                        <option value="Sudan">Sudan</option>
                        <option value="Suriname">Suriname</option>
                        <option value="Swaziland">Swaziland</option>
                        <option value="Sweden">Sweden</option>
                        <option value="Switzerland">Switzerland</option>
                        <option value="Syria">Syria</option>
                        <option value="Tahiti">Tahiti</option>
                        <option value="Taiwan">Taiwan</option>
                        <option value="Tajikistan">Tajikistan</option>
                        <option value="Tanzania">Tanzania</option>
                        <option value="Thailand">Thailand</option>
                        <option value="Togo">Togo</option>
                        <option value="Tokelau">Tokelau</option>
                        <option value="Tonga">Tonga</option>
                        <option value="Trinidad &amp; Tobago">Trinidad
                          &amp; Tobago</option>
                        <option value="Tunisia">Tunisia</option>
                        <option value="Turkey">Turkey</option>
                        <option value="Turkmenistan">Turkmenistan</option>
                        <option value="Turks &amp; Caicos Is">Turks &amp;
                          Caicos Is</option>
                        <option value="Tuvalu">Tuvalu</option>
                        <option value="Uganda">Uganda</option>
                        <option value="Ukraine">Ukraine</option>
                        <option value="United Arab Erimates">United Arab
                          Emirates</option>
                        <option value="United Kingdom">United Kingdom</option>
                        <option value="Uraguay">Uruguay</option>
                        <option value="Uzbekistan">Uzbekistan</option>
                        <option value="Vanuatu">Vanuatu</option>
                        <option value="Vatican City State">Vatican City
                          State</option>
                        <option value="Venezuela">Venezuela</option>
                        <option value="Vietnam">Vietnam</option>
                        <option value="Virgin Islands (Brit)">Virgin
                          Islands (Brit)</option>
                        <option value="Virgin Islands (USA)">Virgin Islands
                          (USA)</option>
                        <option value="Wake Island">Wake Island</option>
                        <option value="Wallis &amp; Futana Is">Wallis &amp;
                          Futana Is</option>
                        <option value="Yemen">Yemen</option>
                        <option value="Zaire">Zaire</option>
                        <option value="Zambia">Zambia</option>
                        <option value="Zimbabwe">Zimbabwe</option>
                      </select>

                    </div>
                  </div>
                </div>

                <div>
                  <div class="wsite-form-field" style="margin: 5px 0px 5px 0px;">
                    <label class="wsite-form-label" for="input-password">Password
                      <span class="form-required">*</span>
                    </label>
                    <div class="wsite-form-input-container">
                      <input
                        class="wsite-form-input wsite-input wsite-input-width-370px"
                        name="input-password" id="input-password" type="password" required="" />
                    </div>
                  </div>
                </div>
                
                <div>
                  <div class="wsite-form-field" style="margin: 5px 0px 5px 0px;">
                    <label class="wsite-form-label" for="input-repeat-password">Repeat password
                      <span class="form-required">*</span>
                    </label>
                    <div class="wsite-form-input-container">
                      <input
                        class="wsite-form-input wsite-input wsite-input-width-370px"
                        name="input-repeat-password" id="input-repeat-password" type="password" required="" />
                    </div>
                  </div>
                </div>
                
              </ul>
            </div>
            <div style="display: none; visibility: hidden;">
              <input type="text" name="wsite_subject" />
            </div>
            <br><button class='wsite-button' style="border:0px;" type="submit"><span class='wsite-button-inner'>Sign up</span></button>
            <button class='wsite-button' style="border:0px;" type="Cancel" onClick="$('#dialog-1').dialog( 'close' );return false"><span class='wsite-button-inner'>Cancel</span></button>
          </form>
</div>
</body></html>