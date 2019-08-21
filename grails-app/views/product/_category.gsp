<div id="category${i }" class="category-selector" style="${hidden?'display:none;':''} padding: 5px;">
    
    <g:hiddenField name='categoriesList[${i }].deleted' value='false'/>
    <g:hiddenField name='categoriesList[${i }].new' value='false'/>

    <span class="del-category">
        <img src="${resource(dir:'images/icons/silk', file:'delete.png')}"
            style="vertical-align:middle;"/>
    </span>
 	<g:if test="${category }">
	 	<format:category category="${category}"/>
 		<g:hiddenField name='categoriesList[${i }].id' value="${category?.id }"/>
 	</g:if>
 	<g:else> 	
		<select id="categoriesList[${i }].id" name="categoriesList[${i }].id" >	
			<g:render template="../category/selectOptions" model="[category:rootCategory, selected:category, level: 0]"/>
		</select>	   
	</g:else>
</div>