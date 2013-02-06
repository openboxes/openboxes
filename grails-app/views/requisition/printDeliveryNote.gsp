<%@ page import="org.pih.warehouse.core.RoleType" %>
<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name="layout" content="print" />
    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'print.css')}" type="text/css" media="print, screen, projection" />
    <g:set var="entityName" value="${warehouse.message(code: 'requisition.label', default: 'Requisition')}" />
    <title><warehouse:message code="default.show.label" args="[entityName]" /></title>
	<script src="${createLinkTo(dir:'js/jquery.nailthumb', file:'jquery.nailthumb.1.1.js')}" type="text/javascript" ></script>
   	<link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.nailthumb',file:'jquery.nailthumb.1.1.css')}" type="text/css" media="all" />
   	
   	<style>
   	.cf-header {
		overflow: auto;
		width: 100%
	}
   	</style>
</head>
<body>
	<div id="print-header">
		<h2>
			<warehouse:message code="requisition.printDeliveryNote.label"/>
			<span style="float: right;">
			    <button type="button" id="print-button" onclick="window.print()">
			        <img src="${resource(dir: 'images/icons/silk', file: 'printer.png')}" />
			        ${warehouse.message(code:"default.print.label")}
			    </button>
			    &nbsp;
			    <a href="javascript:window.close();">Close</a>
			    
			</span>
		</h2>
    </div>
    <div class="clear"></div>
    <div style="float: right;">
	    <div class="header">
	        <label><warehouse:message code="requisition.ward.label"/>:</label> 
	        ${requisition.origin?.name}
	    </div>
	    <div class="header">
	        <label><warehouse:message code="requisition.date.label"/>:</label> 
	        <g:formatDate date="${requisition?.dateRequested}" format="MMMMM dd, yyyy hh:mm a"/>
	    </div>
	    <div class="header">
	        <label><warehouse:message code="requisition.deliveryDate.label"/>:</label> 
	        <g:formatDate date="${new Date()}" format="MMMMM dd, yyyy hh:mm a"/>
	    </div>
	    <%-- 
	    <div class="header">
	        <label><warehouse:message code="requisition.requisitionNumber.label"/>:</label> ${requisition?.requestNumber }
	    </div>
        --%>
        
    </div>
    
    <div class="requisition-header cf-header" style="margin-bottom: 20px;">
	    <div class="print-logo nailthumb-container" style="float: left;">
	   		<img src="${createLinkTo(dir:'images/', file:'hands.jpg')}"/>
	    </div>
	    <div class="requisition-number" style="float: left; text-align: center; margin: 5px; width: 890px;" >
		   	<h4>${session.warehouse?.name } <warehouse:message code="requisition.deliveryNote.label"/></h4>
		   	<h4>${requisition?.name }</h4>
		    <img src="${createLink(controller:'product',action:'barcode',params:[data:requisition?.requestNumber,width:200,height:30,format:'CODE_128']) }"/>
	    	<g:if test="${requisition.requestNumber }">	    	
		    	<h3>${requisition?.requestNumber }</h3>
	    	</g:if>
		   	
	    </div>
	</div>    
    <div class="clear"></div>
    <table id="requisition-items">
            <tr class="theader">
                <th><warehouse:message code="report.number.label"/></th>
                <th></th>
                <th class="center">${warehouse.message(code: 'product.productCode.label')}</th>
                <th>${warehouse.message(code: 'product.label')}</th>
                <th class="center">${warehouse.message(code: 'requisitionItem.quantityPicked.label')}</th>
            </tr>

            <g:each in="${requisition?.requisitionItems}" status="i" var="requisitionItem">
                <g:if test="${picklist}">
                    <g:set var="picklistItems" value="${requisitionItem?.retrievePicklistItems()}" />
                    <g:set var="numInventoryItem" value="${picklistItems?.size() ?: 1}" />
                </g:if>
                <g:else>
                    <g:set var="numInventoryItem" value="${requisitionItem?.calculateNumInventoryItem() ?: 1}" />
                </g:else>
                <g:set var="j" value="${0}"/>
                <g:while test="${j < numInventoryItem}">
	                <tr class="prop">
                        <td class="center">${i+1}</td>
                        <td class="center">
	                        <img src="${createLink(controller:'product',action:'barcode',params:[data:requisitionItem?.product?.productCode,width:100,height:60,format:'CODE_128']) }"/>
                        </td>
                        <td class="center top">
	                        <label>${requisitionItem?.product?.productCode }</label>
                        </td>
                        <td>${requisitionItem?.product?.name}</td>
	                    <td class="center">
	                    	${picklistItems[j]?.quantity?:0}
                        	${requisitionItem?.product?.unitOfMeasure?:"EA"}
	                    </td>
	                    <%j++%>
	                </tr>
                </g:while>
            </g:each>

    </table>
    <p><warehouse:message code="requisitionItem.comment.label"/>:</p>
    <div id="comment-box">

    </div>
<script>
	$(document).ready(function() {
		$('.nailthumb-container').nailthumb({ width : 100, height : 100 });
    	//window.print();
	});	
</script>				    
    
</body>
</html>
