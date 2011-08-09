<div id="category${i }" class="category-div" <g:if test="${hidden}">style="display:none;"</g:if>>
    
    <g:hiddenField name='categoriesList[${i }].deleted' value='false'/>
    <g:hiddenField name='categoriesList[${i }].new' value='false'/>
 
 	<%-- 
 	<g:textField name='categoriesList[${i }].primary' value='${category?.primary}' />
 	--%>
 	<g:if test="${category }">
	 	<format:category category="${category}"/>
 		<g:hiddenField name='categoriesList[${i }].id' value="${category?.id }"/>
 	</g:if>
 	<g:else> 	
		<select id="categoriesList[${i }].id" name="categoriesList[${i }].id" >	
			<g:render template="../category/selectOptions" model="[category:rootCategory, selected:category, level: 0]"/>
		</select>	   
	</g:else>
    <span class="del-category">
        <img src="${resource(dir:'images/icons/silk', file:'delete.png')}"
            style="vertical-align:middle;"/>
    </span>
</div>