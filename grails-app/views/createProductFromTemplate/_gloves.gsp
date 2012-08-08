<div class="template-form">
	<div>
		<label for="category">Category:</label>
		<g:categorySelect  id="category.id" name="category.id" value="${product?.category?.id}"/>			
		
	</div>
	<div>
		<label for="application">Application:</label>
		<g:select name="application" from="${product.constraints.application.inList}" 
          value="${product.application}" class="required"/>
	</div>
	<div class="">	
		<label for="latex">Latex Free?</label>
		<g:select name="latex" from="${product.constraints.latex.inList}" 
          value="${product.latex}" class="required"/>
		
	</div>		
	<div class="">	
		<label for="powder">Powdered?</label>
		<g:select name="powder" from="${product.constraints.powder.inList}" 
          value="${product.powder}" class="required"/>
		

	</div>		
	<div class="">	
		<label for="sterility">Sterility?</label>
		<g:select name="sterility" from="${product.constraints.sterility.inList}" 
          value="${product.sterility}" class="required"/>
		
	</div>		
	<div class="">	
		<label for="material">Material?</label>
		<g:select name="material" from="${product.constraints.material.inList}" 
          value="${product.material}" class="required"/>

	</div>		
	<div>
		<label for="size">Size</label>
		<input type="text" id="size" name="size" class="text required"  value=""/>
	</div>

	<div>
		<label for="title">Title:</label>
		<input type="text" id="title" name="title" class="text" size="80" value="" readonly="readonly" />
	</div>		
<%--			
				
	<div>
		<label for="description">Description:</label>
		<input type="text" id="description" name="description" class="text medium" value="${product.description}" />
	</div>	
	
	<div>		
		<label for="brandName">Brand name:</label>				
		<input type="text" id="brandName" name="brandName" class="text large" value="" /></td>
	</div>
	
	<div>		
		<label for="manufacturer">Manufacturer:</label>
		<input type="text" id="manufacturer"
			name="manufacturer" class="text large"
			value="${product.manufacturer}" /></td>
	</div>
	<div>	
		<label for="manufacturerCode">Manufacturer
				Code:</label></td>
		<input type="text" id="manufacturerCode"
			name="manufacturerCode" class="text large"
			value="${product.manufacturerCode}" /></td>
	</div>	
 --%>

</div>

<script>			
$(document).ready(function() {
	setTitle();

	function setTitle() {
		var title = [];		
		title.push("Glove");
		$('.required').each(function(index) {			
			if ($(this).val() && $(this).val() != 'Not Specified') { 
				title.push($(this).val());
			}
			else { 
				//title.push("<span class='fade'>[unspecified]</span>");
			}
		});
		
		$("#title").val(title.join(', '));
	}
	
	$(".required").keyup(function() { 
		setTitle();
	});
	$(".required").change(function() {
		setTitle();
	});
	
	
});
</script> 	