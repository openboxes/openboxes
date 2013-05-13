<!--  Show recent shipments/receipts -->
<%@ page import="org.pih.warehouse.shipping.ShipmentStatusCode"%>


<div class="widget-small">
	<div class="widget-header">
		<h2><warehouse:message code="requisitions.label"/></h2>
	</div>
	<div class="widget-content" style="padding:0; margin:0">
		<div id="requisition-summary">
			<g:if test="${!requisitions}">
				<div style="margin:10px;" class="center">
					<span class="fade"><warehouse:message code="requisition.noRecent.label"/></span>
                </div>
			</g:if>
			<g:else>
                <table>
                    <g:set var="requisitionMap" value="${requisitions.groupBy { it.status }}"/>
                    <g:each var="status" in="${org.pih.warehouse.requisition.RequisitionStatus.list()}" status="i">
                        <g:set var="statusMessage" value="${format.metadata(obj: status)}"/>
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
                    </g:each>
                    <tfoot>
                        <tr>
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