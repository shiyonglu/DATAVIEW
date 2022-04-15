<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Connect Wallet</title>
<link href="https://unpkg.com/tailwindcss@^1.0/dist/tailwind.min.css" rel="stylesheet">
<script src="./viewJS/connectMetamask.js" defer type="text/javascript" ></script>
<script src="https://cdn.jsdelivr.net/npm/web3@latest/dist/web3.min.js"></script>
<script src="https://unpkg.com/web3@latest/dist/web3.min.js"></script>
</head> 
<body>
<div class="flex w-screen h-screen justify-center iterms-center">

<div class="flex flex-col space-y-6 justify-center content-center items-center space-x-4">
	<button id="connectWallet" class="rounded bg-orange-500 hover:bg-orange-700 py-2 px-4 text-white">Connect Wallet</button>
	<h1 id="getMessage" class="text-2xl">You need an Ethereum Wallet to use Dataview</h1>
	<button id="getWallet" class="rounded bg-orange-500 hover:bg-orange-700 py-2 px-4 text-white">Get Metamask</button>
</div>
</div>

</body>
</html>