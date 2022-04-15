/**
 * 
 */
var webench = document.getElementById('webench');
var profile = document.getElementById('profile');
var logout = document.getElementById('logout');

/**
window.addEventListener('load', async () => {
    if (window.ethereum) {
        window.web3 = new Web3(ethereum);
        await ethereum.enable();
        var accounts = await web3.eth.getAccounts();
       
        if (accounts.length > 0) {
            sessionStorage.setItem("connectedLoginUpdate", "true");
            logout.style.display = "block";
            webench.href = "webench"
    
        }
        else {
            sessionStorage.setItem("connectedLoginupdate", "false");
            logout.style.display = "none";
            webench.href = "connectWallet"

        }
        
    }
})
*/

window.addEventListener('load', async () => {
	var connected = sessionStorage.getItem("connected");
	
	if (connected && connected == "true") {
		logout.style.display = "block";
		webench.href = "webench"
		return
	}
	
	logout.style.display = "none";
    webench.href = "connectWallet"

})

//to logout, just make connected false in session storage
logout.addEventListener('click', () => {
	sessionStorage.setItem("connected", "false");
	location.reload();
})
