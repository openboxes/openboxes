<div id="dlgAddIncomingItem" title="${warehouse.message(code:'shipping.addIncomingItem.label')}" style="display: none;" >
	<div id="searchItemForm" style="display: ${item?'none':''}; overflow: auto; max-height: 400px;">
		<g:form name="searchItem" action="createShipment">	
			<table>
				<tbody>
					<tr>
						<td class="center">
							<g:textField name="productSearch" class="text autocomplete" style="width: 100%" autocomplete="off"/>
						</td>
					</tr>
				</tbody>
			</table>
			<br/>

		</g:form>
        <table id="results" style="display: none;">
            <thead>
            <tr>
                <th class="center">

                </th>
                <th>
                    <warehouse:message code="product.productCode.label"/>
                </th>
                <th>
                    <warehouse:message code="product.label"/>
                </th>
                <th>
                    <warehouse:message code="product.manufacturer.label"/>
                </th>
                <th>
                    <warehouse:message code="category.label"/>
                </th>

            </tr>
            </thead>
            <tbody>
            <%-- Gets populated after user enters search terms. --%>
            </tbody>
        </table>
	</div>	


	<div id="editItemForm" style="display: ${item?'':'none'};">
		<div style="margin: 5px; padding: 5px;" class="title">
			<g:if test="${item?.product}">
				<span class="fade">${item?.product?.productCode}</span> <format:product product="${item.product }"/>
			</g:if>
			<g:else>
				<span id="displayProduct"></span>
			</g:else>
		</div>
	
		<jqvalui:renderValidationScript for="org.pih.warehouse.shipping.ShipmentItem" form="addIncomingItem"/>
		<g:form name="addIncomingItem" action="createShipment">	
			<g:if test="${item }">
				<g:hiddenField name="item.id" value="${item.id}" />
				<g:hiddenField name="container.id" value="${item?.container?.id }"/>
				<g:hiddenField name="product.id" value="${item?.product?.id }"/>
				<g:hiddenField name="inventoryItem.id" value="${item?.inventoryItem?.id }"/>
			</g:if>
			<g:else>
				<g:hiddenField id="containerId" name="container.id" value="${addItemToContainerId?:'' }" />
				<g:hiddenField id="hiddenProduct" name="product.id"/>
			</g:else>
			
			<table>
				<tbody>
					<tr class="prop">
						<td valign="top" class="name">
							<label><warehouse:message code="default.lotSerialNo.label" /></label>
						</td>                            
						<td valign="top" class="value">
							<g:textField name="lotNumber" class="text lotNumber" value="${item?.lotNumber }" size="60"/>
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name">
							<label><warehouse:message code="inventoryItem.expirationDate.label"/></label>						
						</td>
						<td valign="top" class="value">
							<g:jqueryDatePicker id="expirationDate" name="expirationDate" 
								value="${item?.expirationDate }" 
								format="MM/dd/yyyy"
                                size="60"
								cssClass="text"/>
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name">
							<label><warehouse:message code="default.quantity.label" /></label>
						</td>                            
						<td valign="top" class="value">
							<g:textField name="quantity" value="${item?.quantity }" size="60" class="text" />
                            ${item?.product?.unitOfMeasure}
						</td>
					</tr>  	        
					<tr class="prop">
						<td valign="top" class="name">
							<label><warehouse:message code="shipping.recipient.label" /></label>
						</td>                            
						<td valign="top" class="value">
							<g:autoSuggest name="recipient" jsonUrl="${request.contextPath }/json/findPersonByName" 
								 size="60" valueId="${item?.recipient?.id }" valueName="${item?.recipient?.name }" styleClass="text"/>
						</td>
					</tr>					
					<tr>
						<td class="name"></td>
						<td>
							<div class="left">
								<g:if test="${item}">
									<g:submitButton class="button" name="updateItem" value="${warehouse.message(code:'shipping.saveItem.label')}"/>
								</g:if>
								<g:else>
									<g:submitButton name="saveItem" class="button" value="${warehouse.message(code:'shipping.saveItem.label')}"/>
									<g:if test="${addItemToContainerId}">									
										<g:submitButton class="button" name="addAnotherItem" value="${warehouse.message(code:'shipping.saveItemAndAddAnother.label')}"/>
									</g:if>
								</g:else>
								<button name="cancelDialog" type="reset" onclick="$('#dlgAddIncomingItem').dialog('close');" class="button" >
									<warehouse:message code="default.button.close.label"/>
								</button>

								<g:if test="${!item }">
									<a href="javascript:void(0);" class="button back"><warehouse:message code="default.button.back.label"/></a>
								</g:if>

							</div>
						</td>
					</tr>
				</tbody>
			</table>
		</g:form>		
		
		
	</div>
</div>	
		     
<script>

	function rowBuilder(rowdata) {
	    return "<tr id='" + rowdata.value + "'>" + 
		    	"<td class='center'><button class=\"choose\">Choose</button></td>" +
		    	"<td class='productCode'>" + rowdata.product.productCode + "</td>" +
                "<td class='productName'>" + rowdata.product.name + "</td>" +
                "<td class='manufacturer'>" + rowdata.product.manufacturer + "</td>" +
                "<td class='category'>" + rowdata.category.name + "</td>" +
                "</tr>";
	}

	$(document).ready(function() {
		$("#dlgAddIncomingItem").dialog({ 
			autoOpen: true, 
			modal: true, 
			width: 800
		});

		$("#searchItemForm input.autocomplete").keyup(function(event, ui) {
            var searchable = $(this);
            setTimeout(function(){
                var value = searchable.val();
                if (value && value.length > 2) {
                    $.ajax({
                        url: "${request.contextPath }/json/findProductByName",
                        data: "term=" + value + "&warehouseId=" + $("#currentLocationId").val(),
                        success: function(data, textStatus, jqXHR){
                            $("#results > tbody").find("tr").remove();
                            $.each(data, function(){
                                $(rowBuilder(this)).appendTo("#results > tbody");
                            });
                            $("#results tr").removeClass("odd").filter(":odd").addClass("odd");
                            $("#results").show();
                        }
                    });
                }
                else {
                    $("#results > tbody").find("tr").remove();
                    $("#results").hide();
                }
            },300);//set 300ms delay to prevent each key stroke firing search
		});

		$(".back").click(function(event) { 
			$("#searchItemForm").toggle();
			$("#editItemForm").toggle();
		}); 
		
		$("button.choose").live('click', function(event) {
			var row = $(this).closest("tr");
			$("#hiddenProduct").val(row.attr('id'));
			var displayProduct = "<span class='fade'>" + row.find('.productCode').text() + "</span> "
                    + row.find('.productName').text();
			$("#displayProduct").html(displayProduct);
			$("#searchItemForm").toggle();
			$("#editItemForm").toggle();
		});
		
	});
</script>