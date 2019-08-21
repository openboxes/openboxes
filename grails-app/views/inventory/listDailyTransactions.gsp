<%@ page import="org.pih.warehouse.inventory.Transaction" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'transaction.label', default: 'Transaction')}" />
        
        <title><warehouse:message code="transaction.dailyTransactions.label"/></title>    
        <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'scrolltable.css')}" type="text/css" media="screen, projection" />
    </head>    

	<body>
       <div class="body">
       
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>			
								
			<fieldset>		
				<div class="box">
					<h2><warehouse:message code="transaction.dailyTransactionsFor.label"/> ${session.warehouse.name}
                        <span class="fade">${formatDate(date: dateSelected, format: 'EEEEE, MMMMM dd yyyy') }</span></h2>

                    <table>
                        <tr>
                            <td width="15%">
                                <div class="list">
                                    <table class="scrollTable">
                                        <thead class="fixedHeader">
                                            <tr>
                                                <th class="odd"><warehouse:message code="default.dates.label"/></th>
                                            </tr>
                                        </thead>

                                        <g:if test="${transactionsByDate }">
                                            <tbody class="scrollContent">
                                                <g:each var="entry" in="${transactionsByDate }" status="i">
                                                    <g:set var="isDateSelected" value="${dateSelected == entry.key}"/>
                                                    <g:set var="dateParam" value="${formatDate(date: entry.key, format: 'dd/MM/yyyy') }"/>
                                                    <g:set var="dateDisplay" value="${formatDate(date: entry.key, format: 'EEEE, MMMMM dd, yyyy') }"/>
                                                    <tr class="prop ${i%2?'odd':'even' }">
                                                        <td class="left">
                                                            <g:if test="${!isDateSelected}">
                                                                <g:link action="listDailyTransactions" params="[date: dateParam]">
                                                                    ${dateDisplay }
                                                                </g:link>
                                                            </g:if>
                                                            <g:else>
                                                                <span class="selected">${dateDisplay }</span>
                                                            </g:else>
                                                            (${entry.value.size() })
                                                        </td>
                                                    </tr>
                                                </g:each>
                                            </tbody>
                                        </g:if>
                                        <g:else>
                                            <warehouse:message code="transaction.noTransactions.label"/>
                                        </g:else>
                                    </table>
                                </div>

                            </td>
                            <td class="left">

                                <div class="list">
                                    <table>
                                        <thead>
                                            <tr class="odd">
                                                <th><warehouse:message code="default.datetime.label"/></th>
                                                <th><warehouse:message code="transaction.id.label"/></th>
                                                <th><warehouse:message code="transaction.type.label"/></th>
                                                <th><warehouse:message code="product.label"/></th>
                                                <th><warehouse:message code="product.lotNumber.label"/></th>
                                                <th><warehouse:message code="default.quantity.label"/></th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <g:set var="counter" value="${0 }" />
                                            <g:each var="transaction" in="${transactions}" status="i">
                                                <g:each var="transactionEntry" in="${transaction.transactionEntries}" status="j">
                                                    <tr class="${counter++%2?'odd':'even' }">
                                                        <td>
                                                            ${transaction?.dateCreated }
                                                        </td>
                                                        <td>
                                                            <g:link controller="inventory" action="showTransaction" id="${transaction.id }">
                                                            ${transaction?.transactionNumber }
                                                            </g:link>
                                                        </td>
                                                        <td>
                                                            <format:metadata obj="${transaction?.transactionType}"/>
                                                            <g:if test="${transaction.source }">
                                                                from ${transaction?.source?.name }
                                                            </g:if>
                                                            <g:if test="${transaction.destination }">
                                                                to ${transaction?.destination?.name }
                                                            </g:if>

                                                        </td>

                                                        <td>
                                                            <format:product product="${transactionEntry?.inventoryItem?.product}"/>
                                                        </td>
                                                        <td>
                                                            ${transactionEntry?.inventoryItem?.lotNumber }
                                                        </td>
                                                        <td>
                                                            ${transactionEntry.quantity }
                                                        </td>
                                                    </tr>
                                                </g:each>
                                            </g:each>
                                        </tbody>
                                    </table>
                                </div>
                            </td>
                        </tr>
                    </table>
                </div>
			</fieldset>
		</div>
		
	</body>

</html>
