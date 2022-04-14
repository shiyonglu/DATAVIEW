/**
 * 
 */
var webench = document.getElementById('webench');
var profile = document.getElementById('profile');
var logout = document.getElementById('logout');

window.addEventListener('load', async () => {
    if (window.ethereum) {
        window.web3 = new Web3(ethereum);
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