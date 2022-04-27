<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Profile</title>
<link href="./Style/profile.css" rel="Stylesheet" type="text/css"/>
<script src="./viewJS/loginUpdate.js" defer type="text/javascript" ></script>
<script src="./viewJS/profile.js" defer type="text/javascript" ></script>
<script src="https://cdn.jsdelivr.net/npm/web3@latest/dist/web3.min.js"></script>
<script src="https://unpkg.com/web3@latest/dist/web3.min.js"></script>
<link href="https://unpkg.com/tailwindcss@^1.0/dist/tailwind.min.css" rel="stylesheet">
</head>
<body>

<%
		String userId = request.getAttribute("userId").toString();
		
	%>
	

<div class="menuContainer">
   <div class="dropdown">
				<div class = "icon">
					<img src="./Style/images/accountImage.png" id="userLogo" onError="this.onerror=null;this.src='./Style/images/metamaskFox.png';">
				</div>
				<div class = "dropdown-content">
					<a href="#" id="profile" class="profile">Profile</a>
					<p id="webench" class="webench">Webench</p>
					<a href="login.jsp" id="logout" class="logout">Logout</a>
				</div>
	</div>
</div>
<div class="py-4">
    <div class="w-full border-t border-gray-300"></div>
</div>

<div class="content text-center text-orange-900 text-3xl">
            <div class="statusBar" >
                <p id="showAccount" style="visibility: hidden"></p>
                <p id="showBalance" style="visibility: hidden"></p>
            </div>
</div>

 <form method="post" action="profileImageUpload" enctype="multipart/form-data">
	<input type="hidden" name="userId" id="userId" value="<%=userId%>" />
    <input type="file" name="image" />
    <input type="submit" value="Upload" />
  </form>
  
</body>
</html>