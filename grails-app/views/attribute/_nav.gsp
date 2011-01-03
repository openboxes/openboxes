<div>            	
	<span class="menuButton">
		<g:link class="list" controller="product" action="browse"><g:message code="default.browse.label" args="['Products']"/></g:link>
	</span>
	<span class="menuButton">
		<g:link class="new" controller="product" action="create"><g:message code="default.add.label" args="['Product']"/></g:link> 			
	</span>
	<span class="menuButton">
		<g:link class="edit" controller="category" action="tree"><g:message code="default.edit.label" args="['Categories']"/></g:link>
	</span>
	<span class="menuButton">
		<g:link class="edit" controller="attribute" action="list"><g:message code="default.edit.label" args="['Attributes']"/></g:link>
	</span>
</div>



<%-- 
<img src="${createLinkTo(dir: 'images/icons/silk', file: 'bullet_white.png') }"/>
<g:form method="get" action="browse">
	<table>
		<tr class="prop">
			<td class="">
				<label>Search </label>

				<g:textField name="nameContains" value="${params.nameContains}" size="30"/>		
				<span class="buttons">
					<button type="submit" class="positive">
						${message(code: 'default.button.go.label', default: 'Go')}</button>
				</span>											
			</td>
		</tr>
	</table>	
</g:form>			

--%>
			
