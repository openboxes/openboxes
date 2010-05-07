
<%@ page import="org.pih.warehouse.Shipment" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${message(code: 'shipment.label', default: 'Shipment')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
	<!--
	    Specify content to overload like global navigation links,
	    page titles, etc.
	-->
	<content tag="globalLinks">
	    <span class="menuButton">
		<g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link>
	    </span>
	</content>
	<content tag="pageTitle">
	    <g:message code="default.list.label" args="[entityName]" />
	</content>
	<g:javascript library="prototype" />

	<g:javascript>
	   function clearShipment(e) {
	      $('shipmentContent').value='';
	   }

	    function showSpinner(visible) {
		$('spinner').style.display = visible ? "inline" : "none";
	    }
	</g:javascript>
    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
            </g:if>
	    <div class="messages">
		    test
	    </div>

            <div class="list">
                <table>
                    <thead>
                        <tr>                        
                            <g:sortableColumn property="id" title="${message(code: 'shipment.id.label', default: 'Id')}" />                        
                            <g:sortableColumn property="trackingNumber" title="${message(code: 'shipment.trackingNumber.label', default: 'Tracking Number')}" />                        
                            <g:sortableColumn property="expectedShippingDate" title="${message(code: 'shipment.expectedShippingDate.label', default: 'Expected Shipping Date')}" />                        
                            <g:sortableColumn property="actualShippingDate" title="${message(code: 'shipment.actualShippingDate.label', default: 'Actual Shipping Date')}" />                        
                            <th><g:message code="shipment.source.label" default="Source" /></th>                   	    
                            <th><g:message code="shipment.target.label" default="Target" /></th>                   	    
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${shipmentInstanceList}" status="i" var="shipmentInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            <td><g:link action="show" id="${shipmentInstance.id}">${fieldValue(bean: shipmentInstance, field: "id")}</g:link></td>                        
                            <td>${fieldValue(bean: shipmentInstance, field: "trackingNumber")}</td>
                            <td><g:formatDate date="${shipmentInstance.expectedShippingDate}" /></td>
                            <td><g:formatDate date="${shipmentInstance.actualShippingDate}" /></td>
                            <td>${fieldValue(bean: shipmentInstance, field: "source")}</td>
                            <td>${fieldValue(bean: shipmentInstance, field: "target")}</td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${shipmentInstanceTotal}" />
            </div>
	    <g:form action="addShipmentAjax">
		Add Shipment Tracking Number: <input name="trackingNumber" type="text"></input>
		<g:submitToRemote update="updateMe" 
				  value="Add"
				  url="[controller: 'shipment', action: 'addShipmentAjax']"/>
	    </g:form>
	    <div id="updateMe">this div is updated by the form</div>
<%--
	    Quick add:
	    <g:form action="ajaxAdd">
		   <g:textArea id='shipmentContent' name="content" rows="3" cols="50"/><br/>
		   <g:submitToRemote 
		       value="Add shipment"
		       url="[controller: 'shipment', action: 'addShipmentAjax']"
		       update="allShipments"
		       onSuccess="clearShipment(e)"
		       onLoading="showSpinner(true)"
		       onComplete="showSpinner(false)"/>
		   <img id="spinner" style="display: none" src="<g:createLinkTo dir='/images' file='spinner.gif'/>"/>
	    </g:form>
--%>

        </div>
    </body>
</html>
