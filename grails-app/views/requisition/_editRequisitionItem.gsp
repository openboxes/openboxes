<div class="box" id="changeQuantity">
    <%--
    <h2>${warehouse.message(code:'requisitionItem.changeQuantity.label', default:'Change quantity')}</h2>
    --%>


        <h2>Current</h2>
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
                <td class="middle right name">
                    <label><warehouse:message code="requisitionItem.quantity.label"/></label>
                </td>
                <td class="middle">
                    ${requisitionItem?.quantity} EA/1
                </td>
            </tr>
            <g:if test='${requisitionItem.product.productGroups}'>
                <tr class="prop">
                    <td class="top right name">
                        <label><warehouse:message code="product.availablility.label" default="Availability"/></label>
                    </td>
                    <td class="middle">
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
                    </td>
                </tr>

            </g:if>
            <%--
            <tr class="prop">
                <td class="middle right name">
                    <label><warehouse:message code="requisitionItem.quantityApproved.label"/></label>
                </td>
                <td class="middle">
                    ${requisitionItem.quantityApproved} EA/1
                </td>
            </tr>
            --%>
        </table>
    </div>

    <br/>

    <g:hasErrors bean="${requisitionItem}">
        <div class="errors">
            <g:renderErrors bean="${requisitionItem}" as="list" />
        </div>
    </g:hasErrors>
    <div class="box">
        <h2>New</h2>

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
                    <label><warehouse:message code="product.substitution.label" default="Substitution"/></label>
                </td>
                <td class="middle">

                    <g:autoSuggest id="substitution" name="substitution" jsonUrl="${request.contextPath }/json/findProductByName"
                                   width="500" styleClass="text" valueId="${requisitionItem?.product?.id}" valueName="${requisitionItem?.product?.productCode + ' ' + requisitionItem?.product?.name}"
                                   placeholder="Product title (e.g. Ibuprofen, 200 mg, tablet)"/>


                </td>
            </tr>
            <tr class="prop">
                <td class="middle right name">
                    <label><warehouse:message code="requisitionItem.quantity.label"/></label>
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
                <td class="middle center" colspan="2">
                    <button class="button icon approve">
                        ${warehouse.message(code:'default.button.save.label') }
                    </button>



                    <g:remoteLink controller="requisition" action="showRequisitionItems" update="requisitionItems" class="button icon trash"
                                  id="${requisitionItem.requisition.id}">Cancel</g:remoteLink>


                    <g:remoteLink controller="requisition" action="editRequisitionItem" id="${requisitionItem?.requisition?.id }"
                                  params="['requisitionItem.id':requisitionItem?.id, actionType:'nextItem']" update="requisitionItems" class="button icon reload">
                        <warehouse:message code="default.button.reload.label" default="Reload"/>
                    </g:remoteLink>

                </td>
            </tr>
        </table>
    </g:formRemote>

</div>

