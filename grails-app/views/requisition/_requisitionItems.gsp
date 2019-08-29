<div id="requisitionItems">
<table class="zebra">
<thead>
<tr class="odd">
    <th>

    </th>
    <th class='center'>
        <warehouse:message code="default.actions.label"/>
    </th>
    <th>
        <warehouse:message code="requisitionItem.status.label" default="Status" />
    </th>
    <th>
        <warehouse:message code="product.productCode.label" />
    </th>
    <th>
        <warehouse:message code="requisitionItem.product.label" />
    </th>
    <th>
        <warehouse:message code="product.unitOfMeasure.label" />
    </th>
    <th class="center">
        <warehouse:message code="requisitionItem.reasonCode.label" default="Reason Code" />
    </th>
    <th class="center ">
        <warehouse:message code="requisitionItem.quantityRequested.label" />
    </th>
    <th class="center">
        <warehouse:message code="requisitionItem.quantityAvailable.label" default="Quantity available" />
    </th>
    <th class="center">
        <warehouse:message code="requisitionItem.orderIndex.label" default="Sort order" />
    </th>
</tr>
</thead>
<tbody>
<g:if test="${requisition?.requisitionItems?.size() == 0}">
    <tr class="prop odd">
        <td colspan="8" class="center"><warehouse:message
                code="requisition.noRequisitionItems.message" /></td>
    </tr>
</g:if>
<g:set var="count" value="${0}"/>

<g:set var="startTime" value="${System.currentTimeMillis()}"/>
<g:each var="requisitionItem" in="${requisition?.requisitionItems}" status="i">

<g:set var="requisitionStartTime" value="${System.currentTimeMillis()}"/>

<g:if test="${!requisitionItem.parentRequisitionItem}">
<g:set var="selected" value="${requisitionItem == selectedRequisitionItem}"/>
<g:set var="quantityOnHand" value="${quantityOnHandMap[requisitionItem?.product?.id]} "/>
<g:set var="substitution" value="${requisitionItem?.substitution}"/>
<g:set var="quantityOnHandForSubstitution" value="${quantityOnHandMap[substitution?.product?.id]} "/>
<g:set var="quantityRemaining" value="${(requisitionItem?.quantity?:0)-(requisitionItem?.calculateQuantityPicked()?:0)}" />
<g:set var="isCanceled" value="${requisitionItem?.isCanceled()}"/>
<g:set var="isChanged" value="${requisitionItem?.isChanged()}"/>
<g:set var="hasSubstitution" value="${requisitionItem?.hasSubstitution()}"/>
<g:set var="quantityOnHand" value="${quantityOnHand.toInteger()}"/>
<g:set var="isAvailable" value="${(quantityOnHand > 0) && (quantityOnHand >= requisitionItem?.totalQuantity()) }"/>
<g:set var="isAvailableForSubstitution" value="${hasSubstitution && (quantityOnHandForSubstitution > 0) && (quantityOnHandForSubstitution >= requisitionItem?.substitution?.totalQuantity()) }"/>
<tr class="prop ${(count++ % 2) == 0 ? 'odd' : 'even'} ${!selectedRequisitionItem?'':selected?'selected':'unselected'}">

<td class="middle left">
    <div class="action-menu">
        <span class="action-btn">
            <g:if test="${requisitionItem?.isCanceled()}">
                <img src="${resource(dir:'images/icons/silk', file: 'decline.png')}"/>
            </g:if>
            <g:elseif test="${requisitionItem?.isSubstituted()}">
                <img src="${resource(dir:'images/icons/silk', file: 'arrow_switch.png')}"/>
            </g:elseif>
            <g:elseif test="${requisitionItem?.isApproved()}">
                <img src="${resource(dir:'images/icons/silk', file: 'accept.png')}"/>
            </g:elseif>
            <g:elseif test="${requisitionItem?.isChanged()}">
                <img src="${resource(dir:'images/icons/silk', file: 'pencil.png')}"/>
            </g:elseif>
            <g:elseif test="${requisitionItem?.isPending()}">
                <img src="${resource(dir:'images/icons/silk', file: 'hourglass.png')}"/>
            </g:elseif>
            <g:else>
                <img src="${resource(dir:'images/icons/silk', file: 'information.png')}"/>
            </g:else>
        </span>
        <div class="actions">
            <div class="box" style="width:450px;">
                <div>
                    <label>Requisition item:</label>
                    ${requisitionItem.toJson()}
                    ${requisitionItem.calculatePercentageCompleted()}
                </div>
                <div>
                </div>
                <div>
                    <label>Substitution:</label>
                    ${requisitionItem?.substitutionItem?.toJson()}
                </div>
                <div>
                    <label>Modification:</label>
                    ${requisitionItem?.modificationItem?.toJson()}
                </div>
            </div>
        </div>
    </div>
</td>
<td class="left">
    <a name="${requisitionItem?.id}"></a>

    <div class="action-menu">
        <button name="actionButtonDropDown" class="action-btn" id="requisitionItem-${requisitionItem?.id }-action">
            <img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
        </button>
        <div class="actions">
            <g:if test="${!requisitionItem.parentRequisitionItem}">
                <g:if test="${requisitionItem?.canApproveQuantity()}">
                    <div class="action-menu-item">
                        <g:link controller="requisitionItem" action="approveQuantity" id="${requisitionItem?.id }" fragment="${requisitionItem?.id}">
                            <img src="${resource(dir: 'images/icons/silk', file: 'accept.png')}"/>&nbsp;
                            <warehouse:message code="requisitionItem.approveQuantity.label" default="Approve quantity"/>
                        </g:link>
                    </div>
                    <% System.out.println("Time 1: " + (System.currentTimeMillis() - startTime) + " ms") %>
                </g:if>
                <g:if test="${requisitionItem.canChangeQuantity()}">
                    <div class="action-menu-item">
                        <g:link controller="requisition" action="review" id="${requisitionItem?.requisition?.id }"
                                params="['requisitionItem.id':requisitionItem?.id, actionType:'changeQuantity']" fragment="${requisitionItem?.id}">
                            <img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>&nbsp;
                            <warehouse:message code="requisitionItem.changeQuantity.label" default="Change quantity"/>
                        </g:link>
                    </div>
                    <% System.out.println("Time 2: " + (System.currentTimeMillis() - startTime) + " ms") %>
                </g:if>
                <g:if test="${requisitionItem.canChooseSubstitute()}">
                    <div class="action-menu-item">
                        <g:link controller="requisition" action="review" id="${requisitionItem?.requisition?.id }" fragment="${requisitionItem?.id}"
                                params="['requisitionItem.id':requisitionItem?.id, actionType:'chooseSubstitute']" >
                            <img src="${resource(dir: 'images/icons/silk', file: 'arrow_switch.png')}"/>&nbsp;
                            <warehouse:message code="requisitionItem.substitute.label" default="Choose substitute"/>
                        </g:link>
                    </div>
                    <% System.out.println("Time 3: " + (System.currentTimeMillis() - startTime) + " ms") %>
                </g:if>
                <g:if test="${requisitionItem.canChangeQuantity()}">
                    <div class="action-menu-item">
                        <g:link controller="requisition" action="review" id="${requisitionItem?.requisition?.id }"
                                params="['requisitionItem.id':requisitionItem?.id, actionType:'cancelQuantity']" fragment="${requisitionItem?.id}">
                            <img src="${resource(dir: 'images/icons/silk', file: 'decline.png')}"/>&nbsp;
                            <warehouse:message code="requisitionItem.cancel.label" default="Cancel requisition item"/>
                        </g:link>
                    </div>
                </g:if>
                <g:if test="${requisitionItem.canUndoChanges()}">
                    <div class="action-menu-item">
                        <g:link controller="requisitionItem" action="undoChanges" id="${requisitionItem?.id }" params="[redirectAction:'review']" fragment="${requisitionItem?.id}"
                                onclick="return confirm('${warehouse.message(code: 'default.button.undo.confirm.message', default: 'Are you sure?')}');">
                            <img src="${resource(dir: 'images/icons/silk', file: 'arrow_undo.png')}"/>&nbsp;
                            <warehouse:message code="requisitionItem.undoChange.label" default="Undo changes"/>
                        </g:link>
                    </div>
                    <% System.out.println("Time 4: " + (System.currentTimeMillis() - startTime) + " ms") %>

                </g:if>
            </g:if>
            <g:else>
                <div class="action-menu-item">
                    <g:link controller="requisitionItem" action="delete" id="${requisitionItem?.id }" params="[redirectAction:'review']" fragment="${requisitionItem?.id}"
                            onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');">
                        <img src="${resource(dir: 'images/icons/silk', file: 'delete.png')}"/>&nbsp;
                        <warehouse:message code="requisitionItem.delete.label" default="Delete requisition item"/>
                    </g:link>
                </div>
            </g:else>



        </div>
    </div>

</td>
<td class="center middle">
    <format:metadata obj="${requisitionItem.status}" />
</td>
<td class="center middle">
    <g:if test="${isCanceled||hasSubstitution}">
        <div class="canceled">
            ${requisitionItem?.product?.productCode}
        </div>
        <div class="">
            ${requisitionItem?.change?.product?.productCode}
        </div>
    </g:if>
    <g:else>
        <div>
            ${requisitionItem?.product?.productCode}
        </div>
    </g:else>


</td>


<td class="product middle">
    <g:if test="${isCanceled||hasSubstitution}">
        <div class="canceled">
            <format:metadata obj="${requisitionItem?.product?.name}" />
        </div>
        <div class="">
            <format:metadata obj="${requisitionItem?.change?.product?.name}" />
        </div>
    </g:if>
    <g:else>
        <div>
            <format:metadata obj="${requisitionItem?.product?.name}" />
        </div>
    </g:else>

</td>
<td class="center middle">
    ${requisitionItem?.product?.unitOfMeasure}
</td>

<td class="center middle" style="width: 10%;">
    <g:if test="${requisitionItem?.cancelReasonCode}">
        <p>${warehouse.message(code:'enum.ReasonCode.' + requisitionItem?.cancelReasonCode)}</p>
        <p class="fade">${requisitionItem?.cancelComments}</p>
    </g:if>
</td>
<td class="quantity center middle">
    <div class="${isCanceled||isChanged?'canceled':''}">
        ${requisitionItem?.quantity} EA/1
    </div>
    <g:if test="${requisitionItem?.change}">
        ${requisitionItem?.change?.quantity}
        EA/1
    </g:if>


</td>
<td class="center middle">
    <g:if test="${isAvailable||isAvailableForSubstitution}">
        <div class="box available">
            <g:if test="${requisitionItem?.hasSubstitution()}">
                <div class="${isCanceled||isChanged?'canceled':''}">
                    ${quantityOnHand?:0} EA/1
                </div>
                ${quantityOnHandForSubstitution?:0} EA/1
            </g:if>
            <g:else>
                ${quantityOnHand?:0} EA/1
            </g:else>
        </div>
    </g:if>
    <g:else>
        <div class="box unavailable">
            <div>${warehouse.message(code:'inventory.unavailable.label',default:'Unavailable')}</div>
            ${quantityOnHand?:0} EA/1
        </div>
    </g:else>
</td>

<td class="center middle">
    ${requisitionItem.orderIndex}
</td>

</tr>

</g:if>

<g:if test="${!requisitionItem.parentRequisitionItem && selectedRequisitionItem &&
        requisitionItem == selectedRequisitionItem && params?.actionType}">
<tr class="${!selectedRequisitionItem?'':selected?'selected':'unselected'}">
<td colspan="9">
<g:if test="${params?.actionType=='changeQuantity'}">
    <div class="box" id="changeQuantity">
        <h2>${warehouse.message(code:'requisitionItem.changeQuantity.label', default:'Change quantity')}</h2>

        <g:hasErrors bean="${flash.errors}">
            <div class="errors">
                <g:renderErrors bean="${flash.errors}" as="list" />
            </div>
        </g:hasErrors>


        <g:form controller="requisitionItem" action="changeQuantity" fragment="${selectedRequisitionItem?.id}">

            <g:hiddenField name="id" value="${selectedRequisitionItem?.requisition?.id }"/>
            <g:hiddenField name="requisitionItem.id" value="${selectedRequisitionItem?.id }"/>
            <g:hiddenField name="actionType" value="${params.actionType }"/>
            <table>
                <tr>
                    <td class="middle right">
                        <label><warehouse:message code="requisitionItem.quantity.label"/></label>
                    </td>
                    <td class="middle">
                        <g:textField id="quantity" name="quantity" value="" class="text" size="5"/>
                        EA/1
                    </td>
                </tr>
                <tr>
                    <td class="middle right">
                        <label><warehouse:message code="requisitionItem.product.label"/></label>
                    </td>
                    <td class="middle">
                        ${selectedRequisitionItem?.product?.productCode}
                        <format:product product="${selectedRequisitionItem?.product}"/>

                    </td>
                </tr>
                <tr>
                    <td class="middle right">
                        <label><warehouse:message code="requisitionItem.cancelReasonCode.label" default="Reason code"/></label>
                    </td>
                    <td class="middle">
                        <g:selectChangeQuantityReasonCode
                                name="reasonCode"
                                class="chzn-select"
                                style="width:450px;"
                                data-placeholder="Choose a reason code ..."
                                noSelection="['':'']"
                                value="${selectedRequisitionItem?.cancelReasonCode }"/>
                    </td>
                </tr>
                <tr>
                    <td class="top right">
                        <label style="display: block;"><warehouse:message code="requisitionItem.comments.label" default="Comments"/></label>
                    </td>
                    <td>
                        <g:textField name="comments" value="${selectedRequisitionItem.cancelComments}"
                                     size="60" class="text" placeholder="${warehouse.message(code:'requisitionItem.comments.placeholder', default:'Any additional information')}"/>

                    </td>

                </tr>
                <tr>
                    <td class="middle center" colspan="2">
                        <button class="button icon approve">
                            ${warehouse.message(code:'default.button.save.label') }
                        </button>

                        <g:link controller="requisition" action="review" id="${selectedRequisitionItem?.requisition?.id}" class="button icon trash">
                            ${warehouse.message(code:'default.button.cancel.label') }
                        </g:link>
                    </td>
                </tr>
            </table>
        </g:form>
    </div>
    <script type="text/javascript">
        $(function() {
            $("#newQuantity").focus();
        });
    </script>

</g:if>
<g:elseif test="${params?.actionType=='changePackageSize'}">
    <div class="box">
        <h2>${warehouse.message(code:'requisitionItem.changePackageSize.label', default:'Change package size')}</h2>
        <g:form controller="requisition" action="changeQuantity" fragment="${selectedRequisitionItem?.id}">
            <g:hiddenField name="id" value="${selectedRequisitionItem?.requisition?.id }"/>
            <g:hiddenField name="requisitionItem.id" value="${selectedRequisitionItem?.id }"/>
            <g:hiddenField name="actionType" value="${params.actionType }"/>

            <table>
                <tr>
                    <td>
                        <g:textField name="quantityChange" value="${selectedRequisitionItem?.quantity}" class="text center" size="5"/>
                    </td>
                    <td>
                        ${selectedRequisitionItem?.product?.productCode}
                        <format:product product="${selectedRequisitionItem?.product}"/>

                    </td>
                    <td>
                        <g:selectProductPackage product="${selectedRequisitionItem?.product}"/>
                    </td>
                    <td>
                        <g:select name="cancelReasonCode"
                                  from="['Package size',
                                          'Stock out',
                                          'Substituted',
                                          'Damaged','Expired',
                                          'Reserved',
                                          'Cancelled by requestor',
                                          'Clinical adjustment',
                                          'Other']"

                                  noSelection="['null':'']" value="${selectedRequisitionItem.cancelReasonCode?:'Package size' }"/>
                    </td>
                    <td>
                        <button class="button">
                            ${warehouse.message(code:'default.button.save.label') }
                        </button>
                    </td>
                    <td>
                        <g:link controller="requisition" action="review" id="${selectedRequisitionItem?.requisition?.id}">
                            ${warehouse.message(code:'default.button.cancel.label') }
                        </g:link>
                    </td>
                </tr>
                <tr>
                    <td colspan="8">
                        <label style="display: block;"><warehouse:message code="requisitionItem.comments.label" default="Comments"/></label>
                        <g:textArea name="comments" cols="100" rows="5"></g:textArea>
                    </td>

                </tr>
            </table>
        </g:form>
    </div>
    <script type="text/javascript">
        $(function() {
            $("#reasonCode").focus();
        });
    </script>
</g:elseif>
<g:elseif test="${params?.actionType=='cancelQuantity'}">
    <div class="box" class="cancelQuantity">
        <h2>${warehouse.message(code:'requisitionItem.cancel.label', default:'Cancel requisition item')}</h2>


        <g:hasErrors bean="${flash.errors}">
            <div class="errors">
                <g:renderErrors bean="${flash.errors}" as="list" />
            </div>
        </g:hasErrors>


        <g:form controller="requisitionItem" action="cancelQuantity" fragment="${selectedRequisitionItem?.id}">
            <g:hiddenField name="id" value="${selectedRequisitionItem?.id }"/>
            <g:hiddenField name="redirectAction" value="${actionName}"/>
            <g:hiddenField name="actionType" value="${params.actionType }"/>


            <table>

                <tr>
                    <td class="right top">
                        <label><warehouse:message code="requisitionItem.product.label" default="Product"/></label>
                    </td>
                    <td>
                        ${selectedRequisitionItem?.product?.productCode}
                        <format:product product="${selectedRequisitionItem?.product}"/>
                    </td>
                </tr>
                <tr>
                    <td class="right top">
                        <label><warehouse:message code="requisitionItem.cancelQuantity.label" default="Cancel quantity"/></label>
                    </td>
                    <td>
                        ${selectedRequisitionItem?.quantity}
                        ${selectedRequisitionItem?.productPackage?:"EA/1"}
                    </td>
                </tr>
                <tr>
                    <td class="right top">
                        <label><warehouse:message code="requisitionItem.reasonCode.label" default="Reason code"/></label>
                    </td>
                    <td>

                        <g:selectCancelReasonCode
                                name="reasonCode"
                                class="chzn-select"
                                style="width:450px;"
                                data-placeholder="Choose a reason code ..."
                                noSelection="['':'']"
                                value="${selectedRequisitionItem?.cancelReasonCode }"/>

                    </td>
                </tr>
                <tr>
                    <td class="right top">
                        <label><warehouse:message code="requisitionItem.comments.label" default="Comments"/></label>

                    </td>
                    <td>
                        <g:textField name="comments" value="${selectedRequisitionItem.cancelComments}"
                                     size="60" class="text" placeholder="${warehouse.message(code:'requisitionItem.comments.placeholder', default:'Any additional information')}"/>
                    </td>

                </tr>
                <tr>
                    <td></td>
                    <td class="left">
                        <button class="button icon approve">
                            ${warehouse.message(code:'default.button.save.label') }
                        </button>
                        &nbsp;
                        <g:link controller="requisition" action="review" id="${selectedRequisitionItem?.requisition?.id}" class="button icon trash">
                            ${warehouse.message(code:'default.button.cancel.label') }
                        </g:link>
                    </td>
                </tr>
            </table>
        </g:form>
    </div>
    <script type="text/javascript">
        $(function() {
            $("#reasonCode").focus();
        });
    </script>
</g:elseif>
<g:elseif test="${params?.actionType=='chooseSubstitute'}">
    <div class="box" id="chooseSubstitute">
        <h2>${warehouse.message(code:'requisitionItem.chooseSubstitute.label', default: 'Choose substitute') }</h2>
        <div class="dialog">
            <g:hasErrors bean="${flash.errors}">
                <div class="errors">
                    <g:renderErrors bean="${flash.errors}" as="list" />
                </div>
            </g:hasErrors>


            <g:form controller="requisitionItem" action="chooseSubstitute" fragment="${selectedRequisitionItem?.id}">
                <g:hiddenField name="id" value="${selectedRequisitionItem?.id }"/>
                <g:hiddenField name="actionType" value="${params.actionType }"/>
                <table>
                    <tr class="">
                        <td class="middle right">
                            <label><warehouse:message code="requisitionItem.product.label"/></label>
                        </td>
                        <td class="value ${hasErrors(bean: selectedRequisitionItem, field: 'product', 'errors')} ${hasErrors(bean: selectedRequisitionItem, field: 'productPackage', 'errors')}">



                            <g:chooseSubstitute id="substitutionId"
                                                name="substitutionId"
                                                data-placeholder="Choose a product package ..."
                                                jsonUrl="${request.contextPath }/json/searchProductPackages"
                                                width="400"
                                                styleClass="text"/>
                        </td>
                    </tr>
                    <tr class="">
                        <td class="name middle right">
                            <label><warehouse:message code="requisitionItem.quantity.label" default="Quantity"/></label>
                        </td>
                        <td class="value ${hasErrors(bean: selectedRequisitionItem, field: 'quantity', 'errors')}">
                            <g:textField id="quantity" name="quantity" value="${selectedRequisitionItem?.quantity}" class="text"
                                         placeholder="${warehouse.message(code:'default.quantity.label') }"/> EA/1
                        </td>
                    </tr>
                    <tr>
                        <td class="name middle right">
                            <label><warehouse:message code="requisitionItem.totalQuantity.label" default="Total quantity"/></label>
                        </td>
                        <td>
                            <span id="totalQuantity">0</span>
                            <span id="unitOfMeasure">EA</span>
                        </td>
                    </tr>
                    <tr>
                        <td class="middle right">
                            <label><warehouse:message code="requisitionItem.reasonCode.label" default="Reason code"/></label>
                        </td>
                        <td class="value ${hasErrors(bean: selectedRequisitionItem, field: 'cancelReasonCode', 'errors')}">

                            <g:selectSubstitutionReasonCode
                                    name="reasonCode"
                                    class="chzn-select"
                                    style="width:450px;"
                                    data-placeholder="Choose a reason code ..."
                                    noSelection="['':'']"
                                    value="${selectedRequisitionItem?.cancelReasonCode }"/>
                        </td>
                    </tr>
                    <tr>
                        <td class="top right">
                            <label><warehouse:message code="requisitionItem.comments.label" default="Comments"/></label>
                        </td>
                        <td class="${hasErrors(bean: selectedRequisitionItem, field: 'comments', 'errors')}">
                            <g:textField name="comments" value="${selectedRequisitionItem.cancelComments}"
                                         size="60" class="text" placeholder="${warehouse.message(code:'requisitionItem.comments.placeholder', default:'Any additional information')}"/>


                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" class="center">
                            <button class="button icon approve">
                                ${warehouse.message(code:'default.button.save.label') }
                            </button>
                            &nbsp;
                            <g:link controller="requisition" action="review" id="${selectedRequisitionItem?.requisition?.id}" class="button icon trash">
                                ${warehouse.message(code:'default.button.cancel.label') }
                            </g:link>
                        </td>
                    </tr>
                </table>
            </g:form>

        </div>
    </div>
    <script language="javascript">
        $(document).ready(function() {

            $("#productPackageQty,#quantity").change(function() {
                var totalQuantity = 0;
                var productPackageQty = $("#productPackageQty").val();
                productPackageQty = productPackageQty ? productPackageQty : 1;
                var quantity = $("#quantity").val();
                console.log(productPackageQty);
                console.log(quantity);
                if(productPackageQty && quantity) {
                    totalQuantity = addCommas(productPackageQty * quantity);
                }
                else {
                    totalQuantity = 0
                }
                $("#totalQuantity").html(totalQuantity);
            });

            /**
             * Borrowed from
             * http://www.mredkj.com/javascript/numberFormat.html
             *
             * @param nStr
             * @return {*}
             */
            function addCommas(nStr) {
                nStr += '';
                x = nStr.split('.');
                x1 = x[0];
                x2 = x.length > 1 ? '.' + x[1] : '';
                var rgx = /(\d+)(\d{3})/;
                while (rgx.test(x1)) {
                    x1 = x1.replace(rgx, '$1' + ',' + '$2');
                }
                return x1 + x2;
            }

        });
    </script>
</g:elseif>
<g:elseif test="${params?.actionType=='supplementProduct'}">
    <div class="box">
        <h2>${warehouse.message(code:'requisitionItem.addAddition.label') }</h2>
        <div class="dialog">
            <g:form controller="requisition" action="addAddition" fragment="${selectedRequisitionItem?.id}">
                <g:hiddenField name="id" value="${selectedRequisitionItem?.requisition?.id }"/>
                <g:hiddenField name="requisitionItem.id" value="${selectedRequisitionItem?.id }"/>
                <table>
                    <tr>
                        <td>
                            <g:autoSuggest id="product" name="product" jsonUrl="${request.contextPath }/json/findProductByName"
                                           width="200" valueId="" valueName="" styleClass="text"/>
                        </td>
                        <td>
                            <g:textField name="quantity" value="" class="text" placeholder="${warehouse.message(code:'default.quantity.label') }"/>
                        </td>
                        <td>
                            <button class="button">
                                ${warehouse.message(code:'default.button.save.label') }
                            </button>
                        </td>
                        <td>
                            <g:link controller="requisition" action="review" id="${selectedRequisitionItem?.requisition?.id}">
                                ${warehouse.message(code:'default.button.cancel.label') }
                            </g:link>
                        </td>
                    </tr>

                </table>

            </g:form>
        </div>
    </div>
</g:elseif>
</td>

</tr>
</g:if>
<% System.out.println("Requisition item ${requisitionItem.id} " + (System.currentTimeMillis() - requisitionStartTime) + " ms") %>
<g:set var="requisitionStartTime" value="${System.currentTimeMillis()}"/>

</g:each>
<% System.out.println("Total for all rows: " + (System.currentTimeMillis() - startTime) + " ms") %>
</tbody>
</table>
</div>
