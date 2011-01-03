<div>
	<span class="menuButton">
		<g:link class="list" controller="inventory" action="browse">Browse Inventory</g:link>
	</span>
	<span class="menuButton">
		<g:link class="list" controller="inventory" action="listAllTransactions">All Transactions</g:link> 
	</span>
	<span class="menuButton">
		<g:link class="new" controller="inventory" action="createTransaction">Add Transaction</g:link> 				
	</span>
</div>
<%-- 
<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
<g:link action="listPendingTransactions">Pending Transactions</g:link>  
<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
<g:link controller="inventory" action="listConfirmedTransactions">Confirmed Transactions</g:link> 
--%>
