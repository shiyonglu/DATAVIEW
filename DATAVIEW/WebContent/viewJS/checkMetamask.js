var webench = document.getElementById('webench');
var profile = document.getElementById('profile');
var logout = document.getElementById('logout');

// var walletConnected = checkIfWalletConnected();
// console.log(walletConnected);


// if(wallxetConnected) {
//     logout.style.visibility = "visible";
// } else {
//     logout.style.visibility = "hidden";
// }

function checkIfWalletConnected() {
    if (window.ethereum) {
        window.web3 = new Web3(ethereum);
        var accounts = web3.eth.getAccounts();
        if (accounts.length > 0) {
            sessionStorage.setItem("connected1", "true");
            return true;
        }
        else {
            sessionStorage.setItem("connected1", "false");
            return false;
        }
        
    }
}

window.addEventListener('load', async () => {
    if (window.ethereum) {
        window.web3 = new Web3(ethereum);
        var accounts = await web3.eth.getAccounts();
       
        if (accounts.length > 0) {
            sessionStorage.setItem("connected1", "true");
            logout.style.display = "block";
            return true;
        }
        else {
            sessionStorage.setItem("connected1", "false");
            logout.style.display = "none";
            return false;
        }
        
    }
})
