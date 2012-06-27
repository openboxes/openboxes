
<table>
	<tr>
		<td style="width: 50%;">
			<g:set var="selectedProducts" value="${attrs.value }"/>
			<h2>Selected (${selectedProducts.size() })</h2>
			<div class="" style="overflow: auto; mix-height: 100px; height: 233px;">
				<table>
					<tbody>
						<g:each var="product" in="${selectedProducts }" status="i">
							<tr class="prop ${i%2?'even':'odd' }">
								<td width="1%">
									<g:checkBox name="${attrs.name }" value="${product.id }" checked="${attrs.value.contains(product) }"></g:checkBox>
								</td>
								<td>
									${product.name }
									<span class="fade">
										${product.manufacturer }
									</span>
								</td>
							</tr>
						</g:each>
					</tbody>
				</table>
			</div>
		</td>
	
		<td>
		
			<g:set var="availableProducts" value="${attrs.products.findAll { !attrs.value.contains(it) }}"/>
			<h2>Available (${availableProducts.size() })</h2>
			<div style="padding: 10px;" class="odd">
				<g:textField id="productFilter" name="productFilter" value="" size="40" class="medium text"/>
			</div>
		
			<div class="" style="overflow: auto; mix-height: 100px; height: 200px;">
				<table id="data">
					<tbody>
						<g:each var="product" in="${availableProducts }" status="i">
							<tr class="prop ${i%2?'odd':'even' }">
								<td width="1%">
									<%-- 
									<g:link action="addProducts" id="${productGroupInstance?.id }" params="['product.id':product.id ]">
										<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" />
									</g:link>
									--%>
									<g:checkBox class="product-checkbox" name="${attrs.name }" value="${product.id }" checked="${attrs.value.contains(product) }"></g:checkBox>
								</td>
								<td>
									${product.name }
									<span class="fade">
										${product.manufacturer }
									</span>
								</td>
							</tr>
						</g:each>
					</tbody>
				</table>
			</div>
		</td>
	</tr>
</table>

<script>
	//filter results based on query
	function filter(selector, query, andOr) {
		query = $.trim(query); //trim white space
		query = query.replace(/ /gi, andOr); //add OR for regex query
	
		$(selector).each(function() {
			($(this).text().search(new RegExp(query, "i")) < 0) ? $(this).hide().removeClass('visible') : $(this).show().addClass('visible');
		});
	}

	//used to apply alternating row styles
	function zebraRows(selector, className) {
	  $(selector).removeClass(className).addClass(className);
	}
	    

	$(document).ready(function() {			
		zebraRows('#data tbody tr:odd', 'odd');


		$("#productFilter").watermark("${warehouse.message(code:'product.filterProducts.label')}");	
		
		$("#category\\.id").change(function() {
			$(this).closest("form").submit();
		});

		//$('#demotable').tableFilter();
		//default each row to visible
		$('tbody tr').addClass('visible');
		$('#productFilter').keyup(function(event) {
			//if esc is pressed or nothing is entered
			if (event.keyCode == 27 || $(this).val() == '') {
    			//if esc is pressed we want to clear the value of search box
		    	$(this).val('');

			    //we want each row to be visible because if nothing
			    //is entered then all rows are matched.
				$('#data tbody tr').removeClass('visible').show().addClass('visible');
			}
			//if there is text, lets filter
			else {
				filter('#data tbody tr', $(this).val(), '|');  // '|' or '&'
			}
			//reapply zebra rows
			$('.visible td').removeClass('odd');
			//zebraRows('.visible:odd td', 'odd');	
		});   
	});		
</script>
