

<g:if test="${selectedRequisitionItem && requisitionItem == selectedRequisitionItem}">
<div id="modifyRequisitionItem-${selectedRequisitionItem?.id}" class="dialog" style="display: none;">
    <table>
        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
            <td colspan="8">
                <div class="tabs">
                    <ul>
                        <li><a href="#tabs-change"><warehouse:message
                                code="requisitionItem.change.label"
                                default="Modify"/></a></li>
                        <li><a href="#tabs-substitution"><warehouse:message
                                code="requisitionItem.substitute.label"
                                default="Substitute"/></a></li>
                        <li><a href="#tabs-addition"><warehouse:message
                                code="requisitionItem.addition.label"
                                default="Addition"/></a></li>
                        <li><a href="#tabs-cancellation"><warehouse:message
                                code="requisitionItem.cancelation.label"
                                default="Cancelation"/></a></li>
                    </ul>
                    <div id="tabs-change">
                        <g:if test="${!selectedRequisitionItem?.quantityCanceled }">
                            <div class="box">
                                <h2>${warehouse.message(code:'requisitionItem.changeQuantity.label') }</h2>
                                <div class="dialog">
                                    <g:form controller="requisition" action="changeQuantity">
                                        <g:hiddenField name="id" value="${selectedRequisitionItem?.requisition?.id }"/>
                                        <g:hiddenField name="requisitionItem.id" value="${selectedRequisitionItem?.id }"/>
                                        <div class="prop">
                                            ${selectedRequisitionItem?.product?.name }
                                            <g:if test="${requisitionItem?.productPackage}">
                                                ${selectedRequisitionItem?.productPackage?.uom?.code}/${selectedRequisitionItem?.productPackage?.quantity}
                                            </g:if>
                                        </div>
                                        <div class="prop">
                                            <g:textField name="quantity" value="${selectedRequisitionItem?.quantity}" class="text" placeholder="${warehouse.message(code:'default.quantity.label') }"/>
                                        </div>
                                        <div class="prop">
                                            <g:select name="parentCancelReasonCode" from="['Package size','Stock out','Substituted','Damaged','Expired','Reserved',
                                                    'Cancelled by requestor','Clinical adjustment', 'Other']"
                                                      noSelection="['null':'']" value="${selectedRequisitionItem.cancelReasonCode }"/>

                                        </div>
                                        <div class="prop">
                                            <button class="button">
                                                ${warehouse.message(code:'default.button.save.label') }
                                            </button>
                                        </div>
                                    </g:form>
                                </div>
                            </div>
                        </g:if>

                    </div>
                    <div id="tabs-substitution">
                        <g:if test="${!selectedRequisitionItem?.quantityCanceled }">
                            <div class="box">
                                <h2>${warehouse.message(code:'requisitionItem.addSubstitution.label') }</h2>
                                <div class="dialog">
                                    <g:form controller="requisition" action="addSubstitution">
                                        <g:hiddenField name="id" value="${selectedRequisitionItem?.requisition?.id }"/>
                                        <g:hiddenField name="requisitionItem.id" value="${selectedRequisitionItem?.id }"/>
                                        <div class="prop">
                                            <g:autoSuggest id="product" name="product" jsonUrl="${request.contextPath }/json/findProductByName"
                                                           width="200" valueId="" valueName="" styleClass="text"/>
                                        </div>
                                        <div class="prop">
                                            <g:textField name="quantity" value="" class="text" placeholder="${warehouse.message(code:'default.quantity.label') }"/>
                                        </div>
                                        <div class="prop">
                                            <g:select name="parentCancelReasonCode" from="['Package size','Stock out','Substituted','Damaged','Expired','Reserved',
                                                    'Cancelled by requestor','Clinical adjustment', 'Other']"
                                                      noSelection="['null':'']" value="${selectedRequisitionItem.cancelReasonCode }"/>

                                        </div>
                                        <div class="prop">
                                            <button class="button">
                                                ${warehouse.message(code:'default.button.save.label') }
                                            </button>
                                        </div>
                                    </g:form>
                                </div>
                            </div>
                        </g:if>
                    </div>
                    <div id="tabs-addition">
                        <div class="box">
                            <h2>${warehouse.message(code:'requisitionItem.addAddition.label') }</h2>
                            <div class="dialog">
                                <g:form controller="requisition" action="addAddition">
                                    <g:hiddenField name="id" value="${selectedRequisitionItem?.requisition?.id }"/>
                                    <g:hiddenField name="requisitionItem.id" value="${selectedRequisitionItem?.id }"/>
                                    <div class="prop">
                                        <g:autoSuggest id="product" name="product" jsonUrl="${request.contextPath }/json/findProductByName"
                                                       width="200" valueId="" valueName="" styleClass="text"/>
                                    </div>
                                    <div class="prop">
                                        <g:textField name="quantity" value="" class="text" placeholder="${warehouse.message(code:'default.quantity.label') }"/>
                                    </div>
                                    <div class="prop">
                                        <button class="button">
                                            ${warehouse.message(code:'default.button.save.label') }
                                        </button>
                                    </div>
                                </g:form>
                            </div>
                        </div>
                    </div>
                    <div id="tabs-cancellation">

                    </div>
                </div>
            </td>
        </tr>
    </table>
</div>
</g:if>