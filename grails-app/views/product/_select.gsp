<div class="box">
	<g:textField id="productFilter" name="productFilter" value="" size="40" class="medium text"/>
</div>

<div class="" style="overflow: auto; mix-height: 100px; height: 200px;">
	<table id="data">
		<tbody>
			<g:each var="product" in="${attrs.products }" status="i">
				<tr class="prop ${i%2?'even':'odd' }">
					<td width="1">
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
<script src="${createLinkTo(dir:'js/jquery.tableFilter/', file:'picnet.table.filter.min.js')}" type="text/javascript" ></script>
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
		
		$("#category\\.id").change(function() {
			$(this).closest("form").submit();
		});
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
		});   
	});		
</script>
