<%@ page import="grails.converters.JSON; org.pih.warehouse.core.RoleType"%>
<%@ page import="org.pih.warehouse.requisition.RequisitionType"%>
<%@ page contentType="text/html;charset=UTF-8"%>
<html>
<head>
    <meta name="layout" content="custom" />
    <g:set var="entityName" value="${warehouse.message(code: 'requisition.label', default: 'Requisition')}" />
    <title><warehouse:message code="${requisition?.id ? 'default.edit.label' : 'default.create.label'}" args="[entityName]" /></title>
    <link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.tagsinput/',file:'jquery.tagsinput.css')}" type="text/css" media="screen, projection" />
    <script src="${createLinkTo(dir:'js/jquery.tagsinput/', file:'jquery.tagsinput.js')}" type="text/javascript" ></script>
    <style>
    .sortable { list-style-type: none; margin: 0; padding: 0; width: 100%; }
    .sortable tr { margin: 0 5px 5px 5px; padding: 5px; font-size: 1.2em; height: 1.5em; }
    html>body .sortable li { height: 1.5em; line-height: 1.2em; }
    .ui-state-highlight { height: 1.5em; line-height: 1.2em; }
    </style>
</head>
<body>

<g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
</g:if>
<g:if test="${flash.errors}">
    <div class="errors">
        <ul>
            <g:each var="error" in="${flash.errors}">
                <li>${error}</li>
            </g:each>
        </ul>
    </div>
</g:if>
<g:hasErrors bean="${requisition}">
    <div class="errors">
        <g:renderErrors bean="${requisition}" as="list" />
    </div>
</g:hasErrors>
<%--
<g:render template="summary" model="[requisition:requisition]"/>
--%>

<g:render template="summary" model="[requisition:requisition]"/>

<div class="yui-gd">
    <div class="yui-u first">
        <g:render template="header" model="[requisition:requisition]"/>

    </div>
    <div class="yui-u">


        <div class="tabs">
            <ul>
                <li>
                    <a href="#importAsFile">
                        ${warehouse.message(code:'requisitionTemplate.importAsFile.label', default: "Import as file")}
                    </a>
                </li>
                <li>
                    <a href="#importAsString">
                        ${warehouse.message(code:'requisitionTemplate.importAsString.label', default: "Import as string")}
                    </a>
                </li>
                <li>
                    <a href="#addToRequisitionItems">
                        ${warehouse.message(code:'requisitionTemplate.bulkAddByProductCodes.label', default: "Bulk add by product codes")}
                    </a>

                </li>
            </ul>



            <div id="importAsFile">

                <div class="box">
                    <h2>${warehouse.message(code:'requisitionTemplate.addRequisitionItems.label', default: "Upload CSV/TSV")}</h2>
                    <g:uploadForm controller="requisitionTemplate" action="importAsFile">
                        <g:hiddenField name="id" value="${requisition?.id}" />
                        <g:hiddenField name="version" value="${requisition?.version}" />
                        <table>
                            <tbody>
                            <tr class="prop">
                                <td valign="top" class="name"><label><warehouse:message
                                        code="document.selectFile.label" /></label>
                                </td>
                                <td valign="top" class="value">
                                    <input name="file" type="file" />
                                    &nbsp;
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="top name">
                                    <label>${warehouse.message(code:'requisitionTemplate.delimiter.label', default: 'Column delimiter')}</label>
                                </td>
                                <td class="middle">
                                    <g:radio name="delimiter" value="," checked="${params.delimiter.equals(',')||!params.delimiter}"/> Comma
                                    <g:radio name="delimiter" value="\t" checked="${params.delimiter.equals('\t')}"/> Tab
                                </td>
                            </tr>
                            <tr class="prop">
                                <td class="top name">
                                    <label>${warehouse.message(code:'requisitionTemplate.skipLines.label', default: 'Skip lines')}</label>
                                </td>
                                <td class="middle">
                                    <g:textField name="skipLines" value="${params.skipLines?:1}" class="text"/>
                                </td>
                            </tr>

                            </tbody>
                            <tfoot>
                            <tr>
                                <td></td>
                                <td>
                                    <!-- show upload or save depending on whether we are adding a new doc or modifying a previous one -->
                                    <button type="submit" class="button">
                                        ${warehouse.message(code:'default.button.upload.label')}</button>
                                </td>
                            </tr>
                            </tfoot>
                        </table>
                    </g:uploadForm>
                </div>
            </div>

            <div id="importAsString">
                <div class="box">

                    <h2>${warehouse.message(code:'requisitionTemplate.addRequisitionItems.label', default: "Copy-and-paste CSV/TSV")}</h2>
                    <g:form method="post" controller="requisitionTemplate" action="importAsString">
                        <g:hiddenField name="id" value="${requisition?.id}" />
                        <g:hiddenField name="version" value="${requisition?.version}" />
                        <table>
                            <tbody>
                                <tr class="prop">
                                    <td class="top name">
                                        <label>${warehouse.message(code:'requisitionTemplate.delimiter.label', default: 'Column delimiter')}</label>
                                    </td>
                                    <td class="middle">
                                        <g:radio name="delimiter" value="," checked="${params.delimiter.equals(',')||!params.delimiter}"/> Comma
                                        <g:radio name="delimiter" value="\t" checked="${params.delimiter.equals('\t')}"/> Tab
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td class="top name">
                                        <label>${warehouse.message(code:'requisitionTemplate.skipLines.label', default: 'Skip lines')}</label>
                                    </td>
                                    <td class="middle">
                                        <g:textField name="skipLines" value="${params.skipLines?:1}" class="text"/>
                                    </td>
                                </tr>
                                <tr class="prop">
                                    <td class="name">
                                        <label>${warehouse.message(code:'requisitionTemplate.data.label', default: 'Data')}</label>
                                    </td>
                                    <td class="value">
                                        <g:textArea name="csv" rows="10" class="large">Product Code,Product Name,Quantity,Unit of Measure</g:textArea>
                                    </td>
                                </tr>
                            </tbody>
                            <tfoot>
                                <tr>
                                    <td></td>
                                    <td class="left" colspan="4">
                                        <button class="button">
                                            ${warehouse.message(code:'requisitionTemplate.process.label', default: 'Import')}
                                        </button>
                                    </td>
                                </tr>
                            </tfoot>
                        </table>
                    </g:form>
                </div>
            </div>
            <div id="addToRequisitionItems">
                <div class="box">
                    <h2>${warehouse.message(code:'requisitionTemplate.bulkAddByProductCodes.label', default: "Bulk add by product codes")}</h2>

                    <g:form method="post" controller="requisitionTemplate" action="addToRequisitionItems">
                        <g:hiddenField name="id" value="${requisition?.id}" />
                        <g:hiddenField name="version" value="${requisition?.version}" />
                        <table>
                            <tr>
                                <td width="75%">
                                    <g:textField id="productCodesInput" name="multipleProductCodes" value="" class="text large"/>
                                </td>
                                <td class="left">
                                    <button class="button icon add">
                                        ${warehouse.message(code:'requisitionTemplate.addToProducts.label', default: 'Add to products')}
                                    </button>
                                </td>
                            </tr>
                        </table>
                    </g:form>
                </div>
            </div>
        </div>
        <g:if test="${data}">
            <div class="box">
                <h2><warehouse:message code="default.import.label" args="[warehouse.message(code:'default.data.label')]"/></h2>

                <g:form method="post" controller="requisitionTemplate" action="doImport">
                    <g:hiddenField name="id" value="${requisition?.id}" />
                    <g:hiddenField name="version" value="${requisition?.version}" />
                    <table>
                        <thead>
                            <tr>
                                <th>${warehouse.message(code:'default.row.label', default: 'Row')}</th>
                                <th>${warehouse.message(code:'product.productCode.label', default: 'Product code')}</th>
                                <th>${warehouse.message(code:'product.label', default: 'Product')}</th>
                                <th>${warehouse.message(code:'default.qty.label', default: 'Qty')}</th>
                                <th>${warehouse.message(code:'default.uom.label', default: 'UOM')}</th>
                            </tr>
                        </thead>
                        <tbody>
                        <g:each var="row" in="${data}" status="count">
                            <tr class="${count%2?'even':'odd'}">
                                <td>${count+1}</td>
                                <g:each var="column" in="${row}">
                                    <td>${column}</td>
                                </g:each>
                            </tr>
                        </g:each>
                        </tbody>
                        <tfoot>
                            <tr>
                                <td class="center" colspan="5">
                                    <button class="button icon approve">
                                        ${warehouse.message(code:'requisitionTemplate.save.label', default: 'Save')}
                                    </button>
                                </td>
                            </tr>
                        </tfoot>
                    </table>
                </g:form>
            </div>
        </g:if>
        <%--
        <div class="box">
            <h2>${warehouse.message(code:'requisitionTemplate.requisitionItems.label')}</h2>
            <g:form name="requisitionItemForm" method="post" controller="requisitionTemplate" action="update">


                <g:hiddenField name="id" value="${requisition.id}"/>
                <g:hiddenField name="version" value="${requisition.version}"/>

                <div>
                    <table  class="sortable" data-update-url="${createLink(controller:'json', action:'sortRequisitionItems')}">
                        <thead>
                        <tr>
                            <th></th>
                            <th><warehouse:message code="product.productCode.label" default="#"/></th>
                            <th><warehouse:message code="product.label"/></th>
                            <th><warehouse:message code="default.quantity.label"/></th>
                            <th><warehouse:message code="unitOfMeasure.label"/></th>
                            <th><warehouse:message code="default.actions.label"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:each var="requisitionItem" in="${requisition?.requisitionItems}" status="i">
                            <tr class="prop ${i%2?'even':'odd'}" id="requisitionItem_${requisitionItem?.id }" requisitionItem="${requisitionItem?.id}">
                                <td>
                                    <span class="sorthandle"></span>
                                </td>
                                <td>
                                    ${requisitionItem?.product?.productCode}
                                </td>
                                <td>
                                    <g:hiddenField name="requisitionItems[${i}].product.id" value="${requisitionItem?.product?.id}"/>
                                    <g:link controller="inventoryItem" action="showStockCard" id="${requisitionItem?.product?.id}">
                                        ${requisitionItem?.product?.name}
                                    </g:link>
                                </td>
                                <td>
                                    <g:textField name="requisitionItems[${i}].quantity" value="${requisitionItem?.quantity}" class="text" size="6"/>
                                </td>
                                <td>
                                    <g:selectUnitOfMeasure name="requisitionItems[${i}].productPackage.id" class="chzn-select-deselect"
                                                           product="${requisitionItem?.product}" value="${requisitionItem?.productPackage?.id}"/>
                                </td>
                                <td>
                                    <g:link controller="requisitionTemplate" action="removeFromRequisitionItems" id="${requisition?.id}"
                                            params="['requisitionItem.id':requisitionItem?.id]" class="button icon trash">
                                        ${warehouse.message(code:'default.button.delete.label')}
                                    </g:link>
                                </td>
                            </tr>
                        </g:each>
                        <g:unless test="${requisition?.requisitionItems}">
                            <tr>
                                <td colspan="6" class="center">
                                    <span class="fade empty">${warehouse.message(code: "requisition.noRequisitionItems.message")}</span>
                                </td>

                            </tr>
                        </g:unless>
                        </tbody>
                        <tfoot>
                        <tr>
                            <td>

                            </td>
                            <td>

                            </td>
                            <td>
                                <g:autoSuggest id="product" name="product" jsonUrl="${request.contextPath }/json/findProductByName"
                                               width="350" styleClass="text"/>
                            </td>
                            <td>
                                <g:textField name="quantity" value="" class="text" size="6"/>
                            </td>
                            <td class="left">
                                EA/1
                            </td>
                            <td>
                                <button class="button icon add" id="add-requisition-item"><warehouse:message code="default.button.add.label"/></button>

                            </td>

                        </tr>
                        <tr>
                            <td colspan="7">
                                <div class="buttons">
                                    <button class="button" name="save">${warehouse.message(code:'default.button.save.label', default: 'Save') }</button>
                                    &nbsp;
                                    <g:link controller="requisitionTemplate" action="list">
                                        <warehouse:message code="default.button.cancel.label"/>
                                    </g:link>
                                </div>
                            </td>
                        </tr>


                        </tfoot>
                    </table>
                </div>
            </g:form>

        </div>
        --%>
    </div>
</div>
<script>
    $(document).ready(function() {
        $(".tabs").tabs(
            {
                cookie: {
                    // store cookie for a day, without, it would be a session cookie
                    expires: 1
                }
            }
        );

        $(".sortable tbody").sortable({
            handle : '.sorthandle',
            axis : "y",
            helper: "clone",
            forcePlaceholderSize: true,
            placeholder: "ui-state-highlight",
            //connectWith: ".connectable",
            update : function() {
                var updateUrl = "${createLink(controller:'json', action:'sortRequisitionItems') }";
                var sortOrder = $(this).sortable('serialize');
                $.post(updateUrl, sortOrder);
                //$(".sortable tbody tr").removeClass("odd").removeClass("even").filter(":odd").addClass("odd")
                //        .filter(":even").addClass("even");
                //location.reload();
                refreshTable();
            }
        });
        $( ".sortable" ).disableSelection();

    });


</script>
</body>
</html>
