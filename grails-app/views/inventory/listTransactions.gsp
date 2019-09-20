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
                            <div class="filter">
                                <label>
                                    <g:message code="transaction.inventory.label"/>
                                </label>
                                <div class="value">
                                    ${session.warehouse.name}
                                </div>
                            </div>
                            <div class="filter">
                                <label><g:message code="transaction.transactionNumber.label"/></label>
                                <g:textField name="transactionNumber" style="width: 100%" class="text" value="${params.transactionNumber}"></g:textField>
                            </div>
                            <div class="filter">
                                <label><g:message code="transaction.transactionType.label"/></label>
                                <select id="transactionTypeSelect" name="transactionType.id" class="chzn-select-deselect">
                                    <option value="">
                                        <warehouse:message code="transactionType.all.label"/>
                                    </option>
                                    <g:each var="transactionType" in="${org.pih.warehouse.inventory.TransactionType.list()}">
                                        <g:set var="selected" value="${transactionTypeSelected == transactionType }"/>
                                        <option value="${transactionType?.id}" ${selected?'selected="selected"':'' }>
                                        <format:metadata obj="${transactionType }"/>
                                    </g:each>
                                </select>
                            </div>
                            <div class="filter">
                                <label>
                                    <warehouse:message code="default.dateRange.label" default="Date Range"/>
                                </label>
                                <div>
                                    <g:jqueryDatePicker id="transactionDateFrom" name="transactionDateFrom" placeholder="${g.message(code:'default.from.label')}"
                                                        value="${params.transactionDateFrom}" format="MM/dd/yyyy" autocomplete="off"/>
                                </div>
                            </div>
                            <div class="filter">
                                <div>
                                    <g:jqueryDatePicker id="transactionDateTo" name="transactionDateTo" placeholder="${g.message(code:'default.to.label')}"
                                                        value="${params.transactionDateTo}" format="MM/dd/yyyy" autocomplete="off"/>
                                </div>
                            </div>
                            <div class="filter">
                                <g:submitButton name="search" value="Search" class="button"></g:submitButton>
                            </div>

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
                                        <warehouse:message code="default.dateCreated.label"/>
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
                                                            <g:link action="showTransaction" id="${transactionInstance?.id }">
                                                                <img src="${resource(dir: 'images/icons/silk', file: 'zoom.png')}" style="vertical-align: middle;"/>&nbsp;<g:message code="default.button.show.label"/>
                                                            </g:link>
                                                            <g:link action="editTransaction" id="${transactionInstance?.id }">
                                                                <img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}" style="vertical-align: middle;"/>&nbsp;<g:message code="default.button.edit.label"/>
                                                            </g:link>
                                                            <hr/>
                                                            <g:isUserAdmin>
                                                                <g:link action="deleteTransaction" id="${transactionInstance?.id }">
                                                                    <img src="${resource(dir: 'images/icons/silk', file: 'delete.png')}" style="vertical-align: middle;"/>&nbsp;<g:message code="default.button.delete.label"/>
                                                                </g:link>
                                                            </g:isUserAdmin>
                                                        </div>
                                                    </div>
                                                </div>
                                            </div>
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
                                            ${transactionInstance?.dateCreated }
                                        </td>
                                    </tr>
                                </g:each>
                            </tbody>
                        </table>
                    </div>
                    <div class="paginateButtons">
                        <g:paginate total="${transactionCount}" params="['transactionType.id':transactionTypeSelected?.id]"/>
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
