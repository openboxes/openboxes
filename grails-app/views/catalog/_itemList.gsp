<div class="list">
<table>
	<thead>
		<tr>
			<th>Image</th>
			<g:sortableColumn property="name" title="Name" action="${action}"
				params="${params}" />
			<th>Product</th>
			<th>Category</th>
			<th>Tags</th>
			<g:sortableColumn property="price" title="Price" action="${action}"
				params="${params}" />
		</tr>
	</thead>
	<tbody>
		<g:each in="${itemList}" status="i" var="item">
			<tr class="${(i % 2) == 0 ? "odd" : "even"}">
				<td><g:link controller="item" action="show" id="${item.id}">
					<img src="${ps.thumbnailImage(imageUrl:item.imageUrl)}" alt="" />
				</g:link></td>
				<td>
				<h2><g:link controller="item" action="show" id="${item.id}">
					${item.name?.encodeAsHTML()}
				</g:link></h2>
				<p>
				${item.description?.encodeAsHTML()}
				</p>
				</td>
				<td class="nowrap"><g:link controller="item" action="byProduct"
					id="${item.product.id}">
					${item.product.name.encodeAsHTML()}
				</g:link></td>
				<td class="nowrap">					
					<g:each in="${item.product.categories}" var="category">
						<g:link controller="item" action="byCategory" id="${category.id}">${category.name.encodeAsHTML}</g:link> <br/>
					</g:each>
				</td>
				<td><g:each in="${item.tags}" var="tag">
					<g:link controller="tag" action="${tag.tag}">
						${tag.tag.encodeAsHTML()}
					</g:link>
				</g:each></td>
				<td>$${item.price?.encodeAsHTML()}</td>
			</tr>
		</g:each>
	</tbody>
</table>
</div>

