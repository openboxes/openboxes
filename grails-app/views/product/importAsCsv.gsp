
<%@ page import="org.pih.warehouse.product.Product"%>
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
                        <warehouse:message code="product.import.label" default="Upload CSV file"/></a></li>
                    <li><a href="#verify-data-tab">
                        <warehouse:message code="product.import.step2.label" default="Step 2"/>.
                        <warehouse:message code="product.verify.label" default="Verify products"/></a></li>
                    <li><a href="#import-data-tab">
                        <warehouse:message code="product.import.step2.label" default="Step 3"/>.
                        <warehouse:message code="product.import.label" default="Import products"/></a></li>

                    <div class="button-group right">
                        <a class="prev button icon arrowleft" href="#">Previous</a>
                        <a class="next button icon arrowright">Next</a>
                    </div>
                </ul>
                <div id="upload-data-tab" style="padding: 10px;" class="ui-tabs-hide">
                    <div id="upload-form" class="dialog">
                        <g:uploadForm controller="product" action="uploadCsv" fragment="verify-data-tab">
                            <input name="location.id" type="hidden" value="${session.warehouse.id }"/>
                            <input name="type" type="hidden" value="product"/>
                            <table>
                                <tbody>

                                    <tr class="prop">
                                        <td class="name">
                                            <label><warehouse:message code="import.file.label" default="File"/></label>
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

                <div id="verify-data-tab" class="ui-tabs-hide">
                    <g:if test="${command?.products && !productsHaveBeenImported}">
                        <div id="verify" class="dialog">
                            <table>
                                <tbody>
                                    <tr class="">
                                        <td class="value" colspan="2">
                                            <div id="preview">
                                                <table>
                                                    <thead>
                                                        <tr>
                                                            <th>#</th>
                                                            <g:each var="column" in="${columns }">
                                                                <th>${column?.replace("\"", "") }</th>
                                                            </g:each>
                                                        </tr>
                                                    </thead>
                                                    <tbody>
                                                        <g:each var="product" in="${command?.products }" status="status">
                                                            <g:set var="existingProduct" value="${existingProductsMap[product.id] }"/>
                                                            <g:set var="maxLength" value="${product?.description?.length() }"/>
                                                            <tr class="${status%2?'even':'odd' }">
                                                                <td>
                                                                    ${status+1 }
                                                                </td>
                                                                <td>

                                                                    <g:if test="${product?.id }">
                                                                        <g:link controller="inventoryItem" action="showStockCard" id="${product.id }">
                                                                            <g:if test="${product?.id?.length() == 32 }">
                                                                                <span title="${product?.id }">${product?.id?.substring(20, 32) }</span>
                                                                            </g:if>
                                                                            <g:else>
                                                                                ${product?.id }
                                                                            </g:else>
                                                                        </g:link>
                                                                    </g:if>
                                                                    <g:else>
                                                                        <span class="modified">${warehouse.message(code: 'default.new.label') }</span>
                                                                    </g:else>
                                                                </td>
                                                                <td class="${product?.productCode!=existingProduct?.productCode?'modified':'' }">
                                                                    <span title="${existingProduct?.productCode }">${product?.productCode }</span>
                                                                </td>
                                                                <td class="${product?.name!=existingProduct?.name?'modified':'' }">
                                                                    <span title="${existingProduct?.name }">${product?.name }</span>
                                                                </td>
                                                                <td class="${(existingProduct && product?.category!=existingProduct?.category||!product?.category?.id)?'modified':'' }">
                                                                    <span title="${existingProduct?.category }">${product?.category }</span>
                                                                </td>
                                                                <td class="${product?.description!=existingProduct?.description?'modified':'' }">
                                                                    <g:if test="${maxLength > 15 }">
                                                                        <span title="${product?.description }">${product?.description?.substring(0,15)}...</span>
                                                                    </g:if>
                                                                    <g:else>
                                                                        ${product?.description }
                                                                    </g:else>
                                                                </td>
                                                                <td class="${product?.unitOfMeasure!=existingProduct?.unitOfMeasure?'modified':'' }">${product?.unitOfMeasure }</td>
                                                                <td class="${product?.manufacturer!=existingProduct?.manufacturer?'modified':'' }">${product?.manufacturer }</td>
                                                                <td class="${product?.brandName!=existingProduct?.brandName?'modified':'' }">${product?.brandName }</td>
                                                                <td class="${product?.manufacturerCode!=existingProduct?.manufacturerCode?'modified':'' }">${product?.manufacturerCode }</td>
                                                                <td class="${product?.manufacturerName!=existingProduct?.manufacturerName?'modified':'' }">${product?.manufacturerName }</td>
                                                                <td class="${product?.vendor!=existingProduct?.vendor?'modified':'' }">${product?.vendor }</td>
                                                                <td class="${product?.vendorCode!=existingProduct?.vendorCode?'modified':'' }">${product?.vendorCode }</td>
                                                                <td class="${product?.vendorName!=existingProduct?.vendorName?'modified':'' }">${product?.vendorName }</td>
                                                                <td class="${product?.coldChain!=existingProduct?.coldChain?'modified':'' }">${product?.coldChain }</td>
                                                                <td class="${product?.upc!=existingProduct?.upc?'modified':'' }">${product?.upc }</td>
                                                                <td class="${product?.ndc!=existingProduct?.ndc?'modified':'' }">${product?.ndc }</td>
                                                                <td class="fade">${product?.dateCreated }</td>
                                                                <td class="fade">${product?.lastUpdated }</td>
                                                            </tr>
                                                            <g:if test="${product.hasErrors() }">
                                                                <tr>
                                                                    <td colspan="20"><div class="errors"><g:renderErrors bean="${product}" as="list" /></div></td>
                                                                </tr>
                                                            </g:if>
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
                </div>
                <div id="import-data-tab" class="ui-tabs-hide">
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
                                            <g:set var="existingProducts" value="${existingProductsMap?.keySet()?.size()?:0}"/>
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
                                            <label for="tags"><warehouse:message code="product.tags.label" /></label>
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
                                            <%-- <img src="${createLinkTo(dir:'images/skin',file:'database_save.png')}" alt="upload" />--%>
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

                </div>
            </div>

        </div>
			
		<script type="text/javascript">
			$(function() {


                $(".tabs").tabs(
                        {
                            //cookie: {
                                // store cookie for a day, without, it would be a session cookie
                                //expires: 1
                            //}
                        }
                );

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
		</script>

		
			
	</body>
</html>
