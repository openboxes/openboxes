<!--  Show recent shipments/receipts -->
<%@ page import="org.pih.warehouse.shipping.ShipmentStatusCode"%>


<div class="box">
    <h2>
        <warehouse:message code="requisition.summary.label" default="Requisition Summary"/>

        <%--
        <span class="action-menu">
            <button class="action-btn">
                <img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" style="vertical-align: middle"/>
            </button>
            <div class="actions">
                <div class="action-menu-item">
                    <g:link controller="dashboard" action="index" class="${!params.onlyShowMine?'selected':''}">
                        <img src="${createLinkTo(dir:'images/icons/silk',file:'application_view_list.png')}" alt="View requests" style="vertical-align: middle" />
                        Show all requisitions
                    </g:link>
                </div>
                <div class="action-menu-item">
                    <g:link controller="dashboard" action="index" class="${params.onlyShowMine.equals('true')?'selected':''}"
                            params="['onlyShowMine':true]">
                        <img src="${createLinkTo(dir:'images/icons/silk',file:'user.png')}" alt="View requests" style="vertical-align: middle" />
                        Show my requisitions
                    </g:link>
                </div>
            </div>
        </span>
        --%>
    </h2>
	<div class="widget-content" style="padding:0; margin:0">
		<div id="requisition-summary">
			<g:if test="${!requisitionStatistics?.ALL}">
				<div style="margin:10px;" class="center empty">
					<span class="fade"><warehouse:message code="requisition.noRecent.label"/></span>
                </div>
			</g:if>
			<g:else>
                <table>
                    <thead>
                        <tr>
                            <th></th>
                            <th>Status</th>
                            <th>Count</th>
                        </tr>
                    </thead>
                    <tbody>
                        <g:set var="i" value="${0}"/>
                        <g:each var="status" in="${org.pih.warehouse.requisition.RequisitionStatus.list()}">
                            <g:set var="statusMessage" value="${format.metadata(obj: status)}"/>
                            <tr class="${i%2?'odd':'even'}">
                                <td class="center" style="width: 1%">
                                    <img src="${createLinkTo(dir:'images/icons/requisitionStatus', file:'requisition_status_' + status?.name()?.toLowerCase() + '.png')}"/>

                                </td>
                                <td>
                                    <g:link controller="requisition" action="list" params="[status:status]" fragment="${statusMessage}">
                                        <%--<warehouse:message code="requisitions.label"/>--%>
                                        ${format.metadata(obj: status)}
                                    </g:link>
                                </td>
                                <td class="right">
                                    <g:link controller="requisition" action="list" params="[status:status]" fragment="${statusMessage}">
                                        ${requisitionStatistics[status]?:0}
                                    </g:link>
                                </td>
                            </tr>
                            <g:set var="i" value="${i+1}"/>

                        </g:each>
                        <%--
                        <g:if test="${requisitionStatistics['MINE']}">
                            <tr class="${i%2?'odd':'even'}">
                                <td class="center" style="width: 1%">
                                    <img src="${createLinkTo(dir:'images/icons/silk', file: 'user.png')}"/>

                                </td>
                                <td>
                                    <g:link controller="requisition" action="list" params="[status:status]" fragment="${statusMessage}">
                                        <warehouse:message code="requisitions.mine.label" default="My requisitions"/>
                                    </g:link>
                                </td>
                                <td class="right">
                                    <g:link controller="requisition" action="list" params="[status:status]" fragment="${statusMessage}">
                                        ${requisitionStatistics["MINE"]?:0}
                                    </g:link>
                                </td>
                            </tr>
                        </g:if>
                        --%>
                    </tbody>
                    <tfoot>
                        <tr class="odd">
                            <th colspan="2">
                                <label>${warehouse.message(code:'default.total.label')}</label>
                            </th>
                            <th class="right">
                                <g:link controller="requisition" action="list">
                                    ${requisitionStatistics?."ALL"?:0}
                                </g:link>
                            </th>
                        </tr>

                    </tfoot>
                </table>


			</g:else>
		</div>
	</div>	
</div>