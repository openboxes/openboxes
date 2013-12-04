<%@ page import="org.pih.warehouse.inventory.InventoryStatus" %>
<style>
.nailthumb-container {
    width: 100%;
    overflow: hidden;
}
.galleryItem {
    color: #797478;
    font: 10px/1.5 Verdana, Helvetica, sans-serif;
    float: left;
    margin: 2px;
} 
 
.galleryItem img {
    max-width: 100%;
    -webkit-border-radius: 5px;
    -moz-border-radius: 5px;
    border-radius: 5px;
}
</style>

<div id="product-details">

	<%-- 
	<g:if test="${productInstance?.images}">	
		<table class="box">
			<tbody>			
			
				<tr class="odd">
					<td class="odd" colspan="2">
						<label><warehouse:message code="product.images.label"/></label>
						<g:link controller="product" action="edit" id="${productInstance?.id }" fragment="tabs-documents"> 
							<img src="${resource(dir: 'images/icons/silk', file: 'pencil.png')}"/>
						</g:link>						
									
					</td>
				</tr>				
				<tr class="prop">
					<td colspan="2" class="center middle">
						<div class="nailthumb-container">
							<g:each var="document" in="${productInstance?.images}" status="i">
								<div class="galleryItem">
									<a class="open-dialog" href="javascript:openDialog('#dialog-${document.id }', '#img-${document.id }');">
										<img src="${createLink(controller:'product', action:'renderImage', id:document.id)}" title="${warehouse.message(code:'default.clickToZoom.label', default:'Click to zoom')}" style="display:none;" />		
									</a>
							    </div>
							</g:each>
						</div>
						<g:each var="document" in="${productInstance?.images}" status="i">
							<div id="dialog-${document.id }" title="${document.filename }" style="display:none;" class="dialog center">
								<div>
									<img id="img-${document.id }" src="${createLink(controller:'product', action:'viewImage', id:document.id, params:['width':'300','height':'300'])}" 
	           							class="middle image" style="border: 1px solid lightgrey" />
								</div>
								<br/>
								<g:link controller="document" action="download" class="button icon arrowdown" id="${document.id}">Download</g:link>
								&nbsp;
								<a class="close-dialog" href="javascript:closeDialog('#dialog-${document.id }','#img-${document.id }');">
									${warehouse.message(code:'default.button.cancel.label') }
								</a>
							</div>
						</g:each>						
					</td>			
				</tr>													
			</tbody>		
		</table>
	</g:if>
	<g:if test="${productInstance?.productCode }">
		<table class="box">	
			<tr class="odd">
				<td colspan="2">
					<label>${warehouse.message(code: 'product.productCode.label') }</label>
				</td>
			</tr>
			<tr>
				<td class="value" colspan="2">
					<div class="center">					
						<img src="${createLink(controller:'product',action:'barcode',params:[data:productInstance?.productCode,format:'CODE_39',width:200,height:20]) }" class="top"/>
						<div class="productCode">${productInstance?.productCode }</div>
					</div>		
				</td>
			</tr>
		</table>
	</g:if>
	
	<g:if test="${productInstance?.description }">
		<table class="box">
			<tbody>
				<tr class="odd">
					<td colspan="2">
						<label>${warehouse.message(code: 'product.description.label') }</label>
					</td>
				</tr>
				<tr class="prop">	
					<td class="value" colspan="2" style="text-align: justify">
						${productInstance?.description }
					</td>
				</tr>
			</tbody>		
		</table>
	</g:if>
	
	<g:if test="${productInstance?.tags }">
		<table class="box">
			<tbody>
				<tr class="odd">
					<td colspan="2">
						<label>${warehouse.message(code: 'product.tags.label') }</label>
					</td>
				</tr>
				<tr class="prop">	
					<td class="value" colspan="2" style="text-align: justify">
						<div class="tags">
						<g:each var="tag" in="${productInstance?.tags}">
							<span class="tag">
								<g:link controller="inventory" action="browse" params="['tag':tag.tag]">
									${tag.tag }
								</g:link>
							</span>
						</g:each>
					</div>	
					</td>
				</tr>
			</tbody>		
		</table>
	</g:if>
	--%>
<g:set var="latestInventoryDate"
       value="${productInstance?.latestInventoryDate(session.warehouse.id)}" />
<div class="box">
    <h2>
        ${warehouse.message(code: 'product.status.label') }
    </h2>
    <table>
        <tbody>
            <tr class="prop">
                <td class="label">
                    <label><warehouse:message code="product.onHandQuantity.label"/></label>
                </td>
                <td class="value">
                    <div>
                        ${g.formatNumber(number: totalQuantity, format: '###,###,###') }
                        <g:if test="${productInstance?.unitOfMeasure }">
                            <format:metadata obj="${productInstance?.unitOfMeasure}"/>
                        </g:if>
                        <g:else>
                            ${warehouse.message(code:'default.each.label') }
                        </g:else>
                    </div>
                    <g:if test="${productInstance?.packages }">
                        <g:each var="productPackage" in="${productInstance?.packages }">
                            <g:if test="${productPackage?.uom?.code != 'EA' }">
                                <div>
                                    <span class="fade">
                                        <g:set var="quantityPerPackage" value="${totalQuantity / productPackage?.quantity }"/>
                                        ${g.formatNumber(number: quantityPerPackage, format: '###,###,###.#') }
                                        ${productPackage?.uom?.code }/${productPackage.quantity }
                                    </span>
                                </div>
                            </g:if>
                        </g:each>
                    </g:if>
                </td>
            </tr>
            <tr class="prop">
                <td class="label">
                    <label><warehouse:message code="default.status.label"/></label>
                </td>
                <td class="value">
                    <span class="">
                        <g:set var="status" value="${productInstance.getStatus(session.warehouse.id, totalQuantity?:0)}"/>
                        ${warehouse.message(code:'enum.InventoryLevelStatus.'+status)}
                        <%--
                        <g:render template="../product/status" model="[product:productInstance,totalQuantity:totalQuantity,latestInventoryDate:latestInventoryDate]"/>
                        --%>
                    </span>
                </td>
            </tr>
            <tr class="prop">
                <td class="label">
                    <label><warehouse:message code="product.latestInventoryDate.label"/></label>
                </td>
                <td class="value">
                    <span class="">
                        <g:if test="${latestInventoryDate}">
                            <g:prettyDateFormat date="${latestInventoryDate}"/>
                            <div class="fade">
                                ${g.formatDate(date: latestInventoryDate, format: 'dd-MMM-yyyy') }<br/>
                                ${g.formatDate(date: latestInventoryDate, format: 'hh:mm:ss a') }
                            </div>

                        </g:if>
                        <g:else>
                            <span class="fade"><warehouse:message code="default.never.label" /></span>
                        </g:else>
                    </span>
                </td>
            </tr>
            <tr class="prop">
                <td class="label">
                    <label><warehouse:message code="inventoryLevel.binLocation.label"/></label>
                </td>
                <td class="value">
                    <g:if test="${inventoryLevelInstance?.binLocation}">
                        ${inventoryLevelInstance?.binLocation?:'' }
                    </g:if>
                    <g:else>
                        <span class="fade"><warehouse:message code="default.na.label"/></span>
                    </g:else>
                </td>
            </tr>

            <tr class="prop">
                <td class="label">
                    <label><warehouse:message code="inventoryLevel.minQuantity.label"/></label>
                </td>
                <td class="value">
                    <g:if test="${inventoryLevelInstance?.minQuantity}">
                        ${g.formatNumber(number: inventoryLevelInstance?.minQuantity, format: '###,###,###') }
                        <span class="">
                            <g:if test="${productInstance?.unitOfMeasure }">
                                <format:metadata obj="${productInstance?.unitOfMeasure}"/>
                            </g:if>
                            <g:else>
                                ${warehouse.message(code:'default.each.label') }
                            </g:else>
                        </span>
                    </g:if>
                    <g:else>
                        <span class="fade"><warehouse:message code="default.na.label"/></span>
                    </g:else>
                </td>
            </tr>
            <tr class="prop">
                <td class="label">
                    <label><warehouse:message code="inventoryLevel.reorderQuantity.label"/></label>
                </td>
                <td class="value">

                    <g:if test="${inventoryLevelInstance?.reorderQuantity}">
                        ${g.formatNumber(number: inventoryLevelInstance?.reorderQuantity, format: '###,###,###') }
                        <span class="">
                            <g:if test="${productInstance?.unitOfMeasure }">
                                <format:metadata obj="${productInstance?.unitOfMeasure}"/>
                            </g:if>
                            <g:else>
                                ${warehouse.message(code:'default.each.label') }
                            </g:else>
                        </span>
                    </g:if>
                    <g:else>
                        <span class="fade"><warehouse:message code="default.na.label"/></span>
                    </g:else>
                </td>
            </tr>


            <tr class="prop">
					<td class="label">
						<label><warehouse:message code="inventoryLevel.maxQuantity.label"/></label>
					</td>
					<td class="value">
						<g:if test="${inventoryLevelInstance?.maxQuantity}">
                            ${g.formatNumber(number: inventoryLevelInstance?.maxQuantity, format: '###,###,###') }
							<span class="">
								<g:if test="${productInstance?.unitOfMeasure }">
									<format:metadata obj="${productInstance?.unitOfMeasure}"/>
								</g:if>
								<g:else>
									${warehouse.message(code:'default.each.label') }
								</g:else>
							</span>
						</g:if>
						<g:else>
							<span class="fade"><warehouse:message code="default.na.label"/></span>
						</g:else>
					</td>				
				</tr>

                <tr class="prop">
                    <td class="label">
                        <label><warehouse:message code="product.totalValue.label"/></label>
                    </td>
                    <td class="value middle">
                        <g:if test="${productInstance?.pricePerUnit > 0 && totalQuantity > 0 }">
                            $${g.formatNumber(number: (totalQuantity*productInstance?.pricePerUnit), format: '###,###,###.00') }
                            <span class="fade">USD</span>
                        </g:if>
                        <g:else>
                            <span class="fade">$0.00 USD</span>
                        </g:else>
                    </td>
                </tr>

			</tbody>
		</table>			
	</div>
<div class="box">
    <h2>
        ${warehouse.message(code: 'product.details.label') }
    </h2>
    <table>
        <tbody>
            <tr class="prop">
                <td class="label">
                    <label>${warehouse.message(code: 'product.productCode.label') }</label>
                </td>
                <td>
                    ${productInstance?.productCode }
                </td>
            </tr>
            <tr class="prop">
                <td class="label">
                    <label><warehouse:message code="category.label"/></label>
                </td>
                <td class="value" id="productCategory">
                    <span class="">
                        <g:if test="${productInstance?.category?.name }">
                            <g:link controller="inventory" action="browse" params="[subcategoryId:productInstance?.category?.id,showHiddenProducts:'on',showOutOfStockProducts:'on',searchPerformed:true]">
                                <format:category category="${productInstance?.category}"/>
                            </g:link>
                        </g:if>
                        <g:else>
                            <span class="fade"><warehouse:message code="default.none.label"/></span>
                        </g:else>
                    </span>
                    <g:each var="category" in="${productInstance?.categories }">
                        <div>
                            <g:link controller="inventory" action="browse" params="[subcategoryId:category?.id,showHiddenProducts:true,showOutOfStockProducts:true,searchPerformed:true]">
                                <format:category category="${category}"/>
                            </g:link>
                        </div>
                    </g:each>

                </td>
            </tr>
            <g:if test="${productInstance?.productGroups }">
                <tr class="prop">
                    <td class="label left">
                        <label><warehouse:message code="productGroup.label"/></label>
                    </td>
                    <td class="value">
                        <g:each var="productGroup" in="${productInstance?.productGroups }">
                            <g:link controller="productGroup" action="edit" id="${productGroup.id }">
                                ${productGroup?.description }
                            </g:link>
                        </g:each>
                    </td>
                </tr>
            </g:if>
            <tr class="prop">
                <td class="label">
                    <label><warehouse:message code="product.units.label"/></label>
                </td>
                <td class="value" id="unitOfMeasure">
                    <span class="">
                        <g:if test="${productInstance?.unitOfMeasure }">
                            <format:metadata obj="${productInstance?.unitOfMeasure}"/>
                        </g:if>
                        <g:else>
                            <span class="fade"><warehouse:message code="default.none.label"/></span>
                        </g:else>
                    </span>
                </td>
            </tr>

            <tr class="prop">
                <td class="label">
                    <label><warehouse:message code="product.manufacturer.label"/></label>
                </td>
                <td class="value" id="manufacturer">
                    <span class="">
                        <g:if test="${productInstance?.manufacturer }">
                            ${productInstance?.manufacturer }
                        </g:if>
                        <g:else>
                            <span class="fade"><warehouse:message code="default.none.label"/></span>
                        </g:else>
                    </span>
                </td>
            </tr>
            <tr class="prop">
                <td class="label">
                    <label><warehouse:message code="product.brandName.label"/></label>
                </td>
                <td class="value" id="brandName">
                    <span class="">
                        <g:if test="${productInstance?.brandName }">
                            ${productInstance?.brandName }
                        </g:if>
                        <g:else>
                            <span class="fade"><warehouse:message code="default.none.label"/></span>
                        </g:else>
                    </span>
                </td>
            </tr>
            <tr class="prop">
                <td class="label">
                    <label><warehouse:message code="product.modelNumber.label"/></label>
                </td>
                <td class="value" id="modelNumber">
                    <span class="">
                        <g:if test="${productInstance?.modelNumber }">
                            ${productInstance?.modelNumber }
                        </g:if>
                        <g:else>
                            <span class="fade"><warehouse:message code="default.none.label"/></span>
                        </g:else>
                    </span>
                </td>
            </tr>
            <tr class="prop">
                <td class="label">
                    <label><warehouse:message code="product.manufacturerCode.label"/></label>
                </td>
                <td class="value" id="manufacturerCode">
                    <span class="">
                        <g:if test="${productInstance?.manufacturerCode }">
                            ${productInstance?.manufacturerCode }
                        </g:if>
                        <g:else>
                            <span class="fade"><warehouse:message code="default.none.label"/></span>
                        </g:else>
                    </span>
                </td>
            </tr>
            <tr class="prop">
                <td class="label">
                    <label><warehouse:message code="product.vendor.label"/></label>
                </td>
                <td class="value">
                    <span class="">
                        <g:if test="${productInstance?.vendor }">
                            ${productInstance?.vendor }
                        </g:if>
                        <g:else>
                            <span class="fade"><warehouse:message code="default.none.label"/></span>
                        </g:else>
                    </span>
                </td>
            </tr>

            <tr class="prop">
                <td class="label">
                    <label><warehouse:message code="product.vendorCode.label"/></label>
                </td>
                <td class="value">
                    <span class="">
                        <g:if test="${productInstance?.vendorCode }">
                            ${productInstance?.vendorCode }
                        </g:if>
                        <g:else>
                            <span class="fade"><warehouse:message code="default.none.label"/></span>
                        </g:else>
                    </span>
                </td>
            </tr>
            <g:if test="${productInstance?.modelNumber }">
                <tr class="prop">
                    <td class="label">
                        <label><warehouse:message code="product.modelNumber.label"/></label>
                    </td>
                    <td class="value">
                        <span class="">
                            <g:if test="${productInstance?.modelNumber }">
                                ${productInstance?.modelNumber }
                            </g:if>
                            <g:else>
                                <span class="fade"><warehouse:message code="default.none.label"/></span>
                            </g:else>
                        </span>
                    </td>
                </tr>
            </g:if>
            <g:if test="${productInstance?.upc }">
                <tr class="prop">
                    <td class="label">
                        <label><warehouse:message code="product.upc.label"/></label>
                    </td>
                    <td class="value">
                        <span class="">
                            <g:if test="${productInstance?.upc }">
                                ${productInstance?.upc }
                            </g:if>
                            <g:else>
                                <span class="fade"><warehouse:message code="default.none.label"/></span>
                            </g:else>
                        </span>
                    </td>
                </tr>
            </g:if>
            <g:if test="${productInstance?.ndc }">
                <tr class="prop">
                    <td class="label">
                        <label><warehouse:message code="product.ndc.label"/></label>
                    </td>
                    <td class="value">
                        <span class="">
                            <g:if test="${productInstance?.ndc }">
                                ${productInstance?.ndc }
                            </g:if>
                            <g:else>
                                <span class="fade"><warehouse:message code="default.none.label"/></span>
                            </g:else>
                        </span>
                    </td>
                </tr>
            </g:if>
            <tr class="prop">
                <td class="label">
                    <label><warehouse:message code="product.coldChain.label"/></label>
                </td>
                <td class="value">
                    <span class="">${productInstance?.coldChain ? warehouse.message(code:'default.yes.label') : warehouse.message(code:'default.no.label') }</span>
                </td>
            </tr>
            <tr class="prop">
                <td class="label">
                    <label><warehouse:message code="product.controlledSubstance.label"/></label>
                </td>
                <td class="value">
                    <span class="">${productInstance?.controlledSubstance ? warehouse.message(code:'default.yes.label') : warehouse.message(code:'default.no.label') }</span>
                </td>
            </tr>
            <tr class="prop">
                <td class="label">
                    <label><warehouse:message code="product.hazardousMaterial.label"/></label>
                </td>
                <td class="value">
                    <span class="">${productInstance?.hazardousMaterial ? warehouse.message(code:'default.yes.label') : warehouse.message(code:'default.no.label') }</span>
                </td>
            </tr>
            <tr class="prop">
                <td class="label">
                    <label><warehouse:message code="product.reconditioned.label"/></label>
                </td>
                <td class="value">
                    <span class="">${productInstance?.reconditioned ? warehouse.message(code:'default.yes.label') : warehouse.message(code:'default.no.label') }</span>
                </td>
            </tr>

            <g:set var="status" value="${0 }"/>
            <g:each var="productAttribute" in="${productInstance?.attributes}">
                <tr class="prop">
                    <td class="label left">
                        <label><format:metadata obj="${productAttribute?.attribute}"/></label>
                    </td>
                    <td>
                        <span class="">${productAttribute.value }</span>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>
</div>
<g:if test="${productInstance?.packages }">
    <div class="box">
        <h2>
            ${warehouse.message(code: 'product.packaging.label') }
        </h2>
        <table>
            <tbody>
                <g:each var="productPackage" in="${productInstance?.packages?.sort { it.quantity }}">
                    <tr class="prop">
                        <td class="label">
                            <label>${productPackage?.uom }</label>
                        </td>
                        <td class="value">
                            <span class="">
                                ${productPackage?.uom?.code }/${productPackage?.quantity }
                            </span>
                        </td>
                    </tr>
                </g:each>
            </tbody>
        </table>
    </div>
</g:if>
<div class="box">
    <h2>
        ${warehouse.message(code: 'default.auditing.label') }
    </h2>
    <table>
        <tbody>
            <tr class="prop">
                <td class="label">
                    <label><warehouse:message code="product.createdBy.label"/></label>
                </td>
                <td class="value">
                    <span class="fade">${productInstance?.createdBy?.name?:warehouse.message(code: 'default.unknown.label') }</span> <br/>
                </td>
            </tr>
            <tr class="prop">
                <td class="label">
                    <label><warehouse:message code="product.modifiedBy.label"/></label>
                </td>
                <td class="value">
                    <span class="fade">${productInstance?.updatedBy?.name?:warehouse.message(code: 'default.unknown.label') }</span> <br/>
                </td>
            </tr>
            <tr class="prop">
                <td class="label">
                    <label><warehouse:message code="product.createdOn.label"/></label>
                </td>
                <td class="value">
                    <span class="fade">
                        ${g.formatDate(date: productInstance?.dateCreated, format: 'dd-MMM-yyyy')}
                        ${g.formatDate(date: productInstance?.dateCreated, format: 'hh:mm a')}
                    </span>

                </td>
            </tr>



            <tr class="prop">
                <td class="label"  >
                    <label><warehouse:message code="product.modifiedOn.label"/></label>
                </td>
                <td class="value">
                    <span class="fade">
                        ${g.formatDate(date: productInstance?.lastUpdated, format: 'dd-MMM-yyyy')}
                        ${g.formatDate(date: productInstance?.lastUpdated, format: 'hh:mm a')}
                    </span>
                </td>
            </tr>

        </tbody>
    </table>
</div>
		
</div>
<script>
	function openDialog(dialogId, imgId) { 
		$(dialogId).dialog({autoOpen: true, modal: true, width: 600, height: 400});
	}
	function closeDialog(dialogId, imgId) { 
		$(dialogId).dialog('close');
	}
</script>


