<link rel="stylesheet"
	href="${createLinkTo(dir:'js/chosen',file:'chosen.css')}"
	type="text/css" media="screen, projection" />
	
<script src="${createLinkTo(dir:'js/chosen/', file:'chosen.jquery.js')}"
	type="text/javascript"></script>

	<g:selectCategoryWithChosen		
		id="category.id" name="category.id" 
		rootNode="${attrs.rootNode }"
		noSelection="['null':'Choose a primary category']"
		value="${attrs?.product?.category }" 
		class="chzn-select" />	
	 
<script type="text/javascript">
$(".chzn-select").chosen();
$(".chzn-select-deselect").chosen({allow_single_deselect:true});
</script>
