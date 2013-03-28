<%--
	<li rel="${attrs?.from?.id }">
		${attrs.from}
		<g:if test="${attrs.from.categories }">
			<ul>
				<g:each var="category" in="${attrs.from.categories}">
					<g:selectCategory_v2 from="${category }" depth="${attrs.depth+1 }"/>
				</g:each>
			</ul>
		</g:if>
	</li>
--%>
<select>

    <g:each in="${attrs.categories}" var="category">
        <option>
            ${category.name}
            <%--${category.getHierarchyAsString(" &gt; ")}--%>
        </option>
    </g:each>
</select>

