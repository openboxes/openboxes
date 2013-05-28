
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'requisition.label', default: 'Requisition').toLowerCase()}" />
        <title><warehouse:message code="default.review.label" args="[entityName]" /></title>
        <style>
            .selected { color: #666; }
            .unselected { color: #ccc; }
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


                            <table>
                                <thead>
                                    <tr class="odd">
                                        <th class='center'>
                                            <warehouse:message code="default.actions.label"/>
                                        </th>
                                        <th class="center">
                                            <warehouse:message code="requisitionItem.status.label" default="Status" />
                                        </th>
                                        <th>
                                            <warehouse:message code="requisitionItem.product.label" />
                                        </th>
                                        <th class="center ">
                                            <warehouse:message code="requisitionItem.quantityRequested.label" />
                                        </th>
                                        <th class="center">
                                            <warehouse:message code="requisitionItem.totalQuantity.label" default="Quantity total" />
                                        </th>
                                        <th class="center">
                                            <warehouse:message code="requisitionItem.availability.label" default="Availability" /><br/>
                                            <warehouse:message code="requisitionItem.quantityAvailable.label" default="Quantity available" />
                                        </th>
                                        <th class="center">
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
                                    <g:each var="requisitionItem" in="${requisition?.requisitionItems.findAll { !it.parentRequisitionItem }}" status="i">
                                        <g:render template="reviewRequisitionItem" model="[requisitionItem:requisitionItem, i:i]"/>
                                        <g:if test="${selectedRequisitionItem && requisitionItem == selectedRequisitionItem && params?.actionType}">
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
                                    <g:link controller="requisition" action="edit" id="${requisition.id }" class="button">
                                        <warehouse:message code="default.button.back.label"/>
                                    </g:link>

                                    <g:link controller="requisition" action="pick" id="${requisition.id }" class="button">
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
