
<div class="button-container">
    <div class="button-group">

<g:remoteLink controller="requisition" action="showRequisitionItems" id="${requisitionItem?.requisition?.id }" update="requisitionItems" class="button icon log">
    <warehouse:message code="default.button.list.label" default="Back to verify"/>
</g:remoteLink>
<g:remoteLink controller="requisition" action="editRequisitionItem" id="${requisitionItem?.requisition?.id }"
              params="['requisitionItem.id':requisitionItem?.id, actionType:'show']" update="requisitionItems" class="button icon reload">
    <warehouse:message code="default.button.refresh.label" default="Refresh"/>
</g:remoteLink>

</div>
<div class="box" id="changeQuantity">

<h2>
    ${requisitionItem?.product?.productCode}
    ${requisitionItem?.product}
    (${requisitionItem.orderIndex+1} / ${requisitionItem.requisition.requisitionItemCount})



</h2>

    <table>
        <%--
        <tr>
            <td colspan="2">
                <table>
                    <tr>
                        <td class="left">
                            <g:remoteLink controller="requisition" action="editRequisitionItem" id="${requisitionItem?.requisition?.id }"
                                          params="['requisitionItem.id':requisitionItem?.id, actionType:'previousItem']" update="requisitionItems" class="button icon arrowleft">
                                <warehouse:message code="default.button.previous.label"/>
                            </g:remoteLink>

                        </td>
                        <td class="center">
                            <div class="title">
                                ${requisitionItem.orderIndex+1} / ${requisitionItem.requisition.requisitionItemCount}
                            </div>

                        </td>
                        <td class="right">
                            <g:remoteLink controller="requisition" action="editRequisitionItem" id="${requisitionItem?.requisition?.id }"
                                          params="['requisitionItem.id':requisitionItem?.id, actionType:'nextItem']" update="requisitionItems" class="button icon arrowright">
                                <warehouse:message code="default.button.next.label"/>
                            </g:remoteLink>

                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        --%>

        <tr class="prop">
            <td class="middle right name">
                <label><warehouse:message code="requisitionItem.orderIndex.label" default="Item #"/></label>
            </td>
            <td class="middle">
                <format:metadata obj="${requisitionItem?.orderIndex+1}"/>
            </td>
        </tr>
        <tr class="prop">
            <td class="middle right name">
                <label><warehouse:message code="requisitionItem.status.label"/></label>
            </td>
            <td class="middle">
                <span class="tag">
                    <format:metadata obj="${requisitionItem?.status}"/>
                </span>
            </td>
        </tr>
        <tr class="prop">
            <td class="middle right name">
                <label><warehouse:message code="product.label"/></label>
            </td>
            <td class="middle">
                ${requisitionItem?.product?.productCode}
                ${requisitionItem?.product}
                (${requisitionItem?.product?.unitOfMeasure})



            </td>
        </tr>
        <%--
        <tr class="prop">
            <td class="middle name right">
                <label><warehouse:message code="product.package.label" default="Package size(s)"/></label>
            </td>
            <td>
                <g:if test="${!requisitionItem.product.packages}">
                    <li>EA/1</li>
                </g:if>
                <g:each var="productPackage" in="${requisitionItem.product.packages}">
                    <li>${productPackage.name} ${productPackage.uom.code}/${productPackage.quantity}</li>
                </g:each>
            </td>
        </tr>
        --%>
        <tr class="prop">
            <td class="middle right name">
                <label><warehouse:message code="requisitionItem.quantityRequested.label"/></label>
            </td>
            <td class="middle">
                ${requisitionItem?.quantity?:0} EA/1
            </td>
        </tr>
        <tr class="prop">
            <td class="middle right name">
                <label><warehouse:message code="requisitionItem.quantityCanceled.label"/></label>
            </td>
            <td class="middle">
                ${requisitionItem?.quantityCanceled?:0} EA/1
            </td>
        </tr>
        <tr class="prop">
            <td class="middle right name">
                <label><warehouse:message code="requisitionItem.quantityApproved.label"/></label>
            </td>
            <td class="middle">
                ${requisitionItem?.quantityApproved?:0} EA/1
            </td>
        </tr>
        <tr class="prop">
            <td class="top right name">
                <label><warehouse:message code="requisitionItem.substitutions.label" default="Substitutions"/></label>
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
                            <g:set var="rowStyle" value="canceled"/>
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
                                                <li>${productPackage.name} ${productPackage.uom.code}/${productPackage.quantity}</li>
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
            <td>
                <div class="button-group">

                    <g:remoteLink controller="requisition" action="editRequisitionItem" id="${requisitionItem?.requisition?.id }"
                                  params="['requisitionItem.id':requisitionItem?.previous()?.id, actionType:'show']" update="requisitionItems" class="button icon arrowleft">
                        <warehouse:message code="default.button.previous.label" default="Previous"/>
                    </g:remoteLink>
                    <g:remoteLink controller="requisition" action="editRequisitionItem" id="${requisitionItem?.requisition?.id }"
                                  params="['requisitionItem.id':requisitionItem?.next()?.id, actionType:'show']" update="requisitionItems" class="button icon arrowright">
                        <warehouse:message code="default.button.next.label" default="Next"/>
                    </g:remoteLink>
                </div>


            </td>
            <td class="middle left">

                <g:formRemote id="changeQuantityForm" name="changeQuantityForm"
                              onFailure="alert('failure')"
                              url="[controller: 'requisition', action:'saveRequisitionItem']"
                              update="requisitionItems" >

                    <g:hiddenField name="id" value="${requisitionItem?.requisition?.id }"/>
                    <g:hiddenField name="requisitionItem.id" value="${requisitionItem?.id }"/>
                    <g:hiddenField name="actionType" value="${params.actionType }"/>
                    <g:hiddenField name="quantity" value="${requisitionItem?.quantity}" />
                    <div class="button-container">



                            <g:if test="${requisitionItem.canUndoChanges()}">
                                <div class="button-group">
                                    <g:remoteLink controller="requisition" action="undoChanges" id="${requisitionItem?.requisition?.id }"
                                                  params="['requisitionItem.id':requisitionItem?.id, actionType:'show']" update="requisitionItems" class="button icon trash">
                                        <warehouse:message code="default.button.undo.label" default="Undo"/>
                                    </g:remoteLink>
                                </div>
                            </g:if>
                            <g:else>
                                <div class="button-group">
                                    <button class="button icon approve ${requisitionItem.canApproveQuantity()?'':'disabled'}">
                                        ${warehouse.message(code:'verify.button.approve.label', default: 'Approve item') }
                                    </button>
                                </div>
                                <div class="button-group">
                                    <g:remoteLink controller="requisition" action="editRequisitionItem" id="${requisitionItem?.requisition?.id }"
                                                  params="['requisitionItem.id':requisitionItem?.id, actionType:'change']" update="requisitionItems" class="button icon add ${requisitionItem.canChangeQuantity()?'':'disabled'}">
                                        <warehouse:message code="verify.button.change.label" default="Change quantity"/>
                                    </g:remoteLink>
                                    <g:remoteLink controller="requisition" action="editRequisitionItem" id="${requisitionItem?.requisition?.id }"
                                                  params="['requisitionItem.id':requisitionItem?.id, actionType:'change']" update="requisitionItems" class="button icon edit ${requisitionItem.canChangeQuantity()?'':'disabled'}">
                                        <warehouse:message code="verify.button.substitute.label" default="Substitute product"/>
                                    </g:remoteLink>
                                    <g:remoteLink controller="requisition" action="editRequisitionItem" id="${requisitionItem?.requisition?.id }"
                                                  params="['requisitionItem.id':requisitionItem?.id, actionType:'cancel']" update="requisitionItems" class="button icon trash ${requisitionItem.canCancelQuantity()?'':'disabled'}">
                                        <warehouse:message code="verify.button.cancel.label" default="Cancel item"/>
                                    </g:remoteLink>
                                </div>
                            </g:else>

                        <%--
                        <div class="button-group">
                            <g:remoteLink controller="requisition" action="editRequisitionItem" id="${requisitionItem?.requisition?.id }"
                                          params="['requisitionItem.id':requisitionItem?.previous()?.id, actionType:'show']" update="requisitionItems" class="button icon arrowleft">
                                <warehouse:message code="default.button.previous.label" default="Previous"/>
                            </g:remoteLink>
                            <g:remoteLink controller="requisition" action="editRequisitionItem" id="${requisitionItem?.requisition?.id }"
                                          params="['requisitionItem.id':requisitionItem?.next()?.id, actionType:'show']" update="requisitionItems" class="button icon arrowright">
                                <warehouse:message code="default.button.next.label" default="Next"/>
                            </g:remoteLink>
                        </div>
                        --%>
                    </div>



                </g:formRemote>

            </td>

        </tr>

    </table>
</div>




<g:hasErrors bean="${requisitionItem}">
    <div class="errors">
        <g:renderErrors bean="${requisitionItem}" as="list" />
    </div>
</g:hasErrors>

<g:if test="${params.actionType=='change'}">
    <div class="box">
        <h2>Edit requisition item</h2>
        <%--onSuccess="alert('success')" onComplete="alert('complete')" onFailure="alert('failure')"--%>
        <g:formRemote id="changeQuantityForm" name="changeQuantityForm"
                      onFailure="alert('failure')"
                      url="[controller: 'requisition', action:'saveRequisitionItem']"
                      update="requisitionItems" >

            <g:hiddenField name="id" value="${requisitionItem?.requisition?.id }"/>
            <g:hiddenField name="requisitionItem.id" value="${requisitionItem?.id }"/>
            <g:hiddenField name="actionType" value="${params.actionType }"/>

            <table>
                <tr class="prop">
                    <td class="middle right name">
                        <label><warehouse:message code="product.label" default="Product"/></label>
                    </td>
                    <td class="middle">

                        <g:autoSuggest id="substitution" name="substitution" jsonUrl="${request.contextPath }/json/findProductByName"
                                       width="500" styleClass="text" valueId="${requisitionItem?.product?.id}" valueName="${requisitionItem?.product?.productCode + ' ' + requisitionItem?.product?.name}"
                                       placeholder="Product title (e.g. Ibuprofen, 200 mg, tablet)"/>


                    </td>
                </tr>
                <tr class="prop">
                    <td class="middle right name">
                        <label><warehouse:message code="requisitionItem.quantityRequested.label"/></label>
                    </td>
                    <td class="middle">
                        <g:textField id="quantity" name="quantity" value="${requisitionItem?.quantity}" class="text" size="5"/>
                        EA/1
                        <%--
                        <g:selectUnitOfMeasure name="productPackage.id" product="${requisitionItem?.product}" class="chzn-select" style="width:300px;"/>
                        --%>
                    </td>
                </tr>
                <tr class="prop">
                    <td class="middle right name">
                        <label><warehouse:message code="requisitionItem.cancelReasonCode.label" default="Reason code"/></label>
                    </td>
                    <td class="middle">
                        <g:selectChangeQuantityReasonCode
                                name="reasonCode"
                                class="chzn-select"
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
                                      params="['requisitionItem.id':requisitionItem?.id, actionType:'show']" update="requisitionItems">
                            <warehouse:message code="default.button.cancel.label" default="Cancel"/>
                        </g:remoteLink>

                    </td>
                </tr>
            </table>
        </g:formRemote>
    </div>
</g:if>


<g:if test="${params.actionType=='cancel'}">
    <div class='box'>
        <h2>Cancel requisition item</h2>
        <%--onSuccess="alert('success')" onComplete="alert('complete')" onFailure="alert('failure')"--%>
        <g:formRemote id="changeQuantityForm" name="changeQuantityForm"
                      onFailure="alert('failure')"
                      url="[controller: 'requisition', action:'saveRequisitionItem']"
                      update="requisitionItems" >

            <g:hiddenField name="id" value="${requisitionItem?.requisition?.id }"/>
            <g:hiddenField name="requisitionItem.id" value="${requisitionItem?.id }"/>
            <g:hiddenField name="actionType" value="${params.actionType }"/>
            <g:hiddenField name="quantity" value="${0 }"/>

            <table>
                <tr class="prop">
                    <td class="middle right name">
                        <label><warehouse:message code="requisitionItem.cancelReasonCode.label" default="Reason code"/></label>
                    </td>
                    <td class="middle">
                        <g:selectChangeQuantityReasonCode
                                name="reasonCode"
                                class="chzn-select"
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
                                      params="['requisitionItem.id':requisitionItem?.id, actionType:'show']" update="requisitionItems">
                            <warehouse:message code="default.button.cancel.label" default="Cancel"/>
                        </g:remoteLink>

                    </td>
                </tr>
            </table>
        </g:formRemote>
    </div>
</g:if>

