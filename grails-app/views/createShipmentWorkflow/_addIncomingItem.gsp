<div id="dlgAddIncomingItem" title="${warehouse.message(code:'shipping.addIncomingItem.label')}" style="display: none;" >

	<div id="searchItemForm" style="display: ${item?'none':''};">
		<g:form name="searchItem" action="createShipment">	
			<table>
				<tbody>
					<tr>
						<td>
							<g:textField name="productSearch" class="text autocomplete" style="width: 550px" autocomplete="off"/>
							<%-- 
							<g:autoSuggest name="product" jsonUrl="${request.contextPath }/json/findProductByName" 
								width="300" valueId="" valueName="" styleClass="text"/>	
							--%>
						</td>
					</tr>
				</tbody>
			</table>
			<br/>
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
		</g:form>
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
							<%-- 
							<g:autoSuggestString id="lotNumber" name="lotNumber" 
								jsonUrl="${request.contextPath }/json/findLotsByName?productId=" 
								styleClass="text"
								width="200" value="${item?.lotNumber}"/> 
								--%>
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
							<%-- 
							<g:datePicker name="expirationDate" default="none" precision="day"
								value="${item?.expirationDate }" noSelection="['null':'']"
								years="${(1900 + (new Date().year))..(1900+ (new Date() + (20 * 365)).year)}" />
								--%>
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
									<%-- 
									<g:submitButton name="deleteItem" value="${warehouse.message(code:'shipping.removeItem.label')}" 
										onclick="return confirm('${warehouse.message(code:'shipping.confirm.deleteItem.message')}')"/>
									--%>
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
		    	"<td>" + rowdata.category.name + "</td>" +
		    	"<td>" + rowdata.label + "</td>" +
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
							//var row = $(rowBuilder(this));
							//$.data(row, "mydata", this);
							//$(row).data("mydata", this);
							//alert(this);
							//row.appendTo("#results > tbody");
							$(rowBuilder(this)).data("mydata",this).appendTo("#results > tbody");
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
		
		$("button.choose").livequery('click', function(event) { 
			event.preventDefault();
			var row = $(this).closest("tr");
			var data = $(row).data("mydata");
			$("#hiddenProduct").val(data.value);
			var displayProduct = "<span class='fade'>" + data.category.name + " &rsaquo;</span> " + data.label 
			$("#displayProduct").html(displayProduct);
			$("#searchItemForm").toggle();
			$("#editItemForm").toggle();
		});
		
	});
</script>