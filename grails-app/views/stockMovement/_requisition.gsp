<div id="details-tab">
    <div class="box">
        <h2><warehouse:message code="requestDetails.label"/></h2>

        <div>
            <table>

                <thead>
                <tr class="odd">
                    <th></th>
                    <th><warehouse:message code="requisition.status.label"/></th>
                    <th><warehouse:message code="product.productCode.label"/></th>
                    <th><warehouse:message code="product.label"/></th>
                    <th class="center"><warehouse:message code="product.uom.label" /></th>
                    <th class="center"><warehouse:message code="requisitionItem.quantityRequested.label" default="Requested" /></th>
                    <th class="center"><warehouse:message code="requisitionItem.quantityApproved.label" /></th>
                    <th class="center"><warehouse:message code="requisitionItem.quantityPicked.label" default="Picked"/></th>
                    <th class="center"><warehouse:message code="requisitionItem.quantityAdjusted.label" /></th>
                    <th class="center"><warehouse:message code="requisitionItem.quantityIssued.label" default="Issued"/></th>
                    <th class="center"><warehouse:message code="requisitionItem.reasonCodes.label" /></th>
                </tr>
                </thead>
                <tbody>
                <g:each var="requisitionItem" in="${stockMovement?.requisition?.originalRequisitionItems?.sort()}" status="i">
                    <g:render template="../requisition/showRequisitionItem" model="[i:i,requisitionItem:requisitionItem, requestTab:true]"/>
                </g:each>
                </tbody>
            </table>
        </div>
    </div>
</div>
