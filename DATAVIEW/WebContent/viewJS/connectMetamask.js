var connectWallet = document.getElementById('connectWallet');
var getWallet = document.getElementById('getWallet');
var getMessage = document.getElementById('getMessage');

//this will help in hide or display 2 buttons
window.addEventListener('load', () => {
	if(!window.ethereum) {
		getWallet.style.display = "block";
		getMessage.style.display = "block";
		connectWallet.style.display = "none";
	} else {
		getWallet.style.display = "none";
		getMessage.style.display = "none";
		connectWallet.style.display = "block";
	}

})

connectWallet.addEventListener('click', async () => {
	//this check is redundant. This button will only show if it is ethereum
    if (window.ethereum) {
        window.web3 = new Web3(ethereum);
        try {
            await ethereum.enable();
            sessionStorage.setItem("connected", "true");
        } 
        catch (error) {
			sessionStorage.setItem("connected", "false");
        	console.error(error);
        }
    }
	
    location.href="login.jsp";
})



getWallet.addEventListener('click', () => {
	console.log("Inside click");
	window.open("https://metamask.io/download/");
});

//this script is loaded using defer in connectWallet.jsp so that all elements become available before adding listeners;