<div class="box" id="changeQuantity">
<g:set var="quantityOnHand" value="${quantityOnHandMap[requisitionItem?.product?.id]?:0}"/>
<g:set var="isAvailable" value="${(quantityOnHand > 0) && (quantityOnHand >= requisitionItem?.totalQuantity()) }"/>

<h2>
    <span class="box-status ${isAvailable?'success':'error'}">
        ${isAvailable?"Available":"Unavailable"}
    </span>
                ${requisitionItem?.product?.productCode}
                ${requisitionItem?.product}
                (${requisitionItem.orderIndex+1} / ${requisitionItem.requisition.requisitionItemCount})

                <div class="button-group right" style="margin: 5px;">
                    <g:remoteLink controller="requisition" action="editRequisitionItem" id="${requisitionItem?.requisition?.id }"
                                  params="['requisitionItem.id':requisitionItem?.previous()?.id, actionType:'show']" update="requisitionItems" class="button icon arrowleft">
                        <warehouse:message code="default.button.previous.label" default="Previous"/>
                    </g:remoteLink>
                    <g:remoteLink controller="requisition" action="editRequisitionItem" id="${requisitionItem?.requisition?.id }"
                                  params="['requisitionItem.id':requisitionItem?.next()?.id, actionType:'show']" update="requisitionItems" class="button icon arrowright">
                        <warehouse:message code="default.button.next.label" default="Next"/>
                    </g:remoteLink>
                </div>
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
                <format:metadata obj="${requisitionItem?.status}"/>
            </td>
        </tr>
        <tr class="prop">
            <td class="middle right name">
                <label><warehouse:message code="product.label"/></label>
            </td>
            <td class="middle">
                ${requisitionItem?.product?.productCode}
                ${requisitionItem?.product}
            </td>
        </tr>
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
        <tr class="prop">
            <td class="middle right name">
                <label><warehouse:message code="product.unitOfMeasure.label"/></label>
            </td>
            <td class="middle">
                ${requisitionItem?.product?.unitOfMeasure}
            </td>
        </tr>
    </table>
    <table>
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
                <label><warehouse:message code="requisitionItem.quantityOnHand.label" default="On-hand"/></label>
            </td>
            <td class="middle">
                <g:if test='${requisitionItem.product.productGroups}'>
                    <table style="width:auto;" class="box">
                        <tr>
                            <th><warehouse:message code="product.label"/></th>
                            <th><warehouse:message code="product.productPackages.label" default="Package sizes"/></th>
                            <th><warehouse:message code="inventoryItem.quantityOnHand.label"/></th>
                        </tr>
                        <g:set var='count' value='${0 }'/>
                        <g:each var="productGroup" in="${requisitionItem.product.productGroups}">
                            <g:each var="product" in="${productGroup.products}">
                                <tr class="${count++%2?'even':'odd'}">
                                    <td>${product?.productCode} ${product?.name}</td>
                                    <td>
                                        <ul>
                                            <g:each var="productPackage" in="${product.packages}">
                                                <li>${productPackage.name} ${productPackage.uom.code}/${productPackage.quantity}</li>
                                            </g:each>
                                        </ul>
                                    </td>
                                    <td>${quantityOnHandMap[product?.id]?:0} EA/1</td>

                                </tr>
                            </g:each>
                        </g:each>
                    </table>
                </g:if>
                <g:else>
                    ${quantityOnHand} EA/1

                </g:else>
            </td>
        </tr>
        <tr class="prop">
            <td>

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


                        <div class="button-group">
                            <g:if test="${requisitionItem.canUndoChanges()}">
                                <g:remoteLink controller="requisition" action="undoChanges" id="${requisitionItem?.requisition?.id }"
                                              params="['requisitionItem.id':requisitionItem?.id, actionType:'show']" update="requisitionItems" class="button icon trash">
                                    <warehouse:message code="default.button.undo.label" default="Undo"/>
                                </g:remoteLink>
                            </g:if>
                            <g:else>
                                <button class="button icon approve ${requisitionItem.canApproveQuantity()?'':'disabled'}">
                                    ${warehouse.message(code:'default.button.approve.label', default: 'Approve') }
                                </button>
                                <g:remoteLink controller="requisition" action="editRequisitionItem" id="${requisitionItem?.requisition?.id }"
                                              params="['requisitionItem.id':requisitionItem?.id, actionType:'change']" update="requisitionItems" class="button icon edit ${requisitionItem.canChangeQuantity()?'':'disabled'}">
                                    <warehouse:message code="default.button.change.label" default="Change"/>
                                </g:remoteLink>
                                <g:remoteLink controller="requisition" action="editRequisitionItem" id="${requisitionItem?.requisition?.id }"
                                              params="['requisitionItem.id':requisitionItem?.id, actionType:'cancel']" update="requisitionItems" class="button icon trash ${requisitionItem.canCancelQuantity()?'':'disabled'}">
                                    <warehouse:message code="default.button.cancel.label" default="Cancel"/>
                                </g:remoteLink>
                            </g:else>
                        </div>

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
                        <div class="button-group">
                            <g:remoteLink controller="requisition" action="editRequisitionItem" id="${requisitionItem?.requisition?.id }"
                                          params="['requisitionItem.id':requisitionItem?.id, actionType:'show']" update="requisitionItems" class="button icon reload">
                                <warehouse:message code="default.button.refresh.label" default="Refresh"/>
                            </g:remoteLink>
                            <g:remoteLink controller="requisition" action="showRequisitionItems" id="${requisitionItem?.requisition?.id }" update="requisitionItems" class="button icon log">
                                <warehouse:message code="default.button.list.label" default="List"/>
                            </g:remoteLink>
                        </div>
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

