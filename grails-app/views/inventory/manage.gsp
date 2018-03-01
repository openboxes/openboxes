<%@ page import="org.pih.warehouse.core.Location" %>
<%@ page import="org.pih.warehouse.product.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'inventory.label', default: 'Inventory')}" />
        <title><warehouse:message code="inventory.manage.label" default="Browse inventory"/></title>
        <style>
            tr.newProduct { border-top: 1px dotted black; }
        </style>
        <link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.tagsinput/',file:'jquery.tagsinput.css')}" type="text/css" media="screen, projection" />

    </head>
    <body>
        <div class="body">

            <div class="summary">
                <div class="title">
                    <warehouse:message code="inventory.manage.label" default="Manage inventory"/>
                </div>
            </div>

            <g:if test="${flash.message}">
                <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${command}">
                <div class="errors">
                    <g:renderErrors bean="${command}" as="list" />
                </div>
            </g:hasErrors>



            <div class="buttonBar" style="text-align: right">
                <g:link controller="inventory" action="manage" params="[type:'list', tags: params.tags, productCodes: params.productCodes]">List View</g:link> &nbsp;|&nbsp;
                <g:link controller="inventory" action="manage" params="[type:'tabs', tags: params.tags, productCodes: params.productCodes]">Tab View</g:link>
            </div>
            <div class="dialog">

                <div class="yui-gf">
					<div class="yui-u first">

                        <div class="filters" >
                            <g:form method="GET" controller="inventory" action="manage">
                                <div class="box">
                                    <h2><warehouse:message code="inventory.filterByProduct.label"/></h2>
                                    <table>
                                        <tr>
                                            <td>
                                                <label><g:message code="product.tag.label"/></label>
                                                <g:selectTags name="tags" noSelection="['':'']"
                                                              value="${command.tags}"
                                                              data-placeholder="Select tags"
                                                              class="chzn-select-deselect"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>
                                                <label><g:message code="product.productCode.label"/></label>
                                                <g:textField id="productCodes" name="productCodes"
                                                             value="${command?.productCodes}" placeholder="Add products by product code"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>
                                                <div class="buttons">
                                                    <button type="submit" class="button icon search" name="searchPerformed" value="true">
                                                        <warehouse:message code="default.search.label"/>
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    </table>
                                </div>


                            </g:form>
                        </div>
                    </div>
					<div class="yui-u">

                        <form method="POST" action="saveInventoryChanges">
                            <div class="box">
                                <h2><g:message code="inventory.manage.label"/></h2>
                                <table>
                                    <tr class="prop">
                                        <td class="name">
                                            <g:message code="transaction.type.label"/>
                                        </td>
                                        <td class="value">
                                            <g:message code="consumption.label"/>
                                            <%--<g:selectTransactionType name="transactionType" class="chzn-select-deselect"/>--%>
                                            <g:hiddenField name="transactionType.id" value="2"/>
                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <td class="name">
                                            <g:message code="transaction.transactionDate.label"/>
                                        </td>
                                        <td class="value">
                                            <g:datePicker name="transactionDate" value="${new Date()}"/>
                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <td class="name">
                                            <g:message code="comments.label"/>
                                        </td>
                                        <td class="value">
                                            <g:textArea name="comment" class="text large"></g:textArea>
                                        </td>
                                    </tr>
                                </table>
                            </div>
                            <g:if test="${params.type=='tabs'}">

                                <div class="tabs">
                                    <g:if test="${command?.inventoryItems}">
                                        <g:set var="inventoryItemsMap" value="${command?.inventoryItems?.groupBy { it.product.category } }"/>
                                        <ul>
                                            <g:each var="category" in="${inventoryItemsMap.keySet()}">
                                                <li><a href="#${category.id}">${category.name}</a></li>
                                            </g:each>
                                        </ul>
                                        <g:each var="category" in="${inventoryItemsMap.keySet()}">
                                            <div id="${category.id}">
                                                <div class="box dialog">
                                                    <h2>${category.name}</h2>
                                                    <table>
                                                        <thead>
                                                            <tr>
                                                                <th>

                                                                </th>
                                                                <th class="middle" style="width: 1%">
                                                                    <g:message code="product.productCode.label"/>
                                                                </th>
                                                                <th class="middle">
                                                                    <g:message code="product.name.label"/>
                                                                </th>
                                                                <th class="middle">
                                                                    <g:message code="inventoryItem.lotNumber.label"/>
                                                                </th>
                                                                <th class="middle">
                                                                    <g:message code="inventoryItem.expirationDate.label"/>
                                                                </th>
                                                                <th class="middle">
                                                                    <g:message code="default.quantityOnHand.label"/>
                                                                </th>
                                                                <th class="middle">
                                                                    <g:message code="default.quantityUsed.label"/>
                                                                </th>
                                                            </tr>
                                                        </thead>
                                                        <tbody>

                                                            <g:each var="entry" in="${inventoryItemsMap[category]}" status="i">
                                                                <g:set var="inventoryItem" value="${entry.inventoryItem}"/>
                                                                <g:set var="quantityOnHand" value="${entry.quantityOnHand}"/>
                                                                <g:set var="inventoryItems" value="${entry[category]}"/>
                                                                <tr class="${i%2==0?'even':'odd' } product prop">
                                                                    <td class="center middle">
                                                                        <g:if test="${inventoryItem?.product?.images }">
                                                                            <div class="nailthumb-container">
                                                                                <g:set var="image" value="${inventoryItem?.product?.images?.sort()?.first()}"/>
                                                                                <img src="${createLink(controller:'product', action:'renderImage', id:image.id)}" style="display:none" />
                                                                            </div>
                                                                        </g:if>
                                                                        <g:else>
                                                                            <div class="nailthumb-container">
                                                                                <img src="${resource(dir: 'images', file: 'default-product.png')}" style="display:none" />
                                                                            </div>
                                                                        </g:else>
                                                                    </td>
                                                                    <td class="center middle">
                                                                        ${inventoryItem?.product?.productCode }
                                                                    </td>
                                                                    <td class="left middle">
                                                                        <g:link name="productLink" controller="inventoryItem" action="showStockCard" params="['product.id':inventoryItem?.product?.id]" fragment="inventory" style="z-index: 999">
                                                                            <div title="${inventoryItem?.product?.description }" class="popover-trigger" data-id="${inventoryItem?.product?.id }">
                                                                                ${inventoryItem?.product?.name}
                                                                            </div>
                                                                        </g:link>
                                                                    </td>
                                                                    <td class="middle left">
                                                                        <div class="lot">${inventoryItem?.lotNumber }</div>
                                                                    </td>
                                                                    <td>
                                                                        <g:expirationDate date="${inventoryItem?.expirationDate}"/>
                                                                    </td>
                                                                    <td class="middle">
                                                                        ${quantityOnHand}
                                                                    </td>
                                                                    <td class="middle">
                                                                        <input type="number" name="quantityUsed" class="text number" size="10" autocomplete="off" min="0" max="${quantityOnHand}" />
                                                                    </td>
                                                                </tr>
                                                            </g:each>
                                                        </tbody>
                                                    </table>
                                                </div>
                                            </div>
                                        </g:each>
                                    </g:if>
                                </div>
                            </g:if>
                            <g:else>
                                <g:if test="${command?.inventoryItems}">
                                    <g:if test="${params.list('tags').size() == 1}">
                                        <g:hiddenField name="tags" value="${params.tags}"/>
                                    </g:if>
                                    <g:else>
                                        <g:each var="tag" in="${params.tags}">
                                            <g:hiddenField name="tags" value="${tag}"/>
                                        </g:each>
                                    </g:else>


                                    <div class="box dialog">
                                        <h2><g:message code="inventory.manage.label"/></h2>
                                        <table>
                                            <tr>
                                                <th>

                                                </th>
                                                <th class="middle" style="width: 1%">
                                                    <g:message code="product.productCode.label"/>
                                                </th>
                                                <th class="middle">
                                                    <g:message code="product.name.label"/>
                                                </th>
                                                <th class="middle">
                                                    <g:message code="inventoryItem.lotNumber.label"/>
                                                </th>
                                                <th class="middle">
                                                    <g:message code="inventoryItem.expirationDate.label"/>
                                                </th>
                                                <th class="middle">
                                                    <g:message code="default.quantityOnHand.label"/>
                                                </th>
                                                <th class="middle">
                                                    <g:message code="default.quantityUsed.label"/>
                                                </th>
                                            </tr>
                                            <g:set var="showProduct" value="${true}"/>
                                            <g:each var="entry" in="${command?.inventoryItems}" status="i">
                                                <g:set var="inventoryItem" value="${entry.inventoryItem}"/>
                                                <g:set var="quantityOnHand" value="${entry.quantityOnHand}"/>
                                                <g:set var="newProduct" value="${product != entry.product}"/>

                                                <tr class="prop ${i%2==0?'even':'odd' } ${newProduct?'newProduct':''}">
                                                    <td class="center middle">
                                                        <g:if test="${inventoryItem?.product?.images }">
                                                            <div class="nailthumb-container">
                                                                <g:set var="image" value="${inventoryItem?.product?.images?.sort()?.first()}"/>
                                                                <img src="${createLink(controller:'product', action:'renderImage', id:image.id)}" style="display:none" />
                                                            </div>
                                                        </g:if>
                                                        <g:else>
                                                            <div class="nailthumb-container">
                                                                <img src="${resource(dir: 'images', file: 'default-product.png')}" style="display:none" />
                                                            </div>
                                                        </g:else>
                                                    </td>
                                                    <td class="center middle">
                                                        ${inventoryItem?.product?.productCode }
                                                    </td>
                                                    <td class="left middle">
                                                        <g:link name="productLink" controller="inventoryItem" action="showStockCard" params="['product.id':inventoryItem?.product?.id]" fragment="inventory" style="z-index: 999">
                                                            <div title="${inventoryItem?.product?.description }" class="popover-trigger" data-id="${inventoryItem?.product?.id }">
                                                                ${inventoryItem?.product?.name}
                                                            </div>
                                                        </g:link>
                                                    </td>
                                                    <td class="middle left">
                                                        <div class="lot">${inventoryItem?.lotNumber }</div>
                                                    </td>
                                                    <td class="middle">
                                                        <g:expirationDate date="${inventoryItem?.expirationDate}"/>
                                                    </td>
                                                    <td class="middle">
                                                        ${quantityOnHand}
                                                    </td>
                                                    <td class="middle">
                                                        <g:hiddenField name="entries[${i}].inventoryItem.id" value="${inventoryItem?.id}"/>
                                                        <input type="number" name="entries[${i}].quantity" class="text number" size="10" autocomplete="off" min="0" max="${quantityOnHand}" value="${0}" />
                                                    </td>
                                                </tr>
                                                <g:set var="product" value="${entry.product}"/>

                                            </g:each>
                                        </table>
                                    </div>
                                </g:if>
                                <g:unless test="${command?.inventoryItems}">
                                    <div class="box">
                                        <h2></h2>
                                        <div class="center fade empty">
                                            None
                                        </div>
                                    </div>
                                </g:unless>
                            </g:else>
                            <div class="buttons">
                                <button class="button"><g:message code="default.button.save.label"/></button>
                            </div>
                        </form>
					</div>
				</div>
			</div>
		</div>
        <script src="${createLinkTo(dir:'js/jquery.nailthumb', file:'jquery.nailthumb.1.1.js')}" type="text/javascript" ></script>
        <script src="${createLinkTo(dir:'js/jquery.tagcloud', file:'jquery.tagcloud.js')}" type="text/javascript" ></script>
        <script src="${createLinkTo(dir:'js/jquery.tagsinput/', file:'jquery.tagsinput.js')}" type="text/javascript" ></script>

        <script>
			$(document).ready(function() {
                $(".tabs").tabs({
                    cookie : {
                        expires : 1
                    }
                });

                $('#productCodes').tagsInput({
                    'autocomplete_url':'${createLink(controller: 'json', action: 'findProductCodes')}',
                    'width': 'auto',
                    'height': 'auto',
                    'placeholder':'test',
                    'removeWithBackspace' : true
                });
			});	
		</script>
    </body>
</html>
