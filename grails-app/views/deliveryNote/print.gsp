<%@ page import="org.pih.warehouse.core.RoleType" %>
<%@ page contentType="text/html;charset=UTF-8" defaultCodec="html" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
    <g:set var="entityName" value="${warehouse.message(code: 'requisition.label', default: 'Requisition')}"/>
    <title><warehouse:message code="default.show.label" args="[entityName]"/></title>
    <link rel="stylesheet" href="${resource(dir:'css',file:'buttons.css')}" type="text/css" media="all" />
    <script src="https://code.jquery.com/jquery-3.3.1.slim.min.js" integrity="sha384-q8i/X+965DzO0rT7abK41JStQIAqVgRVzpbzo5smXKp4YfRvH+8abtTE1Pi6jizo" crossorigin="anonymous"></script>
    <g:render template="styles"/>
</head>

<body class="${params.orientation?:'portrait'}">
    <g:render template="printToolbar"/>

    <g:render template="printHeader" model="[
        title         : g.message(code: 'requisition.deliveryNote.label', default: 'Delivery Note'),
        documentNumber: requisition.requestNumber,
        documentName  : requisition.name,
        origin        : requisition.origin,
        destination   : requisition.destination,
        requestedBy   : requisition?.requestedBy?.name,
        dateRequested : requisition?.dateRequested,
        shipDate      : requisition?.shipment?.expectedShippingDate,
        receivedDate  : requisition?.shipment?.receipt?.actualDeliveryDate,
    ]"/>

    <g:render template="addressSection" model="[origin: requisition.origin, destination: requisition.destination]"/>

    <g:set var="requisitionItems" value='${requisition.requisitionItems.sort { it.product.name }}'/>
    <g:set var="requisitionItemsCanceled" value='${requisitionItems.findAll { it.isCanceled()}}'/>
    <g:set var="requisitionItems" value='${requisitionItems.findAll { !it.isCanceled()&&!it.isChanged() }}'/>
    <g:set var="requisitionItemsColdChain" value='${requisitionItems.findAll { it?.product?.coldChain }}'/>
    <g:set var="requisitionItemsControlled" value='${requisitionItems.findAll {it?.product?.controlledSubstance && !it?.product?.coldChain}}'/>
    <g:set var="requisitionItemsHazmat" value='${requisitionItems.findAll {it?.product?.hazardousMaterial &&
            !it?.product?.controlledSubstance && !it?.product?.coldChain}}'/>
    <g:set var="requisitionItemsOther" value='${requisitionItems.findAll {!it?.product?.hazardousMaterial && !it?.product?.coldChain && !it?.product?.controlledSubstance}}'/>

    <div class="content">
        <g:if test="${requisitionItemsColdChain}">
            <h2>
                ${warehouse.message(code:'product.coldChain.label', default:'Cold chain')}
            </h2>
            <g:render template="printPage" model="[requisitionItems:requisitionItemsColdChain,
                                                   sortOrder:sortOrder,
                                                   pageBreakAfter: (requisitionItemsControlled||requisitionItemsHazmat||requisitionItemsOther)?'always':'avoid']"/>
        </g:if>
        <g:if test="${requisitionItemsControlled}">
            <h2 class="${requisitionItemsColdChain ? 'mt' : ''}">
                ${warehouse.message(code:'product.controlledSubstance.label', default:'Controlled Substance')}
            </h2>
            <g:render template="printPage" model="[requisitionItems:requisitionItemsControlled,
                                                   sortOrder:sortOrder,
                                                   pageBreakAfter: (requisitionItemsHazmat||requisitionItemsOther)?'always':'avoid']"/>
        </g:if>
        <g:if test="${requisitionItemsHazmat}">
            <h2 class="${requisitionItemsControlled||requisitionItemsColdChain ? 'mt' : ''}">
                ${warehouse.message(code:'product.hazardousMaterial.label', default:'Hazardous Material')}
            </h2>
            <g:render template="printPage" model="[requisitionItems:requisitionItemsHazmat,
                                                   sortOrder:sortOrder,
                                                   pageBreakAfter: (requisitionItemsOther)?'always':'avoid']"/>
        </g:if>
        <g:if test="${requisitionItemsOther}">
            <h2 class="${requisitionItemsHazmat||requisitionItemsControlled||requisitionItemsColdChain ? 'mt' : ''}">
                ${warehouse.message(code:'product.generalGoods.label', default:'General Goods')}
            </h2>
            <g:render template="printPage" model="[requisitionItems:requisitionItemsOther,
                                                   sortOrder:sortOrder,
                                                   pageBreakAfter: (requisitionItemsCanceled)?'always':'avoid']"/>
        </g:if>
        <g:if test="${requisitionItemsCanceled}">
            <h2 class="${requisitionItemsOther ? 'mt' : ''}">
                ${warehouse.message(code:'default.canceled.label', default:'Canceled Items')}
            </h2>
            <g:render template="printPage" model="[requisitionItems:requisitionItemsCanceled,
                                                   location:location,
                                                   sortOrder:sortOrder,
                                                   pageBreakAfter: 'avoid']"/>
        </g:if>

        <g:render template="notesSection" model="[shipment: requisition?.shipment]"/>
        <g:render template="signaturesSection"/>

    </div>
    <script type="text/javascript">
      $(document).ready(function() {
        $("#print-page").click(function(event){
          window.print();
          return false;
        });

        $("#select-orientation").change(function() {
          var selected = this.value;
          if ('URLSearchParams' in window) {
            var searchParams = new URLSearchParams(window.location.search);
            searchParams.set("orientation", selected);
            window.location.search = searchParams.toString();
          }
        });
      });
    </script>
</body>
</html>
