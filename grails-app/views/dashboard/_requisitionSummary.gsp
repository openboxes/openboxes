<!--  Show recent shipments/receipts -->
<%@ page import="org.pih.warehouse.shipping.ShipmentStatusCode"%>


<div class="widget-small">
	<div class="widget-header">
		<h2><warehouse:message code="requisitions.label"/> (all-time)</h2>
	</div>
	<div class="widget-content" style="padding:0; margin:0">
		<div id="requisition-summary">
			<g:if test="${!requisitions}">
				<div style="margin:10px;" class="center empty">
					<span class="fade"><warehouse:message code="requisition.noRecent.label"/></span>
                </div>
			</g:if>
			<g:else>
                <table>
                    <g:set var="requisitionMap" value="${requisitions.groupBy { it.status }}"/>
                    <g:set var="i" value="${0}"/>
                    <g:each var="status" in="${org.pih.warehouse.requisition.RequisitionStatus.list()}">
                        <g:set var="requisitionCount" value="${requisitionMap[status]?.size()?:0}"/>
                        <g:set var="statusMessage" value="${format.metadata(obj: status)}"/>
                        <g:if test="${requisitionCount}">
                            <tr class="${i%2?'odd':'even'}">
                                <td class="center" style="width: 1%">
                                    <img src="${createLinkTo(dir:'images/icons/requisitionStatus', file:'requisition_status_' + status?.name()?.toLowerCase() + '.png')}"/>

                                </td>
                                <td>
                                    <g:link controller="requisition" action="list" params="[status:status]" fragment="${statusMessage}">
                                        <warehouse:message code="requisitions.label"/>
                                        ${format.metadata(obj: status)?.toLowerCase()}
                                    </g:link>
                                </td>
                                <td class="right">
                                    <g:link controller="requisition" action="list" params="[status:status]" fragment="${statusMessage}">
                                        ${requisitionMap[status]?.size()?:0}
                                    </g:link>
                                </td>
                            </tr>
                            <g:set var="i" value="${i+1}"/>
                        </g:if>
                    </g:each>
                    <tfoot>
                        <tr class="odd">
                            <th colspan="2">
                                <label>${warehouse.message(code:'default.total.label')}</label>
                            </th>
                            <th class="right">
                                <g:link controller="requisition" action="list">
                                    ${requisitions?.size()?:0}
                                </g:link>
                            </th>
                        </tr>

                    </tfoot>
                </table>


			</g:else>
		</div>
	</div>	
</div>