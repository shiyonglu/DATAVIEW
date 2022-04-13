var statusBar = document.getElementById('statusBar')
var connectWallet = document.getElementById('connectWallet')
var showAccount = document.getElementById('showAccount')
var showBalance = document.getElementById('showBalance')

var account 
var ethBalance
var dgcBalance
var contractABI = [{"inputs":[],"payable":false,"stateMutability":"nonpayable","type":"constructor"},{"anonymous":false,"inputs":[{"indexed":true,"internalType":"address","name":"tokenOwner","type":"address"},{"indexed":true,"internalType":"address","name":"spender","type":"address"},{"indexed":false,"internalType":"uint256","name":"rawAmt","type":"uint256"}],"name":"Approval","type":"event"},{"anonymous":false,"inputs":[{"indexed":true,"internalType":"uint256","name":"datasetId","type":"uint256"},{"indexed":true,"internalType":"address","name":"account","type":"address"}],"name":"AssignDatasetId","type":"event"},{"anonymous":false,"inputs":[{"indexed":true,"internalType":"uint256","name":"listingID","type":"uint256"},{"indexed":true,"internalType":"uint256","name":"datasetID","type":"uint256"},{"indexed":false,"internalType":"address","name":"oldOwner","type":"address"},{"indexed":false,"internalType":"address","name":"newOwner","type":"address"},{"indexed":false,"internalType":"uint256","name":"price","type":"uint256"}],"name":"Sold","type":"event"},{"anonymous":false,"inputs":[{"indexed":true,"internalType":"address","name":"from","type":"address"},{"indexed":true,"internalType":"address","name":"to","type":"address"},{"indexed":false,"internalType":"uint256","name":"rawAmt","type":"uint256"}],"name":"Transfer","type":"event"},{"anonymous":false,"inputs":[{"indexed":true,"internalType":"uint256","name":"datasetId","type":"uint256"},{"indexed":true,"internalType":"address","name":"oldOwner","type":"address"},{"indexed":true,"internalType":"address","name":"newOwner","type":"address"}],"name":"TransferDatasetOwner","type":"event"},{"payable":true,"stateMutability":"payable","type":"fallback"},{"constant":true,"inputs":[{"internalType":"address","name":"tokenOwner","type":"address"},{"internalType":"address","name":"spender","type":"address"}],"name":"allowance","outputs":[{"internalType":"uint256","name":"remaining","type":"uint256"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":false,"inputs":[{"internalType":"address","name":"spender","type":"address"},{"internalType":"uint256","name":"rawAmt","type":"uint256"}],"name":"approve","outputs":[{"internalType":"bool","name":"success","type":"bool"}],"payable":false,"stateMutability":"nonpayable","type":"function"},{"constant":true,"inputs":[{"internalType":"address","name":"tokenOwner","type":"address"}],"name":"balanceOf","outputs":[{"internalType":"uint256","name":"balance","type":"uint256"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":true,"inputs":[],"name":"decimals","outputs":[{"internalType":"uint8","name":"","type":"uint8"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":true,"inputs":[{"internalType":"uint256","name":"datasetId","type":"uint256"}],"name":"getDatasetOwner","outputs":[{"internalType":"address","name":"","type":"address"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":true,"inputs":[{"internalType":"uint256","name":"listingID","type":"uint256"}],"name":"getListingDatasetID","outputs":[{"internalType":"uint256","name":"","type":"uint256"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":true,"inputs":[{"internalType":"uint256","name":"listingID","type":"uint256"}],"name":"getListingOwner","outputs":[{"internalType":"address","name":"","type":"address"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":true,"inputs":[{"internalType":"uint256","name":"listingID","type":"uint256"}],"name":"getListingPrice","outputs":[{"internalType":"uint256","name":"","type":"uint256"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":true,"inputs":[{"internalType":"uint256","name":"listingID","type":"uint256"}],"name":"getListingStatus","outputs":[{"internalType":"uint256","name":"","type":"uint256"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":false,"inputs":[{"internalType":"string","name":"key","type":"string"},{"internalType":"string","name":"value","type":"string"}],"name":"insertAccountInfo","outputs":[{"internalType":"bool","name":"success","type":"bool"}],"payable":false,"stateMutability":"nonpayable","type":"function"},{"constant":false,"inputs":[{"internalType":"string","name":"firstname","type":"string"},{"internalType":"string","name":"lastname","type":"string"},{"internalType":"string","name":"organization","type":"string"},{"internalType":"string","name":"street1","type":"string"},{"internalType":"string","name":"street2","type":"string"},{"internalType":"string","name":"city","type":"string"},{"internalType":"string","name":"state","type":"string"},{"internalType":"string","name":"province","type":"string"},{"internalType":"string","name":"zip","type":"string"},{"internalType":"string","name":"country","type":"string"},{"internalType":"string","name":"email","type":"string"},{"internalType":"string","name":"phone","type":"string"},{"internalType":"string","name":"fax","type":"string"},{"internalType":"string","name":"website","type":"string"}],"name":"insertAccountProfile","outputs":[{"internalType":"bool","name":"","type":"bool"}],"payable":false,"stateMutability":"nonpayable","type":"function"},{"constant":false,"inputs":[{"internalType":"uint256","name":"datasetId","type":"uint256"},{"internalType":"string","name":"key","type":"string"},{"internalType":"string","name":"value","type":"string"}],"name":"insertMetadata","outputs":[{"internalType":"bool","name":"success","type":"bool"}],"payable":false,"stateMutability":"nonpayable","type":"function"},{"constant":true,"inputs":[],"name":"lastDatasetId","outputs":[{"internalType":"uint256","name":"","type":"uint256"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":true,"inputs":[],"name":"lastListingID","outputs":[{"internalType":"uint256","name":"","type":"uint256"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":false,"inputs":[{"internalType":"uint256","name":"datasetID","type":"uint256"},{"internalType":"uint256","name":"price","type":"uint256"}],"name":"listForSale","outputs":[{"internalType":"uint256","name":"","type":"uint256"}],"payable":false,"stateMutability":"nonpayable","type":"function"},{"constant":true,"inputs":[],"name":"name","outputs":[{"internalType":"string","name":"","type":"string"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":false,"inputs":[{"internalType":"uint256","name":"listingID","type":"uint256"}],"name":"purchaseDataset","outputs":[{"internalType":"bool","name":"","type":"bool"}],"payable":false,"stateMutability":"nonpayable","type":"function"},{"constant":true,"inputs":[{"internalType":"address","name":"account","type":"address"},{"internalType":"string","name":"key","type":"string"}],"name":"queryAccountInfo","outputs":[{"internalType":"string","name":"","type":"string"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":true,"inputs":[{"internalType":"uint256","name":"datasetId","type":"uint256"},{"internalType":"string","name":"key","type":"string"}],"name":"queryMetadata","outputs":[{"internalType":"string","name":"value","type":"string"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":true,"inputs":[{"internalType":"uint256","name":"listingID","type":"uint256"}],"name":"queryPrice","outputs":[{"internalType":"uint256","name":"","type":"uint256"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":false,"inputs":[{"internalType":"string","name":"contributor","type":"string"},{"internalType":"string","name":"coverage","type":"string"},{"internalType":"string","name":"creator","type":"string"},{"internalType":"string","name":"date","type":"string"},{"internalType":"string","name":"description","type":"string"},{"internalType":"string","name":"format","type":"string"},{"internalType":"string","name":"identifier","type":"string"},{"internalType":"string","name":"language","type":"string"},{"internalType":"string","name":"publisher","type":"string"},{"internalType":"string","name":"relation","type":"string"},{"internalType":"string","name":"rights","type":"string"},{"internalType":"string","name":"source","type":"string"},{"internalType":"string","name":"subject","type":"string"},{"internalType":"string","name":"title","type":"string"},{"internalType":"string","name":"dtype","type":"string"}],"name":"registerDatasetProfile","outputs":[{"internalType":"uint256","name":"","type":"uint256"}],"payable":false,"stateMutability":"nonpayable","type":"function"},{"constant":true,"inputs":[],"name":"symbol","outputs":[{"internalType":"string","name":"","type":"string"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":true,"inputs":[],"name":"totalSupply","outputs":[{"internalType":"uint256","name":"","type":"uint256"}],"payable":false,"stateMutability":"view","type":"function"},{"constant":false,"inputs":[{"internalType":"address","name":"to","type":"address"},{"internalType":"uint256","name":"rawAmt","type":"uint256"}],"name":"transfer","outputs":[{"internalType":"bool","name":"success","type":"bool"}],"payable":false,"stateMutability":"nonpayable","type":"function"},{"constant":false,"inputs":[{"internalType":"uint256","name":"datasetId","type":"uint256"},{"internalType":"address","name":"newOwner","type":"address"}],"name":"transferDatasetOwner","outputs":[{"internalType":"bool","name":"success","type":"bool"}],"payable":false,"stateMutability":"nonpayable","type":"function"},{"constant":false,"inputs":[{"internalType":"address","name":"from","type":"address"},{"internalType":"address","name":"to","type":"address"},{"internalType":"uint256","name":"rawAmt","type":"uint256"}],"name":"transferFrom","outputs":[{"internalType":"bool","name":"success","type":"bool"}],"payable":false,"stateMutability":"nonpayable","type":"function"},{"constant":false,"inputs":[{"internalType":"uint256","name":"listingID","type":"uint256"}],"name":"unlistFromSale","outputs":[{"internalType":"bool","name":"","type":"bool"}],"payable":false,"stateMutability":"nonpayable","type":"function"}]
var contractAddress = '0xAaD5dC6860deC75BA775837f83d533782aF72E0c'
var contract

window.addEventListener('load', async () => {
    if (window.ethereum) {
        window.web3 = new Web3(ethereum);
        var accounts = await web3.eth.getAccounts();
        contract = new web3.eth.Contract(contractABI,  contractAddress)
        if (accounts.length > 0) {
            account = accounts[0]
            statusBar.innerHTML = ('<b>Wallet connected!</b>')
            sessionStorage.setItem("connected", "true");
            ethBalance = await web3.eth.getBalance(account)
            dgcBalance = await contract.methods.balanceOf(account).call()

            showAccount.innerHTML = "Current account: "+account
            showAccount.style.visibility = "visible"
            showBalance.innerHTML = "Balance: " + parseFloat(web3.utils.fromWei(ethBalance, 'ether')).toFixed(4) + ' ETH, ' + web3.utils.fromWei(dgcBalance, 'ether') + ' DGC'
            showBalance.style.visibility = "visible"
        }
        else {
            sessionStorage.setItem("connected", "false");
        }
        
    }
})

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
    else if (window.web3) {
        window.web3 = new Web3(web3.currentProvider);
    }
    else {
        statusBar.innerHTML = ("Non-Ethereum browser detected. Please consider trying <a href='https://metamask.io/download' target='window'>MetaMask</a>!")
    }
    location.reload();
})

