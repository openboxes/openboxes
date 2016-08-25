<link rel="stylesheet"
	href="${resource(dir:'js/chosen',file:'chosen.css')}"
	type="text/css" media="screen, projection" />

	<g:selectCategoryWithChosen		
		id="category.id" name="category.id" 
		rootNode="${attrs.rootNode }"
		noSelection="['null':'Choose a primary category']"
		value="${attrs?.product?.category }" 
		class="chzn-select" />	
	 
