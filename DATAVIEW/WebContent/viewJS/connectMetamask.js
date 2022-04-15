var connectWallet = document.getElementById('connectWallet');
var getWallet = document.getElementById('getWallet');
var error = document.getElementById('error');

/**
connectWallet.addEventListener('click', async () => {
    if (window.ethereum) {
        window.web3 = new Web3(ethereum);
        try {
            await ethereum.enable();
            var accounts = await web3.eth.getAccounts();
            account = accounts[0]
        } 
        catch (error) {
            statusBar.innerHTML = error.message
        }
    }
    else {
		error.innerHTML = 'You need an Ethereum Wallet to use DATAVIEW';
    }
    location.reload();
})

 */
 
if(getWallet) {
	getWallet.addEventListener('click', async () => {
	console.log("Inside click");
	window.open("https://metamask.io/download/");
});
} 



