<g:form method="GET" controller="inventory" action="browse" style="display: inline;">
	<g:textField name="searchTerms" value="${params.searchTerms }" size="18"/>
	<button type="submit" class="" name="submitSearch">
		<img src="${createLinkTo(dir: 'images/icons/silk', file: 'zoom.png' )}" class="middle"/>
		&nbsp;Find&nbsp;</button>
</g:form>						
