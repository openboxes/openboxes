
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'requisition.label', default: 'Requisition').toLowerCase()}" />
        <title><warehouse:message code="default.review.label" args="[entityName]" /></title>
        <style>
            .selected { color: #666; }
            .unselected { color: #ccc;}
            .unavailable { background: #FBE3E4; color: #8a1f11; border-color: #FBC2C4;}
            .available { background: #E6EFC2; color: #264409; border-color: #C6D880; }
        </style>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${requisition}">
                <div class="errors">
                    <g:renderErrors bean="${requisition}" as="list" />
                </div>
            </g:hasErrors>

            <div class="dialog">
            
                <g:render template="summary" model="[requisition:requisition]"/>


                <div class="yui-gf">
                    <div class="yui-u first">
                        <g:render template="header" model="[requisition:requisition]"/>
                    </div>
                    <div class="yui-u">
                        <div id="tabs-details" class="box">
                            <h2>
                                <warehouse:message code="requisition.verify.label" default="Verify requisition"/>
                            </h2>

                            <g:form controller="requisition" action="saveDetails">
                                <g:hiddenField name="redirectAction" value="review"/>
                                <g:hiddenField name="id" value="${requisition?.id}"/>
                                <table style="width:auto;">
                                    <tr>
                                        <td class="left middle">
                                            <label>
                                                ${warehouse.message(code:'requisition.dateVerified.label', default: 'Date verified')}
                                            </label>
                                        </td>
                                        <td class="middle">
                                            <g:if test="${params.edit}">
                                                <g:datePicker name="dateVerified" value="${requisition?.dateVerified}" precision="day"/>
                                            </g:if>
                                            <g:else>
                                                <g:if test="${requisition.dateVerified}">
                                                    <g:formatDate date="${requisition?.dateVerified}" format="dd MMMMM yyyy"/>
                                                </g:if>
                                                <g:else>
                                                    ${warehouse.message(code:'default.none.label')}
                                                </g:else>
                                            </g:else>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td class="left middle">
                                            <label>
                                                ${warehouse.message(code:'requisition.verifiedBy.label', default: 'Verified by')}
                                            </label>
                                        </td>
                                        <td class="middle">
                                            <g:if test="${params.edit}">
                                                <g:selectPerson id="verifiedBy" name="verifiedBy.id" value="${requisition?.verifiedBy}"
                                                                noSelection="['null':'']" size="40"/>
                                            </g:if>
                                            <g:else>
                                                <g:if test="${requisition.verifiedBy}">
                                                    ${requisition?.verifiedBy?.name}
                                                </g:if>
                                                <g:else>
                                                    ${warehouse.message(code:'default.none.label')}
                                                </g:else>
                                            </g:else>
                                        </td>
                                        <td>
                                            <g:if test="${params.edit}">
                                                <button class="button icon approve">
                                                    ${warehouse.message(code:'default.button.save.label')}
                                                </button>
                                                &nbsp;
                                                <g:link controller="requisition" action="review" id="${requisition?.id}">
                                                    ${warehouse.message(code:'default.button.cancel.label')}
                                                </g:link>
                                            </g:if>
                                            <g:else>
                                                <g:link controller="requisition" action="review" id="${requisition?.id}"
                                                        params="[edit:'on']" class="button icon edit">
                                                    ${warehouse.message(code:'default.button.edit.label')}
                                                </g:link>
                                            </g:else>
                                        </td>
                                    </tr>


                                </table>
                            </g:form>

                            <hr/>
                            <br/>
                            <table class="zebra">
                                <thead>
                                    <tr class="odd">
                                        <th class='center'>
                                            <warehouse:message code="default.actions.label"/>
                                        </th>
                                        <th>
                                            <warehouse:message code="product.productCode.label" />
                                        </th>
                                        <th>
                                            <warehouse:message code="requisitionItem.product.label" />
                                        </th>
                                        <th class="center">
                                            <warehouse:message code="requisitionItem.status.label" default="Status" />
                                        </th>
                                        <th class="center ">
                                            <warehouse:message code="requisitionItem.quantityRequested.label" />
                                        </th>
                                        <th class="center">
                                            <warehouse:message code="requisitionItem.totalQuantity.label" default="Quantity total" />
                                        </th>
                                        <th class="center">
                                            <warehouse:message code="requisitionItem.quantityAvailable.label" default="Quantity available" />
                                        </th>
                                        <th class="center">
                                            <warehouse:message code="requisitionItem.productPackage.label" default="UOM" />
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
                                    <g:each var="requisitionItem" in="${requisition?.requisitionItems}">
                                        <g:if test="${!requisitionItem.parentRequisitionItem}">
                                            <%--



                                            <g:render template="reviewRequisitionItem" model="[requisitionItem:requisitionItem, i:count++]"/>




                                            --%>
                                            <g:set var="i" value="${count++}"/>
                                            <g:set var="selected" value="${requisitionItem == selectedRequisitionItem}"/>
                                            <g:set var="quantityOnHand" value="${quantityOnHandMap[requisitionItem?.product?.id]} "/>
                                            <g:set var="quantityOnHandForSubstitution" value="${quantityOnHandMap[requisitionItem?.substitution?.product?.id]} "/>
                                            <g:set var="quantityRemaining" value="${(requisitionItem?.quantity?:0)-(requisitionItem?.calculateQuantityPicked()?:0)}" />
                                        <%-- Need to hack this in since the quantityOnHand value was a String --%>
                                            <g:set var="isCanceled" value="${requisitionItem?.isCanceled()}"/>
                                            <g:set var="isChanged" value="${requisitionItem?.isChanged()}"/>
                                            <g:set var="hasSubstitution" value="${requisitionItem?.hasSubstitution()}"/>
                                            <g:set var="quantityOnHand" value="${quantityOnHand.toInteger()}"/>
                                            <g:set var="isAvailable" value="${(quantityOnHand > 0) && (quantityOnHand >= requisitionItem?.totalQuantity()) }"/>
                                            <g:set var="isAvailableForSubstitution" value="${hasSubstitution && (quantityOnHandForSubstitution > 0) && (quantityOnHandForSubstitution >= requisitionItem?.substitution?.totalQuantity()) }"/>
                                        <%--<tr class="${(i % 2) == 0 ? 'even' : 'odd'} ${!selectedRequisitionItem?'':selected?'selected':'unselected'} ${isAvailable?'':'error'}">--%>
                                            <tr class="prop ${(i % 2) == 0 ? 'odd' : 'even'} ${(requisitionItem?.isCanceled()?'canceled':'')} ${!selectedRequisitionItem?'':selected?'selected':'unselected'}">
                                                <%--${isAvailable?'success':'error'}--%>
                                                <td class="left">
                                                    <a name="${selectedRequisitionItem?.id}"></a>
                                                    <g:if test="${!isChild }">
                                                        <g:render template="/requisitionItem/actions" model="[requisition:requisition,requisitionItem:requisitionItem]"/>
                                                    </g:if>
                                                </td>
                                                <td class="center">
                                                    ${requisitionItem?.product?.productCode}
                                                </td>


                                                <td class="product">
                                                <%--
                                                <g:if test="${isChild }">
                                                    <img src="${resource(dir: 'images/icons', file: 'indent.gif')}" class="middle"/>
                                                </g:if>
                                                --%>
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
                                                <td class="center" style="width: 10%;">
                                                    <div class="${isCanceled?'canceled':''}" title="${requisitionItem?.cancelReasonCode}">
                                                        ${requisitionItem.status}
                                                    </div>
                                                    <g:if test="${requisitionItem?.cancelReasonCode}">
                                                        <p>${warehouse.message(code:'enum.ReasonCode.' + requisitionItem?.cancelReasonCode)}</p>
                                                        <p class="fade">${requisitionItem?.cancelComments}</p>
                                                    </g:if>
                                                <%--
                                                <g:if test="${requisitonItem?.isApproved()}">
                                                    <warehouse:message code="enum.RequisitionItemStatus.APPROVED" default="Approved"/>
                                                </g:if>

                                                <g:if test="${requisitionItem?.isCanceled()}">
                                                    <warehouse:message code="enum.RequisitionItemStatus.CANCELLED" default="Cancelled"/>
                                                    <g:if test="${requisitionItem?.isSubstitution()}">
                                                        <warehouse:message code="enum.requisitionItemStatus.SUBSTITUTED" default="Substituted"/>
                                                    </g:if>
                                                </g:if>
                                                <g:elseif test="${requisitionItem?.isChanged()}">
                                                    <warehouse:message code="enum.requisitionItemStatus.CHANGED" default="Changed"/>
                                                </g:elseif>
                                                <g:else>
                                                    ${warehouse.message(code:'default.pending.label')}
                                                </g:else>
                                                --%>
                                                </td>
                                                <td class="quantity center">
                                                    <div class="${isCanceled||isChanged?'canceled':''}">
                                                        ${requisitionItem?.quantity}
                                                        <%--
                                                        ${requisitionItem?.productPackage?.uom?.code?:"EA" }/${requisitionItem?.productPackage?.quantity?:"1" }
                                                        --%>
                                                    </div>
                                                    <g:if test="${requisitionItem?.change}">
                                                        ${requisitionItem?.change?.quantity}
                                                    <%--
                                                    ${requisitionItem?.change?.productPackage?.uom?.code?:"EA"}/${requisitionItem?.change?.productPackage?.quantity?:"1"}
                                                    --%>
                                                    </g:if>


                                                </td>
                                                <td class="center">
                                                    <div class="${isCanceled||isChanged?'canceled':''}">
                                                        ${requisitionItem?.totalQuantity()}
                                                    </div>
                                                    <g:if test="${requisitionItem?.change}">
                                                        ${requisitionItem?.change?.totalQuantity()}
                                                    </g:if>
                                                </td>
                                                <td class="center">
                                                    <g:if test="${isAvailable||isAvailableForSubstitution}">
                                                        <div class="box available">
                                                            <g:if test="${requisitionItem?.hasSubstitution()}">
                                                                <div class="${isCanceled||isChanged?'canceled':''}">
                                                                    ${quantityOnHand?:0}
                                                                </div>
                                                                ${quantityOnHandForSubstitution?:0}
                                                            </g:if>
                                                            <g:else>
                                                                ${quantityOnHand?:0}
                                                            </g:else>
                                                        </div>
                                                    </g:if>
                                                    <g:else>
                                                        <div class="box unavailable">
                                                            ${warehouse.message(code:'inventory.unavailable.label',default:'Unavailable')}
                                                            ${quantityOnHand?:0}
                                                        </div>
                                                    </g:else>
                                                </td>
                                                <td class="center">
                                                    EA/1
                                                </td>
                                                <td class="center">
                                                    ${requisitionItem.orderIndex}
                                                </td>

                                                <%--
                                                <td class="quantity center">
                                                    <label>${requisitionItem?.totalQuantityCanceled()} EA</label>
                                                    <g:if test="${requisitionItem?.productPackage}">
                                                        <div class="fade box">
                                                            ${requisitionItem?.quantityCanceled} x ${(requisitionItem?.productPackage?.quantity?:1) } ${(requisitionItem?.productPackage?.uom?.code?:"EA")}
                                                        </div>
                                                    </g:if>
                                                </td>
                                                --%>

                                                <%--
                                                <td class="quantity right">
                                                    ${quantityAvailableToPromiseMap[requisitionItem?.product?.id]}
                                                    ${requisitionItem?.product.unitOfMeasure?:"EA" }
                                                </td>
                                                --%>

                                            </tr>


                                        </g:if>

                                        <g:if test="${!requisitionItem.parentRequisitionItem && selectedRequisitionItem &&
                                                requisitionItem == selectedRequisitionItem && params?.actionType}">
                                            <tr>
                                                <td colspan="9">
                                                    <g:if test="${params?.actionType=='changeQuantity'}">
                                                        <g:render template="changeQuantity" model="[selectedRequisitionItem:selectedRequisitionItem]"/>
                                                    </g:if>
                                                    <g:elseif test="${params?.actionType=='changePackageSize'}">
                                                        <g:render template="changePackageSize" model="[selectedRequisitionItem:selectedRequisitionItem]"/>
                                                    </g:elseif>
                                                    <g:elseif test="${params?.actionType=='cancelQuantity'}">
                                                        <g:render template="cancelQuantity" model="[selectedRequisitionItem:selectedRequisitionItem]"/>
                                                    </g:elseif>
                                                    <g:elseif test="${params?.actionType=='chooseSubstitute'}">
                                                        <g:render template="chooseSubstitute" model="[selectedRequisitionItem:selectedRequisitionItem]"/>
                                                    </g:elseif>
                                                    <g:elseif test="${params?.actionType=='supplementProduct'}">
                                                        <g:render template="supplementProduct" model="[selectedRequisitionItem:selectedRequisitionItem]"/>
                                                    </g:elseif>
                                                </td>

                                            </tr>
                                        </g:if>

                                    </g:each>
                                </tbody>
                            </table>
                        </div>
                        <div class="clear"></div>
                        <g:unless test="${params.edit}">
                            <div class="buttons">
                                <div class="center">
                                    <g:link controller="requisition" action="edit" id="${requisition.id }" class="button icon arrowleft">
                                        <warehouse:message code="default.button.back.label"/>
                                    </g:link>

                                    <g:link controller="requisition" action="pick" id="${requisition.id }" class="button icon arrowright">
                                        <warehouse:message code="default.button.next.label"/>
                                    </g:link>
                                </div>
                            </div>
                        </g:unless>
			        </div>
                </div>
            </div>
		</div>
		<script type="text/javascript">
			$(function() {
				$(".selectAll").click(function() {
					var thisCheck = $(this);
					if (thisCheck.is(':checked')) {
						$(".selectItem").attr("checked", true);
					}
					else {
						$(".selectItem").attr("checked", false);
					}
				});
			}); 		
		</script>
            
    </body>
</html>
