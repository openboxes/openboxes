<%@ page import="org.pih.warehouse.requisition.RequisitionStatus" %>
<g:set var="startTime" value="${System.currentTimeMillis()}"/>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'requisition.label', default: 'Requisition').toLowerCase()}" />
        <title><warehouse:message code="default.review.label" args="[entityName]" /></title>
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
                        <g:render template="requisitionItems2" model="[requisition: requisition,
                                requisitionItems:requisition.originalRequisitionItems,
                                quantityOnHandMap:quantityOnHandMap,
                                selectedRequisitionItem:selectedRequisitionItem]" />
			        </div>
                </div>
            </div>
		</div>
    </body>
</html>
