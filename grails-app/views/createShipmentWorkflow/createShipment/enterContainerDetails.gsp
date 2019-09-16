<%@ page import="org.pih.warehouse.shipping.ShipmentItem" %>

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="custom" />
    <title><warehouse:message code="shipping.addShipmentItems.label"/></title>

    <style>
    .ui-autocomplete { height: 250px; overflow-y: scroll; overflow-x: hidden;}
    .draggable { cursor: move; }
    .strikethrough { color: lightgrey; }
    #sortable { list-style-type: none; margin: 0; padding: 0; width: 60%; }
    #sortable tr { margin: 0 5px 5px 5px; padding: 5px; font-size: 1.2em; height: 1.5em; }
    html>body #sortable tr { height: 1.5em; line-height: 1.2em; }

    tr.selected {
        border-top: 1px solid lightgrey;
        border-bottom: 1px solid lightgrey;
        background-color: #ebf2f9;
    }
    tr.selected .containerName a {
        color: #666;
        font-weight: bold;
    }
    tr.not-selected {
        border-top: 1px solid lightgrey;
        border-bottom: 1px solid lightgrey;
    }

    </style>

</head>
<body>

<div class="body">
    <g:if test="${message}">
        <div class="message">${message}</div>
    </g:if>

    <g:if test="${shipmentInstance?.hasErrors()}">
        <div class="errors">
            <g:renderErrors bean="${shipmentInstance}" as="list" />
        </div>

    </g:if>
    <g:elseif test="${itemInstance?.hasErrors()}">
        <div class="errors">
            <g:renderErrors bean="${itemInstance}" as="list" />
        </div>
    </g:elseif>
    <g:elseif test="${containerInstance?.hasErrors()}">
        <div class="errors">
            <g:renderErrors bean="${containerInstance}" as="list" />
        </div>
    </g:elseif>

    <div>
        <g:render template="../shipment/summary" />
        <g:render template="flowHeader" model="['currentState':'Packing']"/>

        <!-- figure out what dialog box, if any, we need to render -->
        <g:if test="${containerToEdit || containerTypeToAdd}">
            <g:render template="editContainer" model="['container':containerToEdit, 'containerTypeToAdd':containerTypeToAdd]"/>
        </g:if>
        <g:if test="${containerToMove}">
            <g:render template="moveContainer" model="['container':containerToMove]"/>
        </g:if>
        <g:if test="${boxToEdit}">
            <g:render template="editBox" model="['box':boxToEdit, 'addBoxToContainerId':addBoxToContainerId]"/>
        </g:if>
        <g:if test="${addBoxToContainerId}">
            <g:render template="editBox" model="['box':boxToEdit, 'addBoxToContainerId':addBoxToContainerId]"/>
        </g:if>
        <g:if test="${itemToEdit && shipmentInstance?.destination?.id == session?.warehouse?.id}">
            <g:render template="addIncomingItem" model="['item':itemToEdit, 'addItemToContainerId':addItemToContainerId]"/>
        </g:if>
        <g:elseif test="${itemToEdit}">
            <g:render template="editItem" model="['item':itemToEdit, 'addItemToContainerId':addItemToContainerId]"/>
        </g:elseif>
        <g:if test="${addItemToContainerId && shipmentInstance?.destination?.id == session?.warehouse?.id}">
            <g:render template="addIncomingItem" model="['item':itemToEdit, 'addItemToContainerId':addItemToContainerId]"/>
        </g:if>
        <g:elseif test="${addItemToContainerId}">
            <g:render template="addItem" model="['item':itemToEdit, 'addItemToContainerId':addItemToContainerId]"/>
        </g:elseif>
        <g:if test="${addItemToShipmentId && shipmentInstance?.destination?.id == session?.warehouse?.id}">
            <g:render template="addIncomingItem" model="['item':itemToEdit, 'addItemToContainerId':0]"/>
        </g:if>
        <g:elseif test="${addItemToShipmentId }">
            <g:render template="addItem" model="['item':itemToEdit, 'addItemToContainerId':0]"/>
        </g:elseif>

        <g:if test="${itemToMove }">
            <g:render template="moveItem" model="['item':itemToMove]"/>
        </g:if>

        <hr/>
        <div class="buttonBar">
            <g:render template="shipmentButtons"/>
            <g:render template="containerButtons" model="[container:selectedContainer]"/>

            <div class="button-group">
                <g:link class="button icon reload" controller="createShipmentWorkflow" action="createShipment" event="enterContainerDetails" id="${shipmentInstance?.id}" params="[skipTo: 'Packing']">
                    <warehouse:message code="shipping.reload.label" default="Reload"/></g:link>
            </div>
        </div>


        <div class="yui-gd">
            <div class="yui-u first">


                ${flow?.shipmentInstance}
                <%-- Display the pallets & boxes in this shipment --%>

                <div class="box" >
                    <g:set var="count" value="${0 }"/>
                    <h2><warehouse:message code="containers.label"/></h2>

                    <g:form action="createShipment">
                        <g:hiddenField name="id" value="${shipmentInstance?.id}" />
                        <table class="sortable">
                            <thead>
                            <tr>
                                <td class="right middle" colspan="5">
                                    <g:link action="createShipment" event="enterContainerDetails" params="['containerId':selectedContainer?.id,'direction':'-1']" class="button icon arrowup">
                                        <g:message code="default.button.previous.label"/>
                                    </g:link>
                                    <g:link action="createShipment" event="enterContainerDetails" params="['containerId':selectedContainer?.id,'direction':'1']" class="button icon arrowdown">
                                        <g:message code="default.button.next.label"/>
                                    </g:link>
                                </td>
                            </tr>
                            </thead>
                            <tbody>
                            <!-- UNPACKED ITEMS -->
                            <g:set var="styleClass" value="${selectedContainer == null ? 'selected' : 'not-selected' }"/>
                            <tr class="droppable ${styleClass }" container="null">
                                <td width="1%">
                                    <g:checkBox name="containerId" value="${null }" checked="${false}" disabled="${true}"/>

                                </td>
                                <td class="left" width="5%">
                                    <span class="action-menu" >
                                        <button id="unpackedItemsActionBtn" class="action-btn">
                                            <img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
                                        </button>
                                        <div class="actions left">
                                            <g:render template="containerMenuItems" model="[container:containerInstance]"/>
                                            <g:render template="shipmentMenuItems"/>
                                        </div>
                                    </span>
                                </td>

                                <td class="middle">
                                    <div class="containerName">
                                        <g:link action="createShipment" event="enterContainerDetails" style="display: block;">
                                            <warehouse:message code="shipping.unpackedItems.label"/>
                                        </g:link>
                                    </div>
                                </td>
                                <td class="middle right">
                                    <g:link action="createShipment" event="enterContainerDetails" params="['containerId':null]">
                                        ${shipmentInstance?.countShipmentItemsByContainer(null)} items
                                    </g:link>
                                </td>
                                <td class="right">
                                    <span class="sorthandle"></span>
                                </td>
                            </tr>

                            <!-- ALL OTHER PALLETS, CRATES, BOXES -->
                            <g:if test="${shipmentInstance?.containers }">
                                <g:each var="containerInstance" in="${shipmentInstance?.findAllParentContainers()?.sort { it.sortOrder }}">
                                    <g:set var="styleClass" value="${containerInstance?.id == selectedContainer?.id ? 'selected' : 'not-selected' }"/>
                                    <tr id="container_${containerInstance?.id }" class="droppable ${styleClass } connectable parentContainer" container="${containerInstance?.id }">
                                        <td width="1%">
                                            <g:checkBox name="containerId" value="${containerInstance?.id}" checked="${false}"/>
                                        </td>
                                        <td class="left">
                                            <span class="action-menu">
                                                <button id="containerActionBtn-${containerInstance?.id }" class="action-btn">
                                                    <img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
                                                </button>
                                                <div class="actions left">
                                                    <g:render template="containerMenuItems" model="[container:containerInstance]"/>
                                                </div>
                                            </span>
                                        </td>

                                        <td style="vertical-align: middle;" id="${containerInstance?.id }" class="left">
                                            <div class="draggable draghandle tag" childContainer="${containerInstance?.id}" style="display:block;">
                                                <img src="${resource(dir: 'images/icons/silk', file: 'arrow_out_longer.png')}" class="middle"/>
                                                &nbsp;
                                                <span class="containerName">
                                                    <a name="container-${containerInstance.id }"></a>
                                                    <g:link action="createShipment" event="enterContainerDetails" params="['containerId':containerInstance?.id]">
                                                        ${containerInstance?.name}
                                                    </g:link>
                                                </span>
                                            </div>
                                        </td>
                                        <td class="middle right">
                                            <g:link action="createShipment" event="enterContainerDetails" params="['containerId':containerInstance?.id]">
                                                ${containerInstance?.shipmentItems?.size() } items
                                            </g:link>

                                        </td>
                                        <td class="right">
                                            <span class="sorthandle"></span>
                                        </td>
                                    </tr>

                                    <g:set var="childContainers" value="${shipmentInstance?.findAllChildContainers(containerInstance)?.sort() }"/>
                                    <g:each var="childContainerInstance" in="${childContainers}">
                                        <g:set var="styleClass" value="${childContainerInstance?.id == selectedContainer?.id ? 'selected' : 'not-selected' }"/>
                                        <tr id="container_${childContainerInstance?.id }" class="childContainer droppable ${styleClass }" container="${childContainerInstance?.id }">
                                            <td width="1%">
                                                <g:checkBox name="containerId" value="${childContainerInstance?.id}" checked="${false}"/>
                                            </td>
                                            <td class="left">
                                                <span class="action-menu" >
                                                    <button id="childContainerActionBtn-${childContainerInstance?.id }" class="action-btn">
                                                        <img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
                                                    </button>
                                                    <div class="actions" style="position: absolute; z-index: 1; display: none;">
                                                        <g:render template="containerMenuItems" model="[container:childContainerInstance]"/>
                                                    </div>
                                                </span>
                                            </td>

                                            <td class="middle">
                                                <div class="draggable draghandle tag" childContainer="${childContainerInstance?.id}">
                                                    <img src="${resource(dir: 'images/icons/silk', file: 'arrow_out_longer.png')}" class="middle"/>
                                                    &nbsp;

                                                    <span class="containerName">
                                                        <a name="container-${childContainerInstance.id }"></a>
                                                        <g:link action="createShipment" event="enterContainerDetails" params="['containerId':childContainerInstance?.id]">
                                                            ${childContainerInstance?.name}
                                                        </g:link>
                                                    </span>
                                                </div>
                                            </td>
                                            <td class="middle right">
                                                <g:link action="createShipment" event="enterContainerDetails" params="['containerId':childContainerInstance?.id]">
                                                    ${childContainerInstance?.shipmentItems?.size()?:0 } items
                                                </g:link>
                                            </td>
                                            <td class="right"></td>
                                        </tr>

                                    </g:each>
                                </g:each>
                            </g:if>
                            <tr class="droppable fade not-selected" container="trash" style="height: 44px;">
                                <td colspan="5" class="center middle">
                                    <div class="center">
                                        <img src="${resource(dir: 'images/icons/silk', file: 'bin_empty.png')}"/>
                                        <warehouse:message code="shipment.trash.label" default="Drop here to remove from shipment"/>

                                    </div>

                                </td>
                            </tr>

                            </tbody>
                            <tfoot>
                            <tr>
                                <td colspan="5">
                                    <div class="center">

                                        <a href="javascript:void(0);" class="btnAddContainers button">Add packing units</a>
                                        <g:submitButton name="deleteContainersAndItems" value="Delete Selected" class="button icon trash" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"></g:submitButton>
                                        <g:submitButton name="deleteAllContainersAndItems" value="Delete All" class="button icon trash" onclick="return confirm('${warehouse.message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"></g:submitButton>

                                    </div>
                                </td>
                            </tr>
                            </tfoot>
                        </table>
                    </g:form>
                </div>
            </div>
            <div class="yui-u">


                <div class="box">
                    <h2><g:message code="shipping.addItems.label"/></h2>
                    <div style="margin: 10px">
                        <g:form action="createShipment">
                            <g:hiddenField name="shipmentId" value="${shipmentInstance?.id}"/>
                            <g:hiddenField name="containerId" value="${selectedContainer?.id}"/>
                            <g:autoSuggest id="inventoryItem" name="inventoryItem" jsonUrl="${request.contextPath }/json/findInventoryItems"
                                           placeholder="Enter lot number or product code" styleClass="text" width="500" minLength="2" delay="500"/>
                            <g:textField name="quantity" value="" class="text" placeholder="Quantity" size="10"/>
                            <g:submitButton name="addShipmentItem" value="Add item" class="button icon add"></g:submitButton>
                        </g:form>
                    </div>
                </div>

                <%-- Display the contents of the currently selected container --%>
                <div class="box">
                    <h2>

                        <span class="middle">
                            <g:if test="${selectedContainer}">
                                <g:if test="${selectedContainer.parentContainer }">
                                    ${selectedContainer?.parentContainer?.name } &rsaquo;
                                </g:if>
                                ${selectedContainer?.name }
                            </g:if>
                            <g:else>
                                <warehouse:message code="shipping.unpackedItems.label" />
                            </g:else>
                        </span>
                        <span>
                            <g:link action="createShipment" event="enterContainerDetails" params="['containerId':selectedContainer?.id,'direction':'-1']" class="button icon arrowup">
                                <g:message code="default.button.previous.label"/>
                            </g:link>
                            <g:link action="createShipment" event="enterContainerDetails" params="['containerId':selectedContainer?.id,'direction':'1']" class="button icon arrowdown">
                                <g:message code="default.button.next.label"/>
                            </g:link>
                        </span>


                    </h2>
                    <table style="border: 0px solid lightgrey">
                        <tbody>
                            <tr>
                                <td class="left" style="width:1%;">
                                    <div class="action-menu" >
                                        <button id="selectedContainerActionBtn" class="action-btn">
                                            <img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
                                        </button>
                                        <div class="actions">
                                            <g:render template="containerMenuItems" model="[container:selectedContainer]"/>
                                            <g:render template="shipmentMenuItems" />

                                        </div>
                                    </div>
                                </td>
                                <td class="middle left">
                                    <div class="">
                                        <g:render template="/container/summary" model="[container:selectedContainer]"/>
                                    </div>
                                </td>
                            </tr>
                        </tbody>

                    </table>

                    <table>
                        <thead>
                        <tr class="prop">
                            <th class="middle"></th>
                            <th class="middle"></th>
                            <th class="middle center"><warehouse:message code="product.productCode.label"/></th>
                            <th class="middle"><warehouse:message code="product.label"/></th>
                            <th class="middle"><warehouse:message code="location.binLocation.label"/></th>
                            <th class="middle"><warehouse:message code="inventoryItem.lotNumber.label"/></th>
                            <th class="center middle"><warehouse:message code="inventoryItem.expirationDate.label"/></th>
                            <th class="left middle"><warehouse:message code="default.qty.label"/></th>
                            <th class="left middle"><warehouse:message code="default.uom.label"/></th>
                            <th class="middle"><warehouse:message code="shipping.recipients.label"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:set var="shipmentItemsByContainer" value="${shipmentInstance?.shipmentItems?.groupBy { it.container } ?: [:]}"/>
                        <g:set var="shipmentItems" value="${shipmentItemsByContainer[selectedContainer]}"/>
                        <g:if test="${shipmentItems }">
                            <g:set var="count" value="${0 }"/>
                            <g:each var="shipmentItem" in="${shipmentItems?.sort()}">
                                <tr id="shipmentItemRow-${shipmentItem?.id }" class="${(count++%2)?'odd':'even' }">

                                    <td nowrap="nowrap" class="left">
                                        <div class="action-menu">
                                            <button id="shipmentItemActionBtn-${shipmentItem?.id }" class="action-btn">
                                                <img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
                                            </button>
                                            <div class="actions">
                                                <g:render template="itemMenuItems" model="[itemInstance:shipmentItem]"/>
                                            </div>
                                        </div>
                                    </td>
                                    <td>
                                        <div class="draggable draghandle tag" shipmentItem="${shipmentItem?.id }">
                                            <img src="${resource(dir: 'images/icons/silk', file: 'arrow_out_longer.png')}" class="middle"/>
                                            &nbsp;
                                            ${shipmentItem?.product?.productCode}
                                            (${shipmentItem?.quantity}
                                            ${shipmentItem?.inventoryItem?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label')})
                                        </div>
                                    </td>
                                    <td class="center middle">
                                        ${shipmentItem?.product?.productCode}
                                    </td>
                                    <td class="middle">
                                        <div>
                                            <g:link controller="inventoryItem" action="showStockCard" params="['product.id':shipmentItem?.inventoryItem?.product?.id]">
                                                <format:product product="${shipmentItem?.inventoryItem?.product}"/>
                                            </g:link>
                                        </div>
                                    </td>
                                    <td class="middle">
                                        <div class="binLocation">
                                            <g:if test="${shipmentItem?.binLocation}">
                                                ${shipmentItem?.binLocation?.name}
                                            </g:if>
                                            <g:else>
                                                <g:message code="default.label"/>
                                            </g:else>
                                        </div>
                                    </td>
                                    <td class="middle">
                                        <div class="lotNumber">
                                            ${shipmentItem?.inventoryItem?.lotNumber}
                                        </div>
                                    </td>
                                    <td class="center middle">
                                        <g:if test="${shipmentItem?.inventoryItem?.expirationDate}">
                                            <format:date obj="${shipmentItem?.inventoryItem?.expirationDate}" format="MMM yyyy"/>
                                        </g:if>
                                    </td>
                                    <td class="left middle">
                                        ${shipmentItem?.quantity}
                                    </td>
                                    <td class="left middle">
                                        ${shipmentItem?.inventoryItem?.product?.unitOfMeasure?:warehouse.message(code:'default.each.label')}
                                    </td>

                                    <td class="left middle">
                                        <div title="${shipmentItem?.recipient?.email}">
                                            ${shipmentItem?.recipient?.name?:warehouse.message(code:'default.none.label')}
                                        </div>
                                    </td>
                                </tr>
                            </g:each>
                            <tr class="droppable fade not-selected" container="trash" style="height: 44px;">
                                <td colspan="10" class="center middle">
                                    <div class="center">
                                        <img src="${resource(dir: 'images/icons/silk', file: 'bin_empty.png')}"/>
                                        <warehouse:message code="shipment.trash.label" default="Drop here to remove from shipment"/>
                                    </div>

                                </td>
                            </tr>

                        </g:if>
                        <g:unless test="${shipmentItems }">
                            <tr class="none">
                                <td colspan="10">
                                    <div class="middle center fade empty">
                                        <warehouse:message code="shipment.noShipmentItems.message"/>
                                    </div>
                                </td>
                            </tr>
                        </g:unless>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

    </div>
    <div class="buttons">
        <g:form action="createShipment" method="post" >
            <button name="_eventId_back" class="button">&lsaquo; <warehouse:message code="default.button.back.label"/></button>
            <button name="_eventId_next" class="button"><warehouse:message code="default.button.next.label"/> &rsaquo;</button>
            <button name="_eventId_save" class="button"><warehouse:message code="default.button.saveAndExit.label"/></button>
            <button name="_eventId_cancel" class="button"><warehouse:message code="default.button.cancel.label"/></button>
        </g:form>
    </div>
</div>


<div id="dlgAddContents" title="Import Packing List">
    <div>
    <!-- process an upload or save depending on whether we are adding a new doc or modifying a previous one -->
        <g:uploadForm action="createShipment">
            <g:hiddenField name="id" value="${shipmentInstance?.id}" />
            <table>
                <tbody>
                <tr class="prop">
                    <td valign="top" class="name"><label><warehouse:message
                            code="document.selectFile.label" /></label>
                    </td>
                    <td valign="top" class="value">
                        <input name="fileContents" type="file" />
                    </td>
                </tr>
                </tbody>
                <tfoot>
                <tr>
                    <td></td>
                    <td>
                        <g:submitButton name="importPackingList" value="Import Packing List" class="button icon add"></g:submitButton>
                    </td>
                </tr>
                </tfoot>
            </table>
        </g:uploadForm>
    </div>
</div>

<div id="dlgAddContainers" title="Add packages">
    <div >
    <!-- process an upload or save depending on whether we are adding a new doc or modifying a previous one -->
        <g:form action="createShipment">
            <g:hiddenField name="id" value="${shipmentInstance?.id}" />
            <table>
                <tbody>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label><warehouse:message code="default.name.label" /></label>
                    </td>
                    <td valign="top" class="value">
                        <g:textField id="container-text-name" name="name" value="" placeholder="Enter the default name (e.g. Pallet, Box)" class="text medium containerTextComponent" size="80"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label><warehouse:message code="container.type.label" /></label>
                    </td>
                    <td valign="top" class="value">
                        <g:select name="containerTypeId" optionKey="id" optionValue="name"
                                  from="${org.pih.warehouse.shipping.ContainerType.list()}" noSelection="['':'']" class="chzn-select-deselect containerTextComponent"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label><warehouse:message code="shipment.numberOfContainers.label" default="Number of containers"/></label>
                    </td>
                    <td valign="top" class="value">
                        <g:textField id="container-text-count" name="numberOfContainers" value="" placeholder="How many containers do you want to create?" class="text medium containerTextComponent"  size="80"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label><warehouse:message code="shipment.startIndex.label" default="Starting index"/></label>
                    </td>
                    <td valign="top" class="value">
                        <g:textField id="container-text-start" name="start" value="${1}" placeholder="Enter the number you want to start with" class="text medium containerTextComponent"  size="80"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label><warehouse:message code="shipment.parentContainer.label" default="Parent Container"/></label>
                    </td>
                    <td valign="top" class="value">
                        <g:select name="containerId" class="chzn-select-deselect containerTextComponent"
                                  optionKey="id" optionValue="name" noSelection="['':'Choose parent container or leave empty']"
                                  from="${shipmentInstance?.findAllParentContainers()}" value="${selectedContainer?.id}"></g:select>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label><warehouse:message code="shipment.containerText.label" default="Create the following containers"/></label>

                    </td>
                    <td valign="top" class="value">
                        <g:textArea id="container-text" name="containerText" rows="10" style="width:100%"></g:textArea>
                    </td>
                </tr>


                </tbody>
            </table>
            <div class="buttons">
                <g:submitButton name="addContainers" value="Add packing units" class="button icon add"></g:submitButton>

            </div>

        </g:form>
    </div>
</div>

<script>

    function refreshContainerText() {
        var name = $("#container-text-name").val() || "Container";
        var start = $("#container-text-start").val() || 1;
        var count = $("#container-text-count").val() || 1;
        var end = parseInt(start) + parseInt(count)
        var containerText = ""
        for (var i=start; i<end; i++) {
            containerText += (name + " " + i);
            if (i<end-1) {
                containerText += "\n"
            }
        }
        $("#container-text").val(containerText);
    }
</script>
<script>

    $(document).ready(function() {

        $(".containerTextComponent").change(function(event) {
            refreshContainerText();
        });

        $("#btnAddContents").click(function(event){
            $("#dlgAddContents").dialog('open');
        });
        $("#dlgAddContents").dialog({
            autoOpen: false,
            modal: true,
            width: 600
        });
    });
</script>
<script>
    $(document).ready(function() {
        $(".btnAddContainers").click(function(event){
            $("#dlgAddContainers").dialog('open');
        });
        $("#dlgAddContainers").dialog({
            autoOpen: false,
            modal: true,
            width: 800
        });
    });
</script>

<script>
    $(document).ready(function() {
        var sortable = $(".sortable tbody").sortable({
            handle : '.sorthandle',
            axis : "y",
            helper: "clone",
            forcePlaceholderSize: true,
            containment: ".sortable",
            placeholder: "ui-state-highlight",
            connectWith: ".connectable",
            items: "> tr.parentContainer",
            update : function() {
                var sortOrder = $(this).sortable('serialize');
                console.log(sortOrder);
                $.ajax ({
                    type: "POST",
                    url: "${request.contextPath}/createShipmentWorkflow/createShipment",
                    data: sortOrder + "&_eventId=sortContainers&execution=${request.flowExecutionKey}",
                    dataType: "json",
                    cache: false,
                    success: function(data) {
                        console.log(data);
                    },
                    error: function (jqXHR, textStatus, errorThrown) {
                        console.log(jqXHR);
                        console.log(textStatus);
                        console.log(errorThrown);
                    }
                });
                // Refresh page to make sure that changes are refreshed
                location.reload();
            }
        });

        $('.draggable').draggable({
            handle		: ".draghandle",
            helper		: "clone",
            revert		: true,
            zIndex		: 2700,
            autoSize	: true,
            ghosting	: true,
            onStart		: function ( event ) { alert("started") },
            onStop		: function() { $('.droppable').each(function() { this.expanded = false; }); }
        });

        $('.droppable').droppable( {
            accept: '.draggable',
            tolerance: 'intersect',
            over: function(event, ui) {
                console.log(event);
                console.log(ui);
                console.log($(this));
                $( this ).addClass( "ui-state-highlight" );
            },
            out: function(event, ui) {
                $( this ).removeClass( "ui-state-highlight" );
            },
            drop: function( event, ui ) {
                var shipmentItem = ui.draggable.attr("shipmentItem");
                var childContainer = ui.draggable.attr("childContainer");
                var parentContainer = $(this).attr("container");

                if (shipmentItem) {
                    moveShipmentItemToContainer(shipmentItem, parentContainer);
                }
                else if (childContainer) {
                    if (childContainer != parentContainer) {
                        moveContainerToContainer(childContainer, parentContainer);
                    }
                }

                $("#shipmentItemRow-" + shipmentItem).hide();
                console.log(shipmentItem);
                console.log(childContainer);
                console.log(parentContainer);
                location.reload();
                $(this).removeClass("ui-state-highlight");

            }
        });
    });

    function changeQuantity() {
        var totalQuantity = $("#totalQuantity").val();
        var updateQuantity = getUpdateQuantity();
        var currentQuantity = parseInt(totalQuantity) - parseInt(updateQuantity);
        if (currentQuantity >= 0) {
            $("#currentQuantity").val(currentQuantity);
        }
        else {
            alert("Please specify values that total the initial quantity of " + totalQuantity);
            $(this).val(0);
            $(this).focus();
        }
    }

    function getTotalQuantity() {
        var currentQuantity = $("#currentQuantity").val();
        var updateQuantity = getUpdateQuantity();
        return parseInt(currentQuantity) + parseInt(updateQuantity);
    }

    function getUpdateQuantity() {
        var updateQuantity = 0;
        $(".updateQuantity").each(function() {
            updateQuantity += Number($(this).val());
        });
        return updateQuantity;
    }

    function moveShipmentItemToContainer(shipmentItem, container) {
        var data = "shipmentItem=" + shipmentItem + "&container=" + container
        $.ajax({
            type: "POST",
            url: "${request.contextPath}/createShipmentWorkflow/createShipment",
            data: data + "&_eventId=moveShipmentItemToContainer&execution=${request.flowExecutionKey}",
            dataType: "json",
            cache: false,
            success: function (data) {},
            error: function (jqXHR, textStatus, errorThrown) {}
        });
    }

    function moveContainerToContainer(childContainer, parentContainer) {
        var data = "childContainer=" + childContainer + "&parentContainer=" + parentContainer;

        $.ajax({
            type: "POST",
            url: "${request.contextPath}/createShipmentWorkflow/createShipment",
            data: data + "&_eventId=moveContainerToContainer&execution=${request.flowExecutionKey}",
            dataType: "json",
            cache: false,
            success: function (data) {},
            error: function (jqXHR, textStatus, errorThrown) {}
        });
    }

</script>
<script>
    $(document).ready(function() {
        $("#inventoryItem-suggest").livequery(function () {
            console.log($(this));
            $(this).focus();
        });
    });
</script>


</body>
</html>
