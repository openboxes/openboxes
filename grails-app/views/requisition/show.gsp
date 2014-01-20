
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'requisition.label', default: 'Requisition').toLowerCase()}" />
        <title><warehouse:message code="default.view.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
            
                <g:render template="summary" model="[requisition:requisition]"/>

                <div class="yui-gf">
                    <div class="yui-u first">
                        <g:render template="header" model="[requisition:requisition]"/>
                    </div>
                    <div class="yui-u">
                        <div id="tabs-details">
                            <div class="box">
                                <h2><warehouse:message code="requisition.show.label"/></h2>
                                <table>
                                    <thead>
                                        <tr class="odd">
                                            <th></th>
                                            <%--
                                            <th><warehouse:message code="requisition.progressBar.label" /></th>
                                            <th></th>
                                            --%>

                                            <th><warehouse:message code="product.label" /></th>
                                            <th class="center"><warehouse:message code="requisition.quantity.label" /></th>
                                            <th class="center"><warehouse:message code="requisition.totalQuantity.label" default="Quantity requested" /></th>
                                            <th class="center"><warehouse:message code="picklist.quantity.label" /></th>
                                            <th class="center"><warehouse:message code="requisitionItem.quantityCanceled.label" /></th>
                                            <th class="center"><warehouse:message code="requisition.quantityRemaining.label" /></th>
                                            <th class="center"><warehouse:message code="product.uom.label" /></th>
                                            <th class="center"><warehouse:message code="requisitionItem.orderIndex.label" default="Sort order" /></th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <g:if test="${requisition?.requisitionItems?.size() == 0}">
                                            <tr class="prop odd">
                                                <td colspan="9" class="center"><warehouse:message
                                                        code="requisition.noRequisitionItems.message" /></td>
                                            </tr>
                                        </g:if>
                                        <g:each var="requisitionItem" in="${requisition?.requisitionItems}" status="i">
                                            <g:render template="showRequisitionItem" model="[i:i,requisitionItem:requisitionItem]"/>
                                            <%--
                                            <g:each var="childRequisitionItem" in="${requisitionItem?.requisitionItems}" status="j">
                                                <g:render template="showRequisitionItem" model="[i:j,requisitionItem:childRequisitionItem]"/>
                                            </g:each>
                                            --%>

                                        </g:each>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                        <div class="clear"></div>
                        <div class="buttons">
                            <div class="center">
                                <g:link controller="requisition" action="list" class="button icon arrowleft">
                                    <warehouse:message code="default.button.back.label"/>
                                </g:link>

                                <g:link controller="requisition" action="edit" id="${requisition.id }" class="button icon arrowright">
                                    <warehouse:message code="default.button.next.label"/>
                                </g:link>
                            </div>
                        </div>
                    </div>
                </div>
			</div>
		</div>
         
    </body>
</html>
