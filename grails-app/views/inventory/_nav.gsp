<img src="${createLinkTo(dir:'images/icons/silk',file:'table_refresh.png')}" alt="Inventory" /> &nbsp;
<g:link controller="inventory" action="browse">Browse Inventory</g:link>
<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
<g:link controller="inventory" action="listAllTransactions">All Transactions</g:link> 
<%-- 
<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
<g:link action="listPendingTransactions">Pending Transactions</g:link>  
<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
<g:link controller="inventory" action="listConfirmedTransactions">Confirmed Transactions</g:link> 
--%>
<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
<g:link controller="inventory" action="createTransaction">Add Transaction</g:link> 				
