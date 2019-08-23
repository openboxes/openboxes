
<%@ page import="org.pih.warehouse.product.Product" defaultCodec="html" %>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="custom" />
		<title>
			<warehouse:message code="default.import.label" args="[warehouse.message(code:'default.data.label')]"/>
		</title>
		<link rel="stylesheet" href="${createLinkTo(dir:'js/jquery.tagsinput/',file:'jquery.tagsinput.css')}" type="text/css" media="screen, projection" />
		<script src="${createLinkTo(dir:'js/jquery.tagsinput/', file:'jquery.tagsinput.js')}" type="text/javascript" ></script>
	</head>
	<body>
		<div class="body">
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<g:hasErrors bean="${command}">
				<div class="errors"><g:renderErrors bean="${command}" as="list" /></div>
			</g:hasErrors>


            <div class="tabs tabs-ui">
                <ul>
                    <li><a href="#upload-data-tab"><warehouse:message code="product.import.step1.label" default="Step 1"/>.
                        <warehouse:message code="product.import.upload.label" default="Upload data file"/></a></li>
                    <li><a href="#verify-data-tab">
                        <warehouse:message code="product.import.step2.label" default="Step 2"/>.
                        <warehouse:message code="product.import.verify.label" default="Verify products"/></a></li>
                    <li><a href="#import-data-tab">
                        <warehouse:message code="product.import.step2.label" default="Step 3"/>.
                        <warehouse:message code="product.import.save.label" default="Import products"/></a></li>
                </ul>
                <div id="upload-data-tab" class="ui-tabs-hide">
                    <div class="box">
                        <div id="upload-form" class="dialog">
                            <h2><g:message code="product.import.upload.label" default="Upload data file"/></h2>
                            <g:uploadForm controller="product" action="uploadCsv" fragment="verify-data-tab">
                                <input name="location.id" type="hidden" value="${session.warehouse.id }"/>
                                <input name="type" type="hidden" value="product"/>
                                <table>
                                    <tbody>
                                        <tr class="prop">
                                            <td class="name">
                                                <label><warehouse:message code="import.file.label" default="Choose a starter data file"/></label>
                                            </td>
                                            <td class="value">
                                                <div class="">
                                                    <g:link controller="batch" action="downloadCsvTemplate" params="[template:'products.csv']" class="button icon arrowdown">
                                                        <warehouse:message code="import.product.template.label" default="Download CSV template"/>
                                                    </g:link>
                                                    <b>
                                                    -- OR --
                                                    </b>
                                                    <g:link controller="product" action="exportAsCsv" class="button icon arrowdown">
                                                        <warehouse:message code="import.product.exportAll.label" default="Download CSV of all products"/>
                                                    </g:link>
                                                </div>
                                            </td>
                                        </tr>
                                        <tr class="prop">
                                            <td class="name">
                                                <label><warehouse:message code="import.file.label" default="Choose the data file you'd like to import"/></label>
                                            </td>
                                            <td class="value">
                                                <input name="importFile" type="file" />
                                            </td>
                                        </tr>
                                        <tr class="prop">
                                            <td class="name">
                                                &nbsp;
                                            </td>
                                            <td>
                                                <button type="submit" class="button">
                                                    ${warehouse.message(code: 'default.button.upload.label', default: 'Upload')}
                                                </button>
                                                &nbsp;
                                                <a href="${createLink(controller: "product", action: "importAsCsv")}" >
                                                    <warehouse:message code="default.button.cancel.label"/>
                                                </a>

                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </g:uploadForm>
                        </div>
                    </div>
                </div>

                <div id="verify-data-tab" class="ui-tabs-hide">
                    <div class="box">
                        <h2><g:message code="product.import.verify.label" default="Verify products"/></h2>
                        <g:if test="${command?.products && !productsHaveBeenImported}">
                            <div id="verify" class="dialog" style="overflow-y: auto;">

                                <table>
                                    <tbody>
                                        <tr class="">
                                            <td class="value" colspan="2">
                                                <div id="preview">

                                                    <table class="importDataTable">
                                                        <thead>
                                                            <tr>
                                                                <th>#</th>
                                                                <g:each var="column" in="${columns }">
                                                                    <th>${column?.replace("\"", "") }</th>
                                                                </g:each>
                                                            </tr>
                                                        </thead>
                                                        <tbody>
                                                            <g:each var="productProperties" in="${command?.products }" status="status">
                                                                <g:set var="existingProduct" value="${productProperties.product }"/>
                                                                <g:set var="maxLength" value="${productProperties?.description?.length() }"/>
                                                                <tr class="${status%2?'even':'odd' }">
                                                                    <td>
                                                                        ${status+1 }
                                                                    </td>
                                                                    <td>

                                                                        <g:if test="${productProperties?.id }">
                                                                            <g:link controller="inventoryItem" action="showStockCard" id="${productProperties.id }">
                                                                                <g:if test="${productProperties?.id?.length() == 32 }">
                                                                                    <span title="${productProperties?.id }">${productProperties?.id?.substring(20, 32) }</span>
                                                                                </g:if>
                                                                                <g:else>
                                                                                    ${productProperties?.id }
                                                                                </g:else>
                                                                            </g:link>
                                                                        </g:if>
                                                                        <g:else>
                                                                            <span class="modified">${warehouse.message(code: 'default.new.label') }</span>
                                                                        </g:else>
                                                                    </td>
                                                                    <td class="${productProperties?.productCode!=existingProduct?.productCode?'modified':'' }">
                                                                        <span title="${existingProduct?.productCode }">${productProperties?.productCode }</span>
                                                                    </td>
                                                                    <td class="${productProperties?.name!=existingProduct?.name?'modified':'' }">
                                                                        <span title="${existingProduct?.name }">${productProperties?.name }</span>
                                                                    </td>
                                                                    <td class="${productProperties?.category!=existingProduct?.category?'modified':'' }">
                                                                        <span title="${existingProduct?.category }">${productProperties?.category }</span>
                                                                    </td>
                                                                    <td class="${productProperties?.description!=existingProduct?.description?'modified':'' }">
                                                                        <g:if test="${maxLength > 15 }">
                                                                            <span title="${productProperties?.description }">${productProperties?.description?.substring(0,15)}...</span>
                                                                        </g:if>
                                                                        <g:else>
                                                                            ${productProperties?.description }
                                                                        </g:else>
                                                                    </td>
                                                                    <td class="${productProperties?.unitOfMeasure!=existingProduct?.unitOfMeasure?'modified':'' }">${productProperties?.unitOfMeasure }</td>
                                                                    <td>
                                                                        <ul>
                                                                            <g:each var="tag" in="${productProperties?.tags }">
                                                                                <li class="${!existingProduct?.hasTag(tag)?'modified':'' }">${tag}</li>
                                                                            </g:each>
                                                                        </ul>
                                                                    </td>
                                                                    <td class="${productProperties?.pricePerUnit!=existingProduct?.pricePerUnit?'modified':'' }">${productProperties?.pricePerUnit }</td>
                                                                    <td class="${productProperties?.manufacturer!=existingProduct?.manufacturer?'modified':'' }">${productProperties?.manufacturer }</td>
                                                                    <td class="${productProperties?.brandName!=existingProduct?.brandName?'modified':'' }">${productProperties?.brandName }</td>
                                                                    <td class="${productProperties?.manufacturerCode!=existingProduct?.manufacturerCode?'modified':'' }">${productProperties?.manufacturerCode }</td>
                                                                    <td class="${productProperties?.manufacturerName!=existingProduct?.manufacturerName?'modified':'' }">${productProperties?.manufacturerName }</td>
                                                                    <td class="${productProperties?.vendor!=existingProduct?.vendor?'modified':'' }">${productProperties?.vendor }</td>
                                                                    <td class="${productProperties?.vendorCode!=existingProduct?.vendorCode?'modified':'' }">${productProperties?.vendorCode }</td>
                                                                    <td class="${productProperties?.vendorName!=existingProduct?.vendorName?'modified':'' }">${productProperties?.vendorName }</td>
                                                                    <td class="${productProperties?.coldChain!=existingProduct?.coldChain?'modified':'' }">${productProperties?.coldChain }</td>
                                                                    <td class="${productProperties?.upc!=existingProduct?.upc?'modified':'' }">${productProperties?.upc }</td>
                                                                    <td class="${productProperties?.ndc!=existingProduct?.ndc?'modified':'' }">${productProperties?.ndc }</td>
                                                                    <td class="fade">${productProperties?.product?.dateCreated }</td>
                                                                    <td class="fade">${productProperties?.product?.lastUpdated }</td>
                                                                </tr>
                                                            </g:each>
                                                        </tbody>
                                                    </table>
                                                </div>
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        </g:if>
                        <g:else>
                            <div class="empty center">
                                <warehouse:message code="import.product.uploadDataFile.message" default="You must upload a data file before proceeding to this step."/>
                            </div>
                        </g:else>
                    </div>
                </div>

                <div id="import-data-tab" class="ui-tabs-hide">
                    <div class="box">
                        <h2><warehouse:message code="product.import.save.label" default="Import products"/></h2>


                        <g:if test="${command?.products && !productsHaveBeenImported}">
                            <g:form controller="product" action="importCsv" method="POST">

                                <input name="location.id" type="hidden" value="${session.warehouse.id }"/>
                                <input name="type" type="hidden" value="product"/>


                                <div id="import" class="dialog">
                                    <table>
                                        <tbody>
                                        <tr class="prop">
                                            <td class="name">
                                                <label><warehouse:message code="import.filename.label" default="Filename"/></label>
                                            </td>
                                            <td class="value">
                                                ${command?.importFile?.originalFilename }
                                            </td>
                                        </tr>
                                        <tr class="prop">
                                            <td class="name">
                                                <label><warehouse:message code="import.contentType.label" default="Content Type"/></label>
                                            </td>
                                            <td class="value">
                                                ${command?.importFile?.contentType }
                                            </td>
                                        </tr>
                                        <tr class="prop">
                                            <td class="name">
                                                <label><warehouse:message code="import.size.label" default="Size"/></label>
                                            </td>
                                            <td class="value">
                                                <g:formatNumber number="${(command?.importFile?.size / 1000) }" format="#,###.#"/> kB
                                            </td>
                                        </tr>
                                        <tr class="prop">
                                            <td class="name">
                                                <label><warehouse:message code="import.numOfRecords.label" default="# of Records"/></label>
                                            </td>
                                            <td class="value">
                                                <g:set var="totalProducts" value="${command?.products?.size()?:0 }"/>
                                                <g:set var="existingProducts" value="${command?.products?.findAll { it.product }?.size()?:0}"/>
                                                <g:set var="newProducts" value="${totalProducts - existingProducts }"/>

                                                <ul>
                                                    <li>${existingProducts } ${warehouse.message(code:'import.existingProducts.label', default: 'updates to existing products') }</li>
                                                    <li>${newProducts } ${warehouse.message(code:'import.newProducts.label', default: 'new products to be created') }</li>
                                                    <li>${totalProducts } ${warehouse.message(code:'import.importedProducts.label', default: 'imported products') }</li>
                                                </ul>


                                            </td>
                                        </tr>

                                        <tr class="prop">
                                            <td valign="top" class="name">
                                                <label for="tags1"><warehouse:message code="product.tags.label" /></label>
                                            </td>
                                            <td valign="top" class="value">
                                                <g:textField id="tags1" class="tags" name="tagsToBeAdded" value="${tag }"/>
                                                <script>
                                                    $(function() {
                                                        $('#tags1').tagsInput({
                                                            'autocomplete_url':'${createLink(controller: 'json', action: 'findTags')}',
                                                            'width': 'auto',
                                                            'removeWithBackspace' : true
                                                        });
                                                    });
                                                </script>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td class="name">
                                            </td>
                                            <td class="value">
                                                <g:hiddenField name="importNow" value="${true }"/>
                                                <button type="submit" class="button">
                                                    ${warehouse.message(code: 'default.button.import.label', default: 'Import')}</button>
                                                &nbsp;
                                                <a href="${createLink(controller: "product", action: "importAsCsv")}" >
                                                    <warehouse:message code="default.button.cancel.label"/>
                                                </a>
                                            </td>
                                        </tr>
                                        </tbody>
                                    </table>
                                </div>
                            </g:form>
                        </g:if>
                        <g:else>
                            <div class="empty center">
                                <warehouse:message code="import.product.uploadDataFile.message" default="You must upload a data file before proceeding to this step."/>
                            </div>
                        </g:else>
                    </div>
                </div>
            </div>

            <div class="buttons">
                <div class="left">
                    <a class="prev button icon arrowleft" href="#"><g:message code="default.button.previous.label"/></a>
                </div>
                <div class="right">
                    <a class="next button icon arrowright"><g:message code="default.button.next.label"/></a>
                </div>
            </div>

        </div>

		<script type="text/javascript">
			$(function() {


                $(".tabs").tabs({});

                $(".next").click(function() {
                    var selected = $(".tabs").tabs("option", "selected");
                    console.log(selected);
                    $(".tabs").tabs("option", "selected", selected + 1);
                });
                $(".prev").click(function() {
                    var selected = $(".tabs").tabs("option", "selected");
                    console.log(selected);
                    $(".tabs").tabs("option", "selected", selected - 1);
                });

                $(".tabs ul li a").attr("disabled", "disabled");

			});

            $(window).load(function(){

                var dataTable = $('.importDataTable').dataTable( {
                    "bProcessing": true,
                    "sServerMethod": "GET",
                    "iDisplayLength": 25,
                    "bSearch": false,
                    "bScrollCollapse": true,
                    "bJQueryUI": true,
                    "bAutoWidth": true,
                    "sPaginationType": "full_numbers",
                    "aLengthMenu": [
                        [5, 10, 25, 100, 1000, -1],
                        [5, 10, 25, 100, 1000, "All"]
                    ]
                });
            });
		</script>
	</body>
</html>
