<div id="dlgAddIncomingItem" title="${warehouse.message(code:'shipping.addIncomingItem.label')}" style="display: none;" >

	<div id="searchItemForm" style="display: ${item?'none':''};">
		<g:form name="searchItem" action="createShipment">	
			<table>
				<tbody>
					<tr>
						<td>
							<g:textField name="productSearch" class="text autocomplete" style="width: 550px" autocomplete="off"/>
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
                    <warehouse:message code="category.label"/>
                </th>
                <th>
                    <warehouse:message code="product.label"/>
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
				<span class="fade">
					<format:category category="${item?.product?.category }"/>
					&rsaquo;
				</span>
				<format:product product="${item.product }"/>
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
							<g:textField name="lotNumber" class="text" value="${item?.lotNumber }"/>
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
								readOnly="true"
								cssClass="text"/>
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name">
							<label><warehouse:message code="default.quantity.label" /></label>
						</td>                            
						<td valign="top" class="value">
							<g:textField name="quantity" value="${item?.quantity }" size="5" class="text" /> 
						</td>
					</tr>  	        
					<tr class="prop">
						<td valign="top" class="name">
							<label><warehouse:message code="shipping.recipient.label" /></label>
						</td>                            
						<td valign="top" class="value">
							<g:autoSuggest name="recipient" jsonUrl="${request.contextPath }/json/findPersonByName" 
								width="200" valueId="${item?.recipient?.id }" valueName="${item?.recipient?.name }" styleClass="text"/>	
						</td>
					</tr>					
					<tr>
						<td class="name"></td>
						<td>
							<div class="left">
								<g:if test="${item}">
									<g:submitButton name="updateItem" value="${warehouse.message(code:'shipping.saveItem.label')}"/>
								</g:if>
								<g:else>
									<g:submitButton name="saveItem" value="${warehouse.message(code:'shipping.saveItem.label')}"/>
									<g:if test="${addItemToContainerId}">									
										<g:submitButton name="addAnotherItem" value="${warehouse.message(code:'shipping.saveItemAndAddAnother.label')}"/>
									</g:if>
								</g:else>
								<button name="cancelDialog" type="reset" onclick="$('#dlgAddIncomingItem').dialog('close');">
									<warehouse:message code="default.button.cancel.label"/>
								</button>
							</div>
						</td>
					</tr>
				</tbody>
			</table>
		</g:form>		
		
		
		<g:if test="${!item }">
			<hr/>
			<a href="javascript:void(0);" class="back">&lsaquo; <warehouse:message code="shipping.returnToSearch.label"/></a>	
		</g:if>
	</div>		
</div>	
		     
<script>

	function rowBuilder(rowdata) {
	    return "<tr id='" + rowdata.value + "'>" + 
		    	"<td class='center'><button class=\"choose\">Choose</button></td>" +
		    	"<td class='category'>" + rowdata.category.name + "</td>" +
		    	"<td class='label'>" + rowdata.label + "</td>" +
	    	"</tr>";
	}

	$(document).ready(function() {
		$("#dlgAddIncomingItem").dialog({ 
			autoOpen: true, 
			modal: true, 
			width: 600,
			height: 300
		});	

		$(".autocomplete").keyup(function(event, ui) {
			var value = $(this).val();
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
		});

		$(".back").click(function(event) { 
			$("#searchItemForm").toggle();
			$("#editItemForm").toggle();
		}); 
		
		$("button.choose").live('click', function(event) {
			var row = $(this).closest("tr");
			$("#hiddenProduct").val(row.attr('id'));
			var displayProduct = "<span class='fade'>" + row.find('.category').text() + " &rsaquo;</span> " + row.find('.label').text()
			$("#displayProduct").html(displayProduct);
			$("#searchItemForm").toggle();
			$("#editItemForm").toggle();
		});
		
	});
</script>