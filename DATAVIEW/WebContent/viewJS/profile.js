var showAccount = document.getElementById('showAccount')
var showBalance = document.getElementById('showBalance')


var account 
var ethBalance


window.addEventListener('load', async () => {
    if (window.ethereum) {
        window.web3 = new Web3(ethereum);
        var accounts = await web3.eth.getAccounts();
        if (accounts.length > 0) {
            account = accounts[0]
      
            ethBalance = await web3.eth.getBalance(account)
            showAccount.innerHTML = "Account: "+account
            showAccount.style.visibility = "visible"
            showBalance.innerHTML = "Balance: " + parseFloat(web3.utils.fromWei(ethBalance, 'ether')).toFixed(4) + ' ETH'
            showBalance.style.visibility = "visible"
        }
    }
})

