<%@ page import="org.pih.warehouse.inventory.InventoryLevel; org.pih.warehouse.product.Product" %>
<html>
	<head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="custom" />
        <g:set var="entityName" value="${warehouse.message(code: 'product.label', default: 'Product')}" />

        <g:if test="${productInstance?.id}">
	        <title>${productInstance?.productCode } ${productInstance?.name }</title>
		</g:if>
		<g:else>
	        <title><warehouse:message code="product.add.label" /></title>	
			<content tag="label1"><warehouse:message code="inventory.label"/></content>
		</g:else>
		<link rel="stylesheet" href="${resource(dir:'js/jquery.tagsinput/',file:'jquery.tagsinput.css')}" type="text/css" media="screen, projection" />
		<script src="${resource(dir:'js/jquery.tagsinput/', file:'jquery.tagsinput.js')}" type="text/javascript" ></script>
        <style>
        #category_id_chosen {
            width: 100% !important;
        }
        </style>

    </head>
    <body>
        <div class="body">
            <g:if test="${flash.message}">
            	<div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${productInstance}">
	            <div class="errors">
	                <g:renderErrors bean="${productInstance}" as="list" />
	            </div>
            </g:hasErrors>

   			<g:if test="${productInstance?.id }">
				<g:render template="summary" model="[productInstance:productInstance]"/>
			</g:if>

			<div style="padding: 10px">

                <div class="tabs tabs-ui">
					<ul>
						<li><a href="#tabs-details"><warehouse:message code="product.details.label"/></a></li>
						<%-- Only show these tabs if the product has been created --%>
						<g:if test="${productInstance?.id }">
                            <li><a href="#tabs-manufacturer"><warehouse:message code="product.manufacturer.label"/></a></li>
                            <li><a href="#tabs-status"><warehouse:message code="product.stockLevel.label" default="Stock levels"/></a></li>
                            <%--<li><a href="#tabs-tags"><warehouse:message code="product.tags.label"/></a></li>--%>
                            <li><a href="#tabs-synonyms"><warehouse:message code="product.synonyms.label"/></a></li>
                            <li><a href="#tabs-productGroups"><warehouse:message code="product.substitutions.label" default="Substitutes"/></a></li>
							<li><a href="#tabs-packages"><warehouse:message code="packages.label" default="Packages"/></a></li>
							<li><a href="#tabs-documents"><warehouse:message code="product.documents.label" default="Documents"/></a></li>
                            <li><a href="#tabs-attributes"><warehouse:message code="product.attributes.label" default="Attributes"/></a></li>
                            <li><a href="#tabs-components"><warehouse:message code="product.components.label" default="Bill of Materials"/></a></li>
                        </g:if>
					</ul>	
					<div id="tabs-details" style="padding: 10px;" class="ui-tabs-hide">
                        <g:set var="formAction"><g:if test="${productInstance?.id}">update</g:if><g:else>save</g:else></g:set>
                        <g:form action="${formAction}" method="post">
                            <g:hiddenField name="id" value="${productInstance?.id}" />
                            <g:hiddenField name="version" value="${productInstance?.version}" />
                            <g:hiddenField name="categoryId" value="${params?.category?.id }"/>
                            <!--  So we know which category to show on browse page after submit -->

                            <div class="box" >
                                <h2>
                                    <warehouse:message code="product.productDetails.label" default="Product details"/>
                                </h2>
                                <table>
                                    <tbody>

                                        <%--
                                        <tr class="prop">
                                            <td class="name"><label for="name"><warehouse:message
                                                code="product.genericName.label" /></label></td>
                                            <td
                                                class="value ${hasErrors(bean: productInstance, field: 'genericProducts', 'errors')}">
                                                <ul>
                                                    <g:each var="productGroup" in="${productInstance?.productGroups }">
                                                        <li>
                                                            ${productGroup.description }
                                                            <g:link controller="productGroup" action="edit" id="${productGroup.id }">
                                                                <warehouse:message code="default.button.edit.label"/>
                                                            </g:link>
                                                        </li>
                                                    </g:each>
                                                </ul>
                                            </td>
                                        </tr>
                                        --%>
                                        <tr class="prop first">
                                            <td class="name middle"><label for="productCode"><warehouse:message
                                                    code="product.productCode.label"/></label>

                                            </td>
                                            <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'productCode', 'errors')}">
                                                <g:textField name="productCode" value="${productInstance?.productCode}" size="50" class="medium text"
                                                             placeholder="${warehouse.message(code:'product.productCode.placeholder') }"/>
                                            </td>
                                        </tr>

                                        <tr class="prop first">
                                            <td class="name middle"><label for="name"><warehouse:message
                                                code="product.name.label" /></label></td>
                                            <td valign="top"
                                                class="value ${hasErrors(bean: productInstance, field: 'name', 'errors')}">
                                                <%--
                                                <g:textField name="name" value="${productInstance?.name}" size="80" class="medium text" />
                                                --%>
                                                <g:autoSuggestString id="name" name="name" size="80" class="text"
                                                    jsonUrl="${request.contextPath}/json/autoSuggest" value="${productInstance?.name?.encodeAsHTML()}"
                                                    placeholder="Product name (e.g. Ibuprofen, 200 mg, tablet)"/>
                                            </td>
                                        </tr>

                                        <tr class="prop">
                                            <td class="name middle">
                                              <label for="category.id"><warehouse:message code="product.primaryCategory.label" /></label>
                                            </td>
                                            <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'category', 'errors')}">
                                                <g:selectCategory name="category.id" class="chzn-select" noSelection="['null':'']"
                                                                     value="${productInstance?.category?.id}" />


                                           </td>
                                        </tr>

                                        <tr class="prop">
                                            <td class="name middle"><label for="unitOfMeasure"><warehouse:message
                                                code="product.unitOfMeasure.label" /></label></td>
                                            <td
                                                class="value ${hasErrors(bean: productInstance, field: 'unitOfMeasure', 'errors')}">
                                                <%--
                                                <g:textField name="unitOfMeasure" value="${productInstance?.unitOfMeasure}" size="15" class="medium text"/>
                                                --%>
                                                <g:autoSuggestString id="unitOfMeasure" name="unitOfMeasure" size="80" class="text"
                                                    jsonUrl="${request.contextPath}/json/autoSuggest"
                                                    value="${productInstance?.unitOfMeasure}" placeholder="e.g. each, tablet, tube, vial"/>
                                            </td>
                                        </tr>

                                        <tr class="prop">
                                            <td class="name top"><label for="description"><warehouse:message
                                                code="product.description.label" /></label></td>
                                            <td
                                                class="value ${hasErrors(bean: productInstance, field: 'description', 'errors')}">
                                                <g:textArea name="description" value="${productInstance?.description}" class="medium text"
                                                    cols="80" rows="8"
                                                    placeholder="Detailed text description (optional)" />
                                            </td>
                                        </tr>
                                        <tr class="prop">
                                            <td class="name middle"><label for="upc"><warehouse:message
                                                    code="product.upc.label" /></label></td>
                                            <td class="value ${hasErrors(bean: productInstance, field: 'upc', 'errors')}">
                                                <g:textField name="upc" value="${productInstance?.upc}" size="50" class="medium text"/>
                                            </td>
                                        </tr>
                                        <tr class="prop">
                                            <td class="name middle"><label for="ndc"><warehouse:message
                                                    code="product.ndc.label" /></label></td>
                                            <td class="value ${hasErrors(bean: productInstance, field: 'ndc', 'errors')}">
                                                <g:textField name="ndc" value="${productInstance?.ndc}" size="50" class="medium text"
                                                             placeholder="e.g. 0573-0165"/>
                                            </td>
                                        </tr>
                                    <tr class="prop">
                                        <td class="name">
                                            <label><warehouse:message code="product.tags.label" default="Tags"></warehouse:message></label>
                                        </td>
                                        <td class="value">
                                            <g:textField id="tags1" class="tags" name="tagsToBeAdded" value="${productInstance?.tagsToString() }"/>
                                            <script>
                                                $(function() {
                                                    $('#tags1').tagsInput({
                                                        'autocomplete_url':'${createLink(controller: 'json', action: 'findTags')}',
                                                        'width': 'auto',
                                                        'height': '20px',
                                                        'removeWithBackspace' : true
                                                    });
                                                });
                                            </script>
                                        </td>
                                    </tr>

                                    <tr class="prop">
                                        <td class="name">
                                            <label><warehouse:message code="product.status.label" default="Status"/></label>
                                        </td>
                                        <td class="value ${hasErrors(bean: productInstance, field: 'active', 'errors')} ${hasErrors(bean: productInstance, field: 'essential', 'errors')}">
                                            <table>
                                                <tr>
                                                    <td>
                                                        <g:checkBox name="active" value="${productInstance?.active}" />
                                                        <label for="active"><warehouse:message
                                                        code="product.active.label" /></label>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td>
                                                        <g:checkBox name="essential" value="${productInstance?.essential}" />
                                                        <label for="essential"><warehouse:message
                                                                code="product.essential.label" /></label>

                                                    </td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>

                                    <tr class="prop">
                                        <td class="name">
                                            <label><warehouse:message code="product.handlingRequirements.label" default="Handling requirements"></warehouse:message></label>
                                        </td>
                                        <td class="value ${hasErrors(bean: productInstance, field: 'coldChain', 'errors')} ${hasErrors(bean: productInstance, field: 'controlledSubstance', 'errors')} ${hasErrors(bean: productInstance, field: 'hazardousMaterial', 'errors')}">
                                            <table style="width: auto;">
                                                <tr>
                                                    <td>
                                                        <g:checkBox name="coldChain" value="${productInstance?.coldChain}" />
                                                        <label for="coldChain"><warehouse:message
                                                            code="product.coldChain.label" /></label>
                                                    </td>

                                                    <td>
                                                        <g:checkBox name="controlledSubstance" value="${productInstance?.controlledSubstance}" />
                                                        <label for="controlledSubstance"><warehouse:message
                                                            code="product.controlledSubstance.label" /></label>
                                                    </td>
                                                </tr>
                                                <tr>

                                                    <td>
                                                        <g:checkBox name="hazardousMaterial" value="${productInstance?.hazardousMaterial}" />
                                                        <label for="hazardousMaterial"><warehouse:message
                                                            code="product.hazardousMaterial.label" /></label>
                                                    </td>

                                                    <td>
                                                        <g:checkBox name="reconditioned" value="${productInstance?.reconditioned}" />
                                                        <label for="reconditioned"><warehouse:message
                                                                code="product.reconditioned.label" default="Reconditioned"/></label>
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <td class="name">
                                            <label><warehouse:message code="product.inventoryControl.label" default="Inventory control"></warehouse:message></label>
                                        </td>
                                        <td class="value ${hasErrors(bean: productInstance, field: 'lotControl', 'errors')}">
                                            <table>
                                                <tr>
                                                    <td>
                                                        <g:checkBox name="serialized" value="${productInstance?.serialized}" />
                                                        <label for="serialized"><warehouse:message
                                                            code="product.serialized.label" /></label>
                                                    </td>
                                                </tr>
                                                <tr>

                                                    <td>
                                                        <g:checkBox name="lotControl" value="${productInstance?.lotControl}" />
                                                        <label for="lotControl"><warehouse:message
                                                            code="product.lotControl.label" /></label>
                                                    </td>
                                                </tr>
                                            </table>

                                        </td>
                                    </tr>
                                    </tbody>


                                </table>
                            </div>

                            <div class="box">

                                <h2>
                                    <warehouse:message code="product.manufacturerDetails.label" default="Manufacturer details"/>
                                </h2>

                                <table>
                                    <tbody>
                                    <tr class="prop">
                                        <td class="name middle"><label for="brandName"><warehouse:message
                                                code="product.brandName.label" /></label></td>
                                        <td
                                                class="value ${hasErrors(bean: productInstance, field: 'brandName', 'errors')}">
                                            <g:autoSuggestString id="brandName" name="brandName" size="50" class="text"
                                                                 jsonUrl="${request.contextPath}/json/autoSuggest"
                                                                 value="${productInstance?.brandName}"
                                                                 placeholder="e.g. Advil, Tylenol"/>
                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <td class="name middle"><label for="manufacturer"><warehouse:message
                                                code="product.manufacturer.label" /></label></td>
                                        <td
                                            class="value ${hasErrors(bean: productInstance, field: 'manufacturer', 'errors')}">
                                            <%--
                                            <g:textField name="unitOfMeasure" value="${productInstance?.manufacturer}" size="60" class="medium text"/>
                                            --%>
                                            <g:autoSuggestString id="manufacturer" name="manufacturer" size="50" class="text"
                                                                 jsonUrl="${request.contextPath}/json/autoSuggest"
                                                                 value="${productInstance?.manufacturer}"
                                                                 placeholder="e.g. Pfizer, Beckton Dickson"/>

                                        </td>
                                    </tr>

                                    <tr class="prop">
                                        <td class="name middle"><label for="manufacturerCode"><warehouse:message
                                                code="product.manufacturerCode.label"/></label></td>
                                        <td class="value ${hasErrors(bean: productInstance, field: 'manufacturerCode', 'errors')}">
                                            <g:textField name="manufacturerCode" value="${productInstance?.manufacturerCode}" size="50" class="text"/>
                                            <%--
                                            <g:autoSuggestString id="manufacturerCode" name="manufacturerCode" size="50" class="text"
                                                jsonUrl="${request.contextPath}/json/autoSuggest"
                                                value="${productInstance?.manufacturerCode}"
                                                placeholder=""/>
                                            --%>
                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <td class="name middle"><label for="manufacturerName"><warehouse:message
                                                code="product.manufacturerName.label"/></label></td>
                                        <td class="value ${hasErrors(bean: productInstance, field: 'manufacturerName', 'errors')}">
                                            <g:textField name="manufacturerName" value="${productInstance?.manufacturerName}" size="50" class="text"/>
                                            <%--
                                            <g:autoSuggestString id="manufacturerName" name="manufacturerName" size="50" class="text"
                                                jsonUrl="${request.contextPath}/json/autoSuggest"
                                                value="${productInstance?.manufacturerName}"
                                                placeholder=""/>
                                            --%>
                                        </td>
                                    </tr>


                                    <tr class="prop">
                                        <td class="name middle"><label for="modelNumber"><warehouse:message
                                                code="product.modelNumber.label" /></label></td>
                                        <td
                                            class="value ${hasErrors(bean: productInstance, field: 'modelNumber', 'errors')}">
                                            <g:textField name="modelNumber" value="${productInstance?.modelNumber}" size="50" class="text"/>
                                            <%--
                                            <g:autoSuggestString id="modelNumber" name="modelNumber" size="50" class="text"
                                                jsonUrl="${request.contextPath}/json/autoSuggest"
                                                value="${productInstance?.modelNumber}" promptOnMatch="true"
                                                placeholder="e.g. Usually only pertains to equipment "/>
                                            --%>
                                        </td>
                                    </tr>

                                    <tr class="prop">
                                        <td class="name middle"><label for="vendor"><warehouse:message
                                                code="product.vendor.label" /></label></td>
                                        <td
                                            class="value ${hasErrors(bean: productInstance, field: 'vendor', 'errors')}">
                                            <g:autoSuggestString id="vendor" name="vendor" size="50" class="text"
                                                                 jsonUrl="${request.contextPath}/json/autoSuggest"
                                                                 value="${productInstance?.vendor}"
                                                                 placeholder="e.g. IDA, IMRES, McKesson"/>

                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <td class="name middle"><label for="vendorCode"><warehouse:message
                                                code="product.vendorCode.label"/></label></td>
                                        <td class="value ${hasErrors(bean: productInstance, field: 'vendorCode', 'errors')}">
                                            <g:textField name="vendorCode" value="${productInstance?.vendorCode}" size="50" class="text"/>
                                            <%--
                                            <g:autoSuggestString id="vendorCode" name="vendorCode" size="50" class="text"
                                                jsonUrl="${request.contextPath}/json/autoSuggest"
                                                value="${productInstance?.vendorCode}"
                                                placeholder=""/>
                                            --%>
                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <td class="name middle"><label for="vendorName"><warehouse:message
                                                code="product.vendorName.label"/></label></td>
                                        <td class="value ${hasErrors(bean: productInstance, field: 'vendorName', 'errors')}">
                                            <g:textField name="vendorName" value="${productInstance?.vendorName}" size="50" class="text"/>
                                            <%--
                                            <g:autoSuggestString id="vendorName" name="vendorName" size="50" class="text"
                                                jsonUrl="${request.contextPath}/json/autoSuggest"
                                                value="${productInstance?.vendorName}"
                                                placeholder=""/>
                                            --%>

                                        </td>
                                    </tr>
                                    <tr class="prop">
                                        <td class="name middle"><label for="pricePerUnit"><warehouse:message
                                                code="product.pricePerUnit.label"/></label></td>
                                        <td class="value middle ${hasErrors(bean: productInstance, field: 'pricePerUnit', 'errors')}">
                                            <g:textField name="pricePerUnit" placeholder="Price per unit (${grailsApplication.config.openboxes.locale.defaultCurrencyCode})"
                                                         value="${g.formatNumber(number:productInstance?.pricePerUnit, format:'###,###,##0.####') }"
                                                         class="text" size="50" />

                                            <span class="fade">${grailsApplication.config.openboxes.locale.defaultCurrencyCode}</span>

                                        </td>
                                    </tr>



                                        <%--
                                        <tr class="prop">
                                            <td class="name"><label for="name"><warehouse:message
                                                code="product.genericName.label" /></label></td>
                                            <td
                                                class="value ${hasErrors(bean: productInstance, field: 'genericProducts', 'errors')}">
                                                <ul>
                                                    <g:each var="productGroup" in="${productInstance?.productGroups }">
                                                        <li>
                                                            ${productGroup.description }
                                                            <g:link controller="productGroup" action="edit" id="${productGroup.id }">
                                                                <warehouse:message code="default.button.edit.label"/>
                                                            </g:link>
                                                        </li>
                                                    </g:each>
                                                </ul>
                                            </td>
                                        </tr>
                                        --%>
                                    </tbody>
                                </table>
                            </div>
                            <div class="buttons">
                                <button type="submit" class="button">
                                    ${warehouse.message(code: 'default.button.save.label', default: 'Save')}
                                </button>
                                &nbsp;
                                <g:if test="${productInstance?.id }">
                                    <g:link controller='inventoryItem' action='showStockCard' id='${productInstance?.id }'>
                                        ${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}
                                    </g:link>
                                </g:if>
                                <g:else>
                                    <g:link controller="inventory" action="browse">
                                        ${warehouse.message(code: 'default.button.cancel.label', default: 'Cancel')}
                                    </g:link>
                                </g:else>
                            </div>

                        </g:form>
                    </div>


                    <%-- Only show these tabs if the product has been created --%>
                    <g:if test="${productInstance?.id }">

                        <div id="tabs-manufacturer" style="padding: 10px;" class="ui-tabs-hide">
                            <g:render template="manufacturers" model="[productInstance:productInstance]"/>

                        </div>

                        <div id="tabs-synonyms" style="padding: 10px;" class="ui-tabs-hide">
                            <div class="box">
                                <h2><warehouse:message code="product.synonyms.label" default="Synonyms"/></h2>
                                <g:render template="synonyms" model="[product: productInstance, synonyms:productInstance?.synonyms]"/>
                            </div>
                        </div>

                        <div id="tabs-productGroups" style="padding: 10px;" class="ui-tabs-hide">
                            <div class="box">
                                <h2><warehouse:message code="product.substitutions.label" default="Substitutions"/></h2>
                                <g:render template="productGroups" model="[product: productInstance, productGroups:productInstance?.productGroups]"/>
                            </div>
                        </div>
                        <div id="tabs-attributes" style="padding: 10px;" class="ui-tabs-hide">
                            <g:render template="attributes" model="[productInstance:productInstance]"/>
                        </div>
                        <div id="tabs-status" style="padding: 10px;" class="ui-tabs-hide">
                            <g:render template="inventoryLevels" model="[productInstance:productInstance]"/>
						</div>
                        <div id="tabs-components" style="padding: 10px;" class="ui-tabs-hide">
                            <g:render template="productComponents" model="[productInstance:productInstance]"/>
                        </div>
						<div id="tabs-documents" style="padding: 10px;" class="ui-tabs-hide">
                            <g:render template="documents" model="[productInstance:productInstance]"/>
						</div>
						<div id="tabs-packages" style="padding: 10px;" class="ui-tabs-hide">
                            <g:render template="productPackages" model="[productInstance:productInstance]"/>

						</div>
                        <div id="inventory-level-dialog" class="dialog hidden" title="Add a new stock level">
                            <g:render template="../inventoryLevel/form" model="[productInstance:productInstance,inventoryLevelInstance:new InventoryLevel()]"/>
                        </div>
                        <div id="uom-class-dialog" class="dialog hidden" title="Add a unit of measure class">
                            <g:render template="uomClassDialog" model="[productInstance:productInstance]"/>
						</div>
						<div id="uom-dialog" class="dialog hidden" title="Add a unit of measure">
                            <g:render template="uomDialog" model="[productInstance:productInstance]"/>
						</div>
                        <div id="product-package-dialog" class="dialog hidden" title="${packageInstance?.id?warehouse.message(code:'package.edit.label'):warehouse.message(code:'package.add.label') }">
                            <g:render template="productPackageDialog" model="[productInstance:productInstance,packageInstance:packageInstance]"/>
                        </div>
					</g:if>
				</div>	
			</div>
		</div>

        <g:each var="inventoryLevelInstance" in="${productInstance?.inventoryLevels}" status="i">
            <div id="inventory-level-${inventoryLevelInstance?.id}-dialog" class="dialog hidden" title="Edit inventory level">
                <g:render template="../inventoryLevel/form" model="[inventoryLevelInstance:inventoryLevelInstance]"/>
            </div>
        </g:each>
        <g:each var="packageInstance" in="${productInstance.packages }">
            <g:set var="dialogId" value="${'editProductPackage-' + packageInstance.id}"/>
            <div id="${dialogId}" class="dialog hidden" title="${packageInstance?.id?warehouse.message(code:'package.edit.label'):warehouse.message(code:'package.add.label') }">
                <g:render template="productPackageDialog" model="[dialogId:dialogId,productInstance:productInstance,packageInstance:packageInstance]"/>
            </div>
        </g:each>

		<script type="text/javascript">
            function updateSynonymTable(data) {
                console.log("updateSynonymTable");
                console.log(data);
            }

	    	$(document).ready(function() {
		    	$(".tabs").tabs(
	    			{
	    				cookie: {
	    					// store cookie for a day, without, it would be a session cookie
	    					expires: 1
	    				}
	    			}
				); 

				$(".dialog").dialog({ autoOpen: false, modal: true, width: '800px'});

				$(".open-dialog").click(function() { 
					var id = $(this).attr("dialog-id");
					$("#"+id).dialog('open');
				});
				$(".close-dialog").click(function() { 
					var id = $(this).attr("dialog-id");
					$("#"+id).dialog('close');
				});

				$(".attributeValueSelector").change(function(event) {
					if ($(this).val() == '_other') {
						$(this).parent().find(".otherAttributeValue").show();
					}
					else {
						$(this).parent().find(".otherAttributeValue").val('').hide();
					}
				});

				function updateBinLocation() { 
					$("#binLocation").val('updated')
				}

				$(".binLocation").change(function(){ updateBinLocation() });
				
			});
		</script>
    </body>
</html>
