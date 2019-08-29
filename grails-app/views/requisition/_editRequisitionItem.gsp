<g:set var="quantityAvailable" value="${quantityOnHandMap[requisitionItem.product] }"/>
<g:set var="substitutionAvailable" value="${quantityOnHandMap.values()?.sum() > requisitionItem.quantity}"/>
<g:set var="shouldApprove" value="${quantityAvailable > 0 && requisitionItem.quantity <= quantityAvailable}"/>
<g:set var="shouldChange" value="${false}"/>
<g:set var="shouldCancel" value="${requisitionItem.quantity > quantityAvailable && !substitutionAvailable}"/>
<g:set var="shouldSubstitute" value="${requisitionItem.quantity > quantityAvailable && substitutionAvailable}"/>

<g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
</g:if>

<g:hasErrors bean="${requisitionItem}">
    <div class="errors">
        <g:renderErrors bean="${requisitionItem}" as="list" />
    </div>
</g:hasErrors>

<g:if test="${params.actionType=='show'}">
<div class="box" id="editRequisitionItem">
    <h2>
        <table>
            <tr>
                <td class="middle left">

                    <div class="button-container">
                        <div class="button-group">

                            <g:remoteLink controller="requisition" action="showRequisitionItems" id="${requisitionItem?.requisition?.id }" update="requisitionItems" class="button icon log">
                                <warehouse:message code="default.button.list.label" default="Back to list"/>
                            </g:remoteLink>
                            <g:remoteLink controller="requisition" action="editRequisitionItem" id="${requisitionItem?.requisition?.id }"
                                          params="['requisitionItem.id':requisitionItem?.id, actionType:'show']" update="requisitionItems" class="button icon reload">
                                <warehouse:message code="default.button.refresh.label" default="Refresh"/>
                            </g:remoteLink>

                        </div>
                    </div>
                </td>
                <td class="middle center">
                    ${requisitionItem.orderIndex+1} out of ${requisitionItem.requisition.requisitionItemCount}
                </td>
                <td class="middle right">
                    <div class="button-container">
                        <div class="button-group">
                            <g:remoteLink controller="requisition" action="previousRequisitionItem" id="${requisitionItem?.requisition?.id }"
                                          onFailure="alert('An error has occurred.  Please contact your system administrator (${requisition.requestNumber}).')"
                                          params="['requisitionItem.id':requisitionItem?.id, actionType:'show']" update="requisitionItems" class="button icon arrowleft">
                                <warehouse:message code="default.button.previous.label" default="Previous"/>
                            </g:remoteLink>
                            <g:remoteLink controller="requisition" action="nextRequisitionItem" id="${requisitionItem?.requisition?.id }"
                                          onFailure="alert('An error has occurred.  Please contact your system administrator (${requisition.requestNumber}).')"
                                          params="['requisitionItem.id':requisitionItem?.id, actionType:'show']" update="requisitionItems" class="button icon arrowright">
                                <warehouse:message code="default.button.next.label" default="Next"/>
                            </g:remoteLink>
                        </div>

                        <g:remoteLink controller="requisition" action="showRequisitionItems" id="${requisitionItem?.requisition?.id }" update="requisitionItems" class="button icon remove">
                            <warehouse:message code="default.button.close.label" default="Close"/>
                        </g:remoteLink>
                    </div>
                </td>
            </tr>

        </table>
    </h2>

    <table>
        <tr class="prop">
            <td class="middle right name">
                <label><warehouse:message code="requisitionItem.status.label"/></label>
            </td>
            <td class="middle">

                <g:if test="${requisitionItem?.isSubstituted()}">
                    <img src="${resource(dir:'images/icons/silk',file:'arrow_switch.png')}"/>
                </g:if>
                <g:elseif test="${requisitionItem?.isSubstitution()}">
                    <img src="${resource(dir:'images/icons',file:'indent.gif')}"/>
                </g:elseif>
                <g:elseif test="${requisitionItem?.isChanged()}">
                    <img src="${resource(dir:'images/icons/silk',file:'pencil.png')}"/>
                </g:elseif>
                <g:elseif test="${requisitionItem?.isPending()}">
                    <img src="${resource(dir:'images/icons/silk',file:'hourglass.png')}"/>
                </g:elseif>
                <g:elseif test="${requisitionItem?.isCanceled()}">
                    <img src="${resource(dir:'images/icons/silk',file:'decline.png')}"/>
                </g:elseif>
                <g:elseif test="${requisitionItem?.isApproved()||requisitionItem?.isCompleted()}">
                    <img src="${resource(dir:'images/icons/silk',file:'accept.png')}"/>
                </g:elseif>
                <format:metadata obj="${requisitionItem?.status}"/>

            </td>
        </tr>

        <tr class="prop">
            <td class="middle right name">
                <label><warehouse:message code="product.label"/></label>
            </td>
            <td class="middle">
                <g:if test="${requisitionItem?.isCanceled()}">
                    <div class="canceled">
                        ${requisitionItem?.product?.productCode}
                        ${requisitionItem?.product?.name} (${requisitionItem?.product?.unitOfMeasure})
                    </div>
                </g:if>
                <g:elseif test="${requisitionItem?.isSubstituted()}">
                    <div class="canceled">
                        ${requisitionItem?.product?.productCode}
                        ${requisitionItem?.product?.name} (${requisitionItem?.product?.unitOfMeasure})
                    </div>
                    <div>
                        ${requisitionItem?.substitutionItem?.product?.productCode}
                        ${requisitionItem?.substitutionItem?.product?.name} (${requisitionItem?.substitutionItem?.product?.unitOfMeasure})
                    </div>
                </g:elseif>
                <g:else>
                    ${requisitionItem?.product?.productCode}
                    <format:product product="${requisitionItem?.product}"/>

                    (${requisitionItem?.product?.unitOfMeasure})
                </g:else>
            </td>

        </tr>
        <tr class="prop">
            <td class="middle right name">
                <label><warehouse:message code="requisitionItem.quantity.label"/></label>
            </td>
            <td class="middle">
                <g:if test="${requisitionItem?.isSubstituted()}">
                    <div class="canceled">
                        ${requisitionItem?.quantity?:0} EA/1
                    </div>
                    <div>
                        ${requisitionItem?.substitutionItem?.quantity?:0} EA/1
                    </div>
                </g:if>
                <g:elseif test="${requisitionItem?.isCanceled()}">
                    <div class="canceled">
                        ${requisitionItem?.quantity?:0} EA/1
                    </div>
                </g:elseif>
                <g:elseif test="${requisitionItem?.isChanged()}">
                    <div class="canceled">
                        ${requisitionItem?.quantity?:0} EA/1
                    </div>
                    <div>
                        ${requisitionItem?.modificationItem?.quantity?:0} EA/1
                    </div>

                </g:elseif>
                <g:else>
                    ${requisitionItem?.quantity?:0} EA/1
                </g:else>
            </td>
        </tr>
        <tr class="prop">
            <td class="middle right name">
                <label><warehouse:message code="requisitionItem.cancelReasonCode.label"/></label>
            </td>
            <td class="middle">
                <g:if test="${requisitionItem?.cancelReasonCode}">
                    ${warehouse.message(code:'enum.ReasonCode.' + requisitionItem?.cancelReasonCode)}
                </g:if>
                <g:else>
                    ${warehouse.message(code:'default.none.label')}
                </g:else>

            </td>
        </tr>
        <tr class="prop">
            <td class="middle right name">
                <label><warehouse:message code="requisitionItem.cancelComments.label"/></label>
            </td>
            <td class="middle">
                <g:if test="${requisitionItem?.cancelComments}">
                    ${requisitionItem?.cancelComments}
                </g:if>
                <g:else>
                    ${warehouse.message(code:'default.none.label')}
                </g:else>



            </td>
        </tr>
        <tr class="prop">
            <td class="top right name">
                <label><warehouse:message code="requisitionItem.availability.label" default="Availability"/></label>
            </td>
            <td class="middle">
                <table style="width:auto;" class="box">
                    <tr>
                        <th><warehouse:message code="product.productCode.label"/></th>
                        <th><warehouse:message code="product.label"/></th>
                        <th><warehouse:message code="product.productPackages.label" default="Package sizes"/></th>
                        <th><warehouse:message code="inventoryItem.quantityOnHand.label"/></th>
                        <th><warehouse:message code="inventoryItem.status.label" default="Status"/></th>
                    </tr>
                    <g:set var='count' value='${0 }'/>
                    <g:each var="product" in="${quantityOnHandMap.keySet()}">
                        <g:set var="quantityOnHand" value="${quantityOnHandMap[product]?:0}"/>
                        <g:set var="isAvailable" value="${(quantityOnHand > 0) && (quantityOnHand >= requisitionItem?.totalQuantity()) }"/>


                        <g:set var="rowStyle" value=""/>
                        <g:if test="${product == requisitionItem.product}">
                            <g:set var="rowStyle" value="highlight"/>
                        </g:if>
                        <tr class="${count++%2?'even':'odd'} ${rowStyle}">
                            <td>
                                ${product?.productCode}
                            </td>
                            <td>
                                <g:link controller="inventoryItem" action="showStockCard" id="${product?.id }" target="_blank" >
                                    ${product?.name}
                                </g:link>
                            </td>
                            <td>
                                <ul>
                                    <g:if test='${product.packages}'>
                                        <g:each var="productPackage" in="${product.packages}">
                                            <li>${productPackage?.name} ${productPackage?.uom?.code}/${productPackage?.quantity}</li>
                                        </g:each>
                                    </g:if>
                                    <g:else>
                                        <li>EA/1</li>
                                    </g:else>
                                </ul>
                            </td>
                            <td>
                                ${quantityOnHandMap[product]?:0} EA/1
                            </td>
                            <td>
                                <span class="box-status ${isAvailable?'success':'error'}">
                                    ${isAvailable?"Available":"Unavailable"}
                                </span>
                            </td>

                        </tr>

                    </g:each>
                </table>
            </td>
        </tr>
        <tr class="prop">
            <td class="name">
                <label><g:message code="requisition.verifyAction.label" default="Choose an Action"/></label>
            </td>
            <td class="middle left">

                <div class="button-container">
                    <g:if test="${requisitionItem.canUndoChanges()}">
                        <div class="button-group">
                            <g:remoteLink controller="requisition" action="undoChanges" id="${requisitionItem?.requisition?.id }"
                                          onFailure="alert('An error has occurred.  Please contact your system administrator (${requisition.requestNumber}).')"
                                          params="['requisitionItem.id':requisitionItem?.id, actionType:'show']"
                                          update="requisitionItems" class="button icon reload">
                                <warehouse:message code="default.button.undo.label" default="Undo changes"/>
                            </g:remoteLink>
                        </div>
                    </g:if>
                    <g:else>
                        <div class="">
                            <g:remoteLink controller="requisition" action="approveQuantity" id="${requisitionItem?.requisition?.id }"
                                          onFailure="alert('An error has occurred.  Please contact your system administrator (${requisition.requestNumber}).')"
                                          params="['requisitionItem.id':requisitionItem?.id, actionType:'show']" update="requisitionItems" class="button icon add ${requisitionItem.canApproveQuantity()?'':'disabled'} ${shouldApprove?'primary':''}">
                                ${warehouse.message(code:'verify.button.approve.label', default: 'Approve item') }
                            </g:remoteLink>
                            <g:remoteLink controller="requisition" action="editRequisitionItem" id="${requisitionItem?.requisition?.id }"
                                          onFailure="alert('An error has occurred.  Please contact your system administrator (${requisition.requestNumber}).')"
                                          params="['requisitionItem.id':requisitionItem?.id, actionType:'change']" update="requisitionItems" class="button icon edit ${requisitionItem.canChangeQuantity()?'':'disabled'} ${shouldChange?'primary':''}">
                                <warehouse:message code="verify.button.change.label" default="Change quantity"/>
                            </g:remoteLink>
                            <g:remoteLink controller="requisition" action="editRequisitionItem" id="${requisitionItem?.requisition?.id }"
                                          onFailure="alert('An error has occurred.  Please contact your system administrator (${requisition.requestNumber}).')"
                                          params="['requisitionItem.id':requisitionItem?.id, actionType:'substitute']" update="requisitionItems" class="button icon edit ${requisitionItem.canChooseSubstitute()?'':'disabled'} ${shouldSubstitute?'primary':''}">
                                <warehouse:message code="verify.button.substitute.label" default="Substitute product"/>
                            </g:remoteLink>
                            <g:remoteLink controller="requisition" action="editRequisitionItem" id="${requisitionItem?.requisition?.id }"
                                          onFailure="alert('An error has occurred.  Please contact your system administrator (${requisition.requestNumber}).')"
                                          params="['requisitionItem.id':requisitionItem?.id, actionType:'cancel']" update="requisitionItems" class="button icon trash ${requisitionItem.canCancelQuantity()?'':'disabled'} ${shouldCancel?'primary':''}">
                                <warehouse:message code="verify.button.cancel.label" default="Cancel item"/>
                            </g:remoteLink>
                        </div>
                    </g:else>
                </div>
            </td>
        </tr>
    </table>
</div>
</g:if>
<g:if test="${params.actionType=='change'}">
    <div class="box">
        <h2><warehouse:message code="requisitionItem.changeQuantity.label" default="Change quantity"/></h2>
        <g:formRemote id="changeQuantityForm" name="changeQuantityForm"
                      onFailure="alert('failure')"
                      url="[controller: 'requisition', action:'saveRequisitionItem']"
                      update="requisitionItems" >

            <g:hiddenField name="id" value="${requisitionItem?.requisition?.id }"/>
            <g:hiddenField name="requisitionItem.id" value="${requisitionItem?.id }"/>
            <g:hiddenField name="actionType" value="${params.actionType }"/>
            <g:hiddenField name="product.id" value="${requisitionItem?.product?.id}"/>
            <table>
                <tr class="prop">
                    <td class="middle right name">
                        <label><warehouse:message code="requisitionItem.status.label"/></label>
                    </td>
                    <td class="middle">
                        <g:if test="${requisitionItem?.isSubstituted()}">
                            <img src="${resource(dir:'images/icons/silk',file:'arrow_switch.png')}"/>
                        </g:if>
                        <g:elseif test="${requisitionItem?.isSubstitution()}">
                            <img src="${resource(dir:'images/icons',file:'indent.gif')}"/>
                        </g:elseif>
                        <g:elseif test="${requisitionItem?.isChanged()}">
                            <img src="${resource(dir:'images/icons/silk',file:'pencil.png')}"/>
                        </g:elseif>
                        <g:elseif test="${requisitionItem?.isPending()}">
                            <img src="${resource(dir:'images/icons/silk',file:'hourglass.png')}"/>
                        </g:elseif>
                        <g:elseif test="${requisitionItem?.isCanceled()}">
                            <img src="${resource(dir:'images/icons/silk',file:'decline.png')}"/>
                        </g:elseif>
                        <g:elseif test="${requisitionItem?.isApproved()||requisitionItem?.isCompleted()}">
                            <img src="${resource(dir:'images/icons/silk',file:'accept.png')}"/>
                        </g:elseif>
                        <format:metadata obj="${requisitionItem?.status}"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="middle right name">
                        <label><warehouse:message code="product.label"/></label>
                    </td>
                    <td class="middle">
                        ${requisitionItem?.product?.productCode}
                        <format:product product="${requisitionItem?.product}"/>
                        (${requisitionItem?.product?.unitOfMeasure})
                    </td>
                </tr>

                <tr class="prop">
                    <td class="middle right name">
                        <label><warehouse:message code="requisitionItem.quantity.label"/></label>
                    </td>
                    <td class="middle">
                        <g:textField id="quantity" name="quantity" value="${requisitionItem?.quantity}" class="text" size="5"/>
                        EA/1
                    </td>
                </tr>

                <tr class="prop">
                    <td class="middle right name">
                        <label><warehouse:message code="requisitionItem.cancelReasonCode.label" default="Reason code"/></label>
                    </td>
                    <td class="middle">

                        <g:selectChangeQuantityReasonCode
                                name="reasonCode"
                                class="remote-chzn-select-deselect"
                                style="width:450px;"
                                data-placeholder="Choose a reason code ..."
                                noSelection="['':'']"
                                value="${requisitionItem?.cancelReasonCode }"/>

                    </td>
                </tr>
                <tr class="prop">
                    <td class="top right name">
                        <label style="display: block;"><warehouse:message code="requisitionItem.comments.label" default="Comments"/></label>
                    </td>
                    <td>
                        <g:textArea name="comments" value="${requisitionItem.cancelComments}"
                                    cols="80" rows="5" class="text"
                                    placeholder="${warehouse.message(code:'requisitionItem.comments.placeholder', default:'Enter any additional information')}"/>

                    </td>

                </tr>
                <tr class="prop">
                    <td class="top right name">
                        <label><warehouse:message code="requisitionItem.availability.label" default="Availability"/></label>
                    </td>
                    <td class="middle">
                        <table style="width:auto;" class="box">
                            <tr>
                                <th><warehouse:message code="product.productCode.label"/></th>
                                <th><warehouse:message code="product.label"/></th>
                                <th><warehouse:message code="product.productPackages.label" default="Package sizes"/></th>
                                <th><warehouse:message code="inventoryItem.quantityOnHand.label"/></th>
                                <th><warehouse:message code="inventoryItem.status.label" default="Status"/></th>
                            </tr>
                            <g:set var='count' value='${0 }'/>
                            <g:each var="product" in="${quantityOnHandMap.keySet()}">
                                <g:set var="quantityOnHand" value="${quantityOnHandMap[product]?:0}"/>
                                <g:set var="isAvailable" value="${(quantityOnHand > 0) && (quantityOnHand >= requisitionItem?.totalQuantity()) }"/>


                                <g:set var="rowStyle" value=""/>
                                <g:if test="${product == requisitionItem.product}">
                                    <g:set var="rowStyle" value="highlight"/>
                                </g:if>
                                <tr class="${count++%2?'even':'odd'} ${rowStyle}">
                                    <td>
                                        ${product?.productCode}
                                    </td>
                                    <td>
                                        <g:link controller="inventoryItem" action="showStockCard" id="${product?.id }" target="_blank" >
                                            ${product?.name}
                                        </g:link>
                                    </td>
                                    <td>
                                        <ul>
                                            <g:if test='${product.packages}'>
                                                <g:each var="productPackage" in="${product.packages}">
                                                    <li>${productPackage?.name} ${productPackage?.uom?.code}/${productPackage?.quantity}</li>
                                                </g:each>
                                            </g:if>
                                            <g:else>
                                                <li>EA/1</li>
                                            </g:else>
                                        </ul>
                                    </td>
                                    <td>
                                        ${quantityOnHandMap[product]?:0} EA/1
                                    </td>
                                    <td>
                                        <span class="box-status ${isAvailable?'success':'error'}">
                                            ${isAvailable?"Available":"Unavailable"}
                                        </span>
                                    </td>

                                </tr>

                            </g:each>
                        </table>
                    </td>
                </tr>


                <tr class="prop">
                    <td></td>
                    <td class="middle left">
                        <div class="button-container">
                        <button class="button icon approve">
                            ${warehouse.message(code:'default.button.save.label') }
                        </button>

                        <g:remoteLink controller="requisition" action="editRequisitionItem" id="${requisitionItem?.requisition?.id }"
                                      class="button icon trash"
                                      onFailure="alert('An error has occurred.  Please contact your system administrator (${requisition.requestNumber}).')"
                                      params="['requisitionItem.id':requisitionItem?.id, actionType:'show']" update="requisitionItems">
                            <warehouse:message code="default.button.discard.label" default="Discard changes"/>
                        </g:remoteLink>
                        </div>

                    </td>
                </tr>
            </table>
        </g:formRemote>
    </div>
</g:if>

<g:if test="${params.actionType=='substitute'}">
    <div class="box">
        <h2><warehouse:message code="requisitionItem.substitution.label" default="Substitution"/></h2>
        <g:formRemote id="substitutionForm" name="substitutionForm"
                      onFailure="alert('failure')"
                      url="[controller: 'requisition', action:'saveRequisitionItem']"
                      update="requisitionItems" >

            <g:hiddenField name="id" value="${requisitionItem?.requisition?.id }"/>
            <g:hiddenField name="requisitionItem.id" value="${requisitionItem?.id }"/>
            <g:hiddenField name="actionType" value="${params.actionType }"/>
            <table>
                <tr class="prop">
                    <td class="middle right name">
                        <label><warehouse:message code="requisitionItem.status.label"/></label>
                    </td>
                    <td class="middle">
                        <g:if test="${requisitionItem?.isSubstituted()}">
                            <img src="${resource(dir:'images/icons/silk',file:'arrow_switch.png')}"/>
                        </g:if>
                        <g:elseif test="${requisitionItem?.isSubstitution()}">
                            <img src="${resource(dir:'images/icons',file:'indent.gif')}"/>
                        </g:elseif>
                        <g:elseif test="${requisitionItem?.isChanged()}">
                            <img src="${resource(dir:'images/icons/silk',file:'pencil.png')}"/>
                        </g:elseif>
                        <g:elseif test="${requisitionItem?.isPending()}">
                            <img src="${resource(dir:'images/icons/silk',file:'hourglass.png')}"/>
                        </g:elseif>
                        <g:elseif test="${requisitionItem?.isCanceled()}">
                            <img src="${resource(dir:'images/icons/silk',file:'decline.png')}"/>
                        </g:elseif>
                        <g:elseif test="${requisitionItem?.isApproved()||requisitionItem?.isCompleted()}">
                            <img src="${resource(dir:'images/icons/silk',file:'accept.png')}"/>
                        </g:elseif>
                        <format:metadata obj="${requisitionItem?.status}"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="middle right name">
                        <label><warehouse:message code="product.label" default="Product"/></label>
                    </td>
                    <td class="middle">
                        ${requisitionItem?.product?.productCode} ${requisitionItem?.product?.name}
                    </td>
                </tr>

                <g:if test="${requisitionItem?.product?.genericProduct?.description}">
                    <tr class="prop">
                        <td class="top right name">
                            <label><g:message code="productGroup.description.label" default="Description"/></label>
                        </td>
                        <td class="middle">
                            <h1 class="error">
                                ${requisitionItem?.product?.genericProduct?.description}
                            </h1>
                        </td>
                    </tr>
                </g:if>



                <tr class="prop">
                    <td class="top right name">
                        <label><warehouse:message code="requisitionItem.substitution.label" default="Substitution"/></label>
                    </td>
                    <td class="middle">
                        <table style="width:auto;" class="box">
                            <tr>
                                <th><warehouse:message code="product.productCode.label"/></th>
                                <th><warehouse:message code="product.label"/></th>
                                <th><warehouse:message code="product.productPackages.label" default="Package sizes"/></th>
                                <th><warehouse:message code="inventoryItem.quantityOnHand.label"/></th>
                                <th><warehouse:message code="inventoryItem.status.label" default="Status"/></th>
                            </tr>
                            <g:set var='count' value='${0 }'/>
                            <g:each in="${quantityOnHandMap.keySet()}" var="product" status="i">
                                <g:set var="quantityOnHand" value="${quantityOnHandMap[product]?:0}"/>
                                <g:set var="isAvailable" value="${(quantityOnHand > 0) && (quantityOnHand >= requisitionItem?.totalQuantity()) }"/>
                                <g:set var="rowStyle" value=""/>
                                <g:if test="${product == requisitionItem.product}">
                                    <g:set var="rowStyle" value="highlight"/>
                                </g:if>
                                <tr class="${count++%2?'even':'odd'} ${rowStyle}">
                                    <td>
                                        <g:radio name="product.id" value="${product.id}" checked="${false }" />
                                    </td>
                                    <td>
                                        ${product.productCode} ${product.name}
                                    </td>
                                    <td>
                                        <ul>
                                            <g:if test='${product.packages}'>
                                                <g:each var="productPackage" in="${product.packages}">
                                                    <li>${productPackage?.name} ${productPackage?.uom?.code}/${productPackage?.quantity}</li>
                                                </g:each>
                                            </g:if>
                                            <g:else>
                                                <li>EA/1</li>
                                            </g:else>
                                        </ul>
                                    </td>
                                    <td>
                                        ${quantityOnHandMap[product]?:0} EA/1
                                    </td>
                                    <td>
                                        <span class="box-status ${isAvailable?'success':'error'}">
                                            ${isAvailable?"Available":"Unavailable"}
                                        </span>
                                    </td>
                                </tr>
                            </g:each>
                            <tr>
                                <td>
                                    <g:radio name="product.id" value="other" checked="${false }" />
                                </td>
                                <td colspan="4">
                                    <g:autoSuggest id="otherSubstitute" name="otherSubstitute" jsonUrl="${request.contextPath }/json/findProductByName"
                                                   width="600" styleClass="text" placeholder="Or enter a custom substitution ..."/>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="middle right name">
                        <label><warehouse:message code="requisitionItem.quantity.label"/></label>
                    </td>
                    <td class="middle">
                        <g:textField id="quantity" name="quantity" value="${requisitionItem?.quantity}" class="text" size="5"/>
                        EA/1
                    </td>
                </tr>
                <tr class="prop">
                    <td class="middle right name">
                        <label><warehouse:message code="requisitionItem.cancelReasonCode.label" default="Reason code"/></label>
                    </td>
                    <td class="middle">
                        <g:selectSubstitutionReasonCode
                                name="reasonCode"
                                class="remote-chzn-select-deselect"
                                style="width:450px;"
                                data-placeholder="Choose a reason code ..."
                                noSelection="['':'']"
                                value="${requisitionItem?.cancelReasonCode }"/>

                    </td>
                </tr>
                <tr class="prop">
                    <td class="top right name">
                        <label style="display: block;"><warehouse:message code="requisitionItem.comments.label" default="Comments"/></label>
                    </td>
                    <td>
                        <g:textArea name="comments" value="${requisitionItem.cancelComments}"
                                    cols="80" rows="5" class="text"
                                    placeholder="${warehouse.message(code:'requisitionItem.comments.placeholder', default:'Any additional information')}"/>

                    </td>
                </tr>

                <tr class="prop">
                    <td></td>
                    <td class="middle left">
                        <div class="button-container">
                            <g:if test="${requisitionItem?.product?.genericProduct?.description}">
                                <div id="confirmRestrictions" title="Confirmation Required">
                                    <div class="message">${g.message(code:'productGroup.confirmRestrictions.message')}</div>
                                    <div class="error">${requisitionItem?.product?.genericProduct?.description}</div>
                                </div>
                                <script type="text/javascript">
                                    $(document).ready(function() {
                                        $("#confirmRestrictions").dialog({autoOpen: false, modal: true, width: 600});
                                        $("#confirmRestrictionsButton").click(function(event) {
                                            event.preventDefault();
                                            var targetUrl = $(this).attr("href");

                                            $("#confirmRestrictions").dialog({
                                                buttons: {
                                                    "Confirm": function () {
                                                        $("#substitutionForm").submit();
                                                        $(this).dialog("close");
                                                    },
                                                    "Cancel": function () {
                                                        $(this).dialog("close");
                                                    }
                                                }
                                            });

                                            $("#confirmRestrictions").dialog("open");
                                        });
                                    });
                                </script>
                                <g:set var="confirmRestrictions" value="${g.message(code:'productGroup.confirmRestrictions.message')}"/>
                                <button class="button icon approve" id="confirmRestrictionsButton">
                                    ${warehouse.message(code:'default.button.save.label') }
                                </button>
                            </g:if>
                            <g:else>
                                <button class="button icon approve">
                                    ${warehouse.message(code:'default.button.save.label') }
                                </button>
                            </g:else>

                            <g:remoteLink controller="requisition" action="editRequisitionItem" id="${requisitionItem?.requisition?.id }"
                                          class="button icon trash"
                                          onFailure="alert('An error has occurred.  Please contact your system administrator (${requisition.requestNumber}).')"
                                          params="['requisitionItem.id':requisitionItem?.id, actionType:'show']" update="requisitionItems">
                                <warehouse:message code="default.button.discard.label" default="Discard changes"/>
                            </g:remoteLink>
                        </div>

                    </td>
                </tr>
            </table>
        </g:formRemote>
    </div>
</g:if>

<g:if test="${params.actionType=='cancel'}">
    <div class='box'>
        <h2>Cancel requisition item</h2>
        <g:formRemote id="changeQuantityForm" name="changeQuantityForm"
                      onFailure="alert('failure')"
                      url="[controller: 'requisition', action:'saveRequisitionItem']"
                      update="requisitionItems" >

            <g:hiddenField name="id" value="${requisitionItem?.requisition?.id }"/>
            <g:hiddenField name="requisitionItem.id" value="${requisitionItem?.id }"/>
            <g:hiddenField name="actionType" value="${params.actionType }"/>
            <g:hiddenField name="product.id" value="${requisitionItem?.product?.id }"/>
            <g:hiddenField name="quantity" value="${0 }"/>

            <table>
                <tr class="prop">
                    <td class="middle right name">
                        <label><warehouse:message code="requisitionItem.status.label"/></label>
                    </td>
                    <td class="middle">
                        <g:if test="${requisitionItem?.isSubstituted()}">
                            <img src="${resource(dir:'images/icons/silk',file:'arrow_switch.png')}"/>
                        </g:if>
                        <g:elseif test="${requisitionItem?.isSubstitution()}">
                            <img src="${resource(dir:'images/icons',file:'indent.gif')}"/>
                        </g:elseif>
                        <g:elseif test="${requisitionItem?.isChanged()}">
                            <img src="${resource(dir:'images/icons/silk',file:'pencil.png')}"/>
                        </g:elseif>
                        <g:elseif test="${requisitionItem?.isPending()}">
                            <img src="${resource(dir:'images/icons/silk',file:'hourglass.png')}"/>
                        </g:elseif>
                        <g:elseif test="${requisitionItem?.isCanceled()}">
                            <img src="${resource(dir:'images/icons/silk',file:'decline.png')}"/>
                        </g:elseif>
                        <g:elseif test="${requisitionItem?.isApproved()||requisitionItem?.isCompleted()}">
                            <img src="${resource(dir:'images/icons/silk',file:'accept.png')}"/>
                        </g:elseif>
                        <format:metadata obj="${requisitionItem?.status}"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="middle right name">
                        <label><warehouse:message code="product.label"/></label>
                    </td>
                    <td class="middle">
                        ${requisitionItem?.product?.productCode}
                        <format:product product="${requisitionItem?.product}"/>
                        (${requisitionItem?.product?.unitOfMeasure})
                    </td>
                </tr>
                <tr class="prop">
                    <td class="middle right name">
                        <label><warehouse:message code="requisitionItem.quantity.label"/></label>
                    </td>
                    <td class="middle">
                        0 EA/1
                    </td>
                </tr>

                <tr class="prop">
                    <td class="middle right name">
                        <label><warehouse:message code="requisitionItem.cancelReasonCode.label" default="Reason code"/></label>
                    </td>
                    <td class="middle">
                        <g:selectCancelReasonCode
                                name="reasonCode"
                                class="remote-chzn-select-deselect"
                                style="width:450px;"
                                data-placeholder="Choose a reason code ..."
                                noSelection="['':'']"
                                value="${requisitionItem?.cancelReasonCode }"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="top right name">
                        <label style="display: block;"><warehouse:message code="requisitionItem.comments.label" default="Comments"/></label>
                    </td>
                    <td>
                        <g:textArea name="comments" value="${requisitionItem.cancelComments}"
                                    cols="80" rows="5" class="text"
                                    placeholder="${warehouse.message(code:'requisitionItem.comments.placeholder', default:'Any additional information')}"/>

                    </td>

                </tr>
                <tr class="prop">
                    <td></td>
                    <td class="middle left">

                        <button class="button icon approve">
                            ${warehouse.message(code:'default.button.save.label') }
                        </button>
                        <g:remoteLink controller="requisition" action="editRequisitionItem" id="${requisitionItem?.requisition?.id }"
                                      class="button icon trash"
                                      onFailure="alert('An error has occurred.  Please contact your system administrator (${requisition.requestNumber}).')"
                                      params="['requisitionItem.id':requisitionItem?.id, actionType:'show']" update="requisitionItems">
                            <warehouse:message code="default.button.discard.label" default="Discard changes"/>
                        </g:remoteLink>


                    </td>
                </tr>
            </table>
        </g:formRemote>
    </div>
</g:if>
<script type="text/javascript">
$(document).ready(function() {
    $(".remote-chzn-select").chosen();
    $(".remote-chzn-select-deselect").chosen({allow_single_deselect:true});
});
</script>
