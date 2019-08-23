
<%@ page import="org.pih.warehouse.requisition.RequisitionItem" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'requisitionItem.label', default: 'RequisitionItem')}" />
        <title><warehouse:message code="default.edit.label" args="[entityName]" /></title>
        <!-- Specify content to overload like global navigation links, page titles, etc. -->
		<content tag="pageTitle"><warehouse:message code="default.edit.label" args="[entityName]" /></content>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${requisitionItemInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${requisitionItemInstance}" as="list" />
	            </div>
            </g:hasErrors>


            <g:hiddenField name="id" value="${requisitionItemInstance?.id}" />
            <g:hiddenField name="version" value="${requisitionItemInstance?.version}" />
            
                
            <div class="dialog">

                 <g:render template="../requisition/summary" model="[requisition:requisitionItemInstance?.requisition]"/>


                <div class="yui-gd">
                    <div class="yui-u first">
                        <g:render template="../requisition/header" model="[requisition:requisitionItemInstance?.requisition]"/>
                    </div>
                    <div class="yui-u">
                        <div class="box">
                            <h2>
                                ${requisitionItemInstance?.product?.productCode} - ${requisitionItemInstance?.product?.name}
                                <g:if test="${requisitionItemInstance?.productPackage}">
                                    (${requisitionItemInstance?.productPackage?.uom?.code}/${requisitionItemInstance?.productPackage?.quantity})
                                </g:if>
                                <g:else>
                                    (EA/1)
                                </g:else>
                            </h2>
                            <div>
                                <div class="box">
                                    <label for="quantity"><warehouse:message code="requisitionItem.quantityRequested.label" default="Quantity requested" /></label>
                                    <div>
                                    ${requisitionItemInstance?.quantity }
                                    ${requisitionItemInstance?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label') }
                                    </div>
                                </div>
                                <div class="box">
                                    <label for="quantity"><warehouse:message code="requisitionItem.quantityCanceled.label" default="Quantity canceled" /></label>

                                    <div>
                                        ${requisitionItemInstance?.quantityCanceled?:0 }
                                        ${requisitionItemInstance?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label') }
                                        ${requisitionItemInstance?.cancelReasonCode }
                                    </div>
                                </div>

                                <div class="box">
                                    <label><warehouse:message code="default.quantityOnHand.label" default="Quantity on Hand" /></label>
                                    <div>
                                        ${quantityOnHand }
                                        ${requisitionItemInstance?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label') }
                                    </div>
                                </div>
                            </div>
                            <br/>

                            <div class="box">
                                <table>
                                    <thead>
                                    <tr class="odd">
                                        <th></th>
                                        <th>${warehouse.message(code:'product.label') }</th>
                                        <th>${warehouse.message(code:'product.unitOfMeasure.label') }</th>
                                        <th>${warehouse.message(code:'requisitionItem.quantity.label') }</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <g:each var="requisitionItem" in="${requisitionItemInstance?.requisitionItems }">
                                        <tr>
                                            <td>
                                                ${requisitionItem?.product?.productCode }
                                            </td>
                                            <td>
                                                ${requisitionItem?.product?.name }
                                            </td>
                                            <td>
                                                ${requisitionItem?.product?.unitOfMeasure }
                                            </td>
                                            <td>
                                                ${requisitionItem?.quantity}
                                            </td>
                                        </tr>
                                    </g:each>
                                    <g:unless test="${requisitionItemInstance?.requisitionItems }">
                                        <tr>
                                            <td colspan="4" class="center">
                                                <warehouse:message code="requisitionItem.noChanges.message"/>
                                            </td>
                                        </tr>
                                    </g:unless>
                                    </tbody>
                                </table>
                            </div>

                        <div class="tabs">
                            <ul>
                                <li><a href="#tabs-change"><warehouse:message
                                        code="requisitionItem.change.label"
                                        default="Modify requisition item"/></a></li>
                                <li><a href="#tabs-substitution"><warehouse:message
                                        code="requisitionItem.substitute.label"
                                        default="Subsitute another item"/></a></li>
                                <li><a href="#tabs-cancelation"><warehouse:message
                                        code="requisitionItem.cancel.label"
                                        default="Cancel this item"/></a></li>
                                <li><a href="#tabs-addition"><warehouse:message
                                        code="requisitionItem.addition.label"
                                        default="Add a supplemental item"/></a></li>
                            </ul>
                            <div id="tabs-change">
                                <g:if test="${!requisitionItemInstance?.quantityCanceled }">
                                    <div class="">
                                        <h3>${warehouse.message(code:'requisitionItem.changeQuantityOrPackageSize.label') }</h3>
                                        <div class="dialog">
                                            <g:form controller="requisition" action="changeQuantity">
                                                <g:hiddenField name="id" value="${requisitionItemInstance?.requisition?.id }"/>
                                                <g:hiddenField name="requisitionItem.id" value="${requisitionItemInstance?.id }"/>
                                                <table>
                                                    <tr>
                                                        <td></td>

                                                        <td class="prop">
                                                            ${requisitionItemInstance?.product?.name }
                                                        </td>
                                                        <td class="prop">
                                                            <g:selectProductPackage product="${requisitionItemInstance.product}" noSelection="['null':'']"/>

                                                        </td>
                                                        <td class="prop">
                                                            <g:textField name="quantity" value="${requisitionItemInstance?.quantity}" class="text" placeholder="${warehouse.message(code:'default.quantity.label') }"/>
                                                        </td>
                                                        <td class="prop">
                                                            <g:select name="requisitionItem.cancelReasonCode"
                                                                from="['Package size','Stock out','Substituted','Damaged','Expired','Reserved',
                                                                    'Cancelled by requestor','Clinical adjustment', 'Other']"
                                                                      noSelection="['null':'']" value="${requisitionItemInstance.cancelReasonCode }"/>

                                                        </td>
                                                        <td class="prop">
                                                            <button class="button">
                                                                ${warehouse.message(code:'default.button.save.label') }
                                                            </button>
                                                        </td>

                                                    </tr>
                                                </table>
                                            </g:form>
                                        </div>
                                    </div>
                                </g:if>
                                <g:else>
                                    <warehouse:message code="requisition.requisitionItemHasBeenCanceled.message" default="Requisition item has been canceled"/>

                                </g:else>

                            </div>
                            <div id="tabs-substitution">
                                <g:if test="${!requisitionItemInstance?.quantityCanceled }">
                                    <div class="box">
                                        <h3>${warehouse.message(code:'requisitionItem.addSubstitution.label') }</h3>
                                        <div class="dialog">
                                            <g:form controller="requisition" action="addSubstitution">
                                                <g:hiddenField name="id" value="${requisitionItemInstance?.requisition?.id }"/>
                                                <g:hiddenField name="requisitionItem.id" value="${requisitionItemInstance?.id }"/>
                                                <table>
                                                    <tr>
                                                        <td>
                                                            <g:autoSuggest id="product" name="product" jsonUrl="${request.contextPath }/json/searchProduct"
                                                                   width="200" valueId="" valueName="" styleClass="text"/>
                                                        </td>
                                                        <td class="prop">
                                                            <g:textField name="quantity" value="" class="text" placeholder="${warehouse.message(code:'default.quantity.label') }"/>
                                                        </td>
                                                        <td class="prop">
                                                            <g:select name="requisitionItem.cancelReasonCode" from="['Package size','Stock out','Substituted','Damaged','Expired','Reserved',
                                                                    'Cancelled by requestor','Clinical adjustment', 'Other']"
                                                                      noSelection="['null':'']" value="${requisitionItemInstance.cancelReasonCode }"/>

                                                        </td>
                                                        <td class="prop">
                                                            <button class="button">
                                                                ${warehouse.message(code:'default.button.save.label') }
                                                            </button>
                                                        </td>
                                                </tr>
                                                </table>
                                            </g:form>

                                        </div>
                                    </div>
                                </g:if>
                                <g:else>
                                    <warehouse:message code="requisition.requisitionItemHasBeenCanceled.message" default="Requisition item has been canceled"/>
                                </g:else>

                            </div>
                            <div id="tabs-cancelation">

                                <!--Cancellation tab-->
                            </div>
                            <div id="tabs-addition">
                                <div class="box">
                                    <h3>${warehouse.message(code:'requisitionItem.addAddition.label') }</h3>
                                    <div class="dialog">
                                        <g:form controller="requisition" action="addAddition">
                                            <g:hiddenField name="id" value="${requisitionItemInstance?.requisition?.id }"/>
                                            <g:hiddenField name="requisitionItem.id" value="${requisitionItemInstance?.id }"/>
                                            <table>
                                                <tr>
                                                    <td class="prop">
                                                        <g:autoSuggest id="product" name="product" jsonUrl="${request.contextPath }/json/findProductByName"
                                                                       width="200" valueId="" valueName="" styleClass="text"/>
                                                    </td>
                                                    <td class="prop">
                                                        <g:textField name="quantity" value="" class="text" placeholder="${warehouse.message(code:'default.quantity.label') }"/>
                                                    </td>
                                                    <td class="prop">
                                                        <button class="button">
                                                            ${warehouse.message(code:'default.button.save.label') }
                                                        </button>
                                                    </td>
                                                </tr>

                                            </table>

                                        </g:form>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="buttons center">
                        <g:link controller="requisition" action="review" class="button" id="${requisitionItemInstance?.requisition?.id }">
                            <warehouse:message code="requisition.backToItems.label" default="Back to requisition items"/>
                        </g:link>
                    </div>
                </div>
            </div>
        </div>
    </div>
		<script>
			$(document).ready(function() {
                $(".tabs").tabs().addClass('ui-tabs-vertical ui-helper-clearfix');
                $(".tabs li").removeClass('ui-corner-top').addClass('ui-corner-left');
			});	
		</script>        
        
    </body>
</html>
