<%@ page import="org.pih.warehouse.inventory.Transaction" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'transaction.label', default: 'Transaction')}" />
        
        <title><warehouse:message code="transaction.list.label"/></title>    
    </head>    

	<body>
       <div class="body">
       
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			
			<div class="yui-gf">
                <div class="yui-u first">
                    <div class="box">
                        <h2>
                            <warehouse:message code="default.filters.label"/>
                        </h2>
                        <form>
                            <table>

                                <tr>
                                    <td>
                                        <label>
                                            <warehouse:message code="transaction.inventory.label"/>
                                        </label>

                                    </td>
                                    <td>
                                        ${session.warehouse.name}
                                    </td>

                                </tr>
                                <tr>
                                    <td>
                                        <label>
                                            <warehouse:message code="transaction.transactionType.label"/>

                                        </label>
                                    </td>
                                    <td>
                                        <select id="transactionTypeSelect" name="transactionType.id" class="chzn-select-deselect">
                                            <option value="">
                                                <warehouse:message code="transactionType.all.label"/>
                                            </option>
                                            <g:each var="transactionType" in="${org.pih.warehouse.inventory.TransactionType.list()}">
                                                <g:set var="numberOfTransactions" value="${transactionMap[transactionType?.id]?.size()?:0}"/>
                                                <g:set var="selected" value="${transactionTypeSelected == transactionType }"/>
                                                <g:if test="${numberOfTransactions > 0 }">
                                                    <option value="${transactionType?.id}" ${selected?'selected="selected"':'' }>
                                                    <format:metadata obj="${transactionType }"/>
                                                    (${numberOfTransactions })

                                                </g:if>
                                            </g:each>
                                        </select>
                                    </td>
                                </tr>
                                <tr>
                                    <td colspan="2" class="center">

                                        <g:submitButton name="search" value="Search" class="button"></g:submitButton>
                                    </td>

                                </tr>


                            </table>
                        </form>
                    </div>
                </div>

                <div class="yui-u">

                    <div class="box">
                        <h2><warehouse:message code="default.showing.message" args="[transactionCount]"/></h2>
                        <table>
                            <thead>
                                <tr class="even">
                                    <th><warehouse:message code="default.actions.label"/></th>
                                    <th><warehouse:message code="default.count.label" default="Count"/></th>
                                    <th><warehouse:message code="transaction.transactionNumber.label"/></th>
                                    <th><warehouse:message code="default.date.label"/></th>
                                    <th><warehouse:message code="transaction.type.label"/></th>
                                    <th><warehouse:message code="inventory.label"/></th>
                                    <th class="center">
                                        <warehouse:message code="default.source.label"/> /
                                        <warehouse:message code="default.destination.label"/>
                                    </th>
                                    <th class="center">
                                        <warehouse:message code="default.createdBy.label"/>
                                    </th>
                                    <th class="center">
                                        <warehouse:message code="default.updatedBy.label"/>
                                    </th>
                                    <th class="center">
                                        <warehouse:message code="default.dateCreated.label"/>
                                    </th>
                                    <th class="center">
                                        <warehouse:message code="default.lastUpdated.label"/>
                                    </th>
                                </tr>
                            </thead>
                            <tbody>
                                <g:each var="transactionInstance" in="${transactionInstanceList}" status="i">
                                    <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                                        <td align="center">

                                            <!-- Action menu -->
                                            <div>
                                                <div class="action-menu">
                                                    <button class="action-btn">
                                                        <img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle;"/>
                                                    </button>
                                                    <div class="actions">
                                                        <div class="action-menu-item">
                                                            <g:link controller="inventory" action="showTransaction" id="${transactionInstance?.id }">
                                                                <img src="${resource(dir: 'images/icons/silk', file: 'zoom.png')}" style="vertical-align: middle;"/>&nbsp;<warehouse:message code="transaction.view.label"/>
                                                            </g:link>
                                                        </div>
                                                        <div class="action-menu-item">
                                                            <g:link controller="inventory" action="editTransaction" id="${transactionInstance?.id }">
                                                                <img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}" style="vertical-align: middle;"/>&nbsp;<warehouse:message code="transaction.edit.label"/>
                                                            </g:link>
                                                        </div>
                                                        <div class="action-menu-item">
                                                            <g:link controller="inventory" action="deleteTransaction" id="${transactionInstance?.id }" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                                                                <img src="${resource(dir:'images/icons/silk',file:'bin.png')}" alt="Delete" />
                                                                &nbsp;${warehouse.message(code: 'transaction.delete.label')}&nbsp;
                                                            </g:link>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>

                                            <%--
                                            <g:if test="${transactionInstance?.confirmed }">
                                                <g:link action="confirmTransaction" id="${transactionInstance?.id }">Reset</g:link>
                                            </g:if>
                                            <g:else>
                                                <g:link action="confirmTransaction" id="${transactionInstance?.id }">Confirm</g:link>
                                            </g:else>
                                            --%>
                                        </td>
                                        <td>
                                            <div class="count">${transactionInstance?.transactionEntries?.size() }</div>
                                        </td>
                                        <td class="middle">
                                            <g:link action="showTransaction" id="${transactionInstance?.id }">
                                                <span class="transactionNumber">${transactionInstance?.transactionNumber?:transactionInstance?.id}</span>
                                            </g:link>
                                        </td>
                                        <td>
                                            ${formatDate(date: transactionInstance?.transactionDate, format: 'dd-MMM-yyyy') }
                                            ${formatDate(date: transactionInstance?.transactionDate, format: 'hh:mm:ssa') }
                                        </td>
                                        <td>
                                            <div>
                                            <format:metadata obj="${transactionInstance?.transactionType}"/>
                                            </div>
                                        </td>
                                        <td>
                                            ${transactionInstance?.inventory }
                                        </td>
                                        <td class="center">
                                            <g:if test="${transactionInstance?.source?.name}">
                                                ${transactionInstance?.source?.name }
                                            </g:if>
                                            <g:elseif test="${transactionInstance?.destination?.name }">
                                                ${transactionInstance?.destination?.name }
                                            </g:elseif>
                                            <g:else>
                                                <span class="fade">
                                                    <warehouse:message code="default.na.label" default="Not applicable"/>
                                                </span>
                                            </g:else>

                                        </td>
                                        <td class="center">
                                            <g:if test="${transactionInstance?.createdBy}">
                                                ${transactionInstance?.createdBy?.name }
                                            </g:if>
                                            <g:else>
                                                <span class="fade">
                                                    <warehouse:message code="default.nobody.label" default="Nobody"/>
                                                </span>
                                            </g:else>

                                        </td>
                                        <td class="center">
                                            <g:if test="${transactionInstance?.updatedBy}">
                                                ${transactionInstance?.updatedBy?.name }
                                            </g:if>
                                            <g:else>
                                                <span class="fade">
                                                    <warehouse:message code="default.nobody.label" default="Nobody"/>
                                                </span>
                                            </g:else>
                                        </td>
                                        <td class="center">
                                            ${transactionInstance?.dateCreated }
                                        </td>
                                        <td class="center">
                                            ${transactionInstance?.lastUpdated }
                                        </td>
                                    </tr>
                                </g:each>
                            </tbody>
                        </table>
                        <div class="paginateButtons">
                            <g:paginate total="${transactionCount}" params="['transactionType.id':transactionTypeSelected?.id]"/>
                        </div>
                    </div>

                </div>
			</div>
		</div>
		
		<script>
			$(document).ready(function() {			
				$("#transactionTypeSelect").change(function() {
				    $(this).closest("form").submit();
				});
			});
		
		</script>		
	</body>
</html>
