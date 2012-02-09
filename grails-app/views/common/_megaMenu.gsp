<!--Example MegaMenu Starts-->
<ul class="megamenu">
	<li><a href="javascript:void(0)"><warehouse:message code="inventory.label" /></a>
		<div>		
			<h3>something</h3>							
			<ul>
				<li>
					<span class="menuButton">
						<g:link controller="inventory" action="browse"><warehouse:message code="inventory.browse.label"/></g:link>
					</span>
				</li>
				<li>
					<span class="menuButton">
						<g:link controller="product" action="create"><warehouse:message code="product.add.label"/></g:link>
					</span>
				</li>
			</ul>
		</div>

	</li>
	<li><a href="javascript:void(0)"><warehouse:message code="orders.label"  default="Orders"/></a>
		<div style="width: 265px;">
			<h3>Form Content Example</h3>
			<form action="#" method="get" id="form-content">
				<fieldset>
					<legend>Login</legend>
					<div class="FieldLabel">Login</div>
					<div class="FieldItem">
						<input type="text" class="Text" />
					</div>
					<div class="FieldLabel">Password</div>
					<div class="FieldItem">
						<input type="password" class="Text" />
					</div>
					<div class="FieldItem">
						<input type="button" value="Login" />
					</div>
				</fieldset>
			</form>
		</div></li>
	<li><a href="javascript:void(0)"><warehouse:message code="requests.label"  default="Requests"/></a>
		<div style="width: 300px;">
			<ul id="list-content">
				<li>Point 1 is the first point
					<ul>
						<li>Point 1.1 goes here</li>
						<li>Point 1.2 goes here</li>
						<li>Point 1.3 can go here also</li>
					</ul>
				</li>
				<li>Point 2 is the second point
					<ul>
						<li>Point 2.1 is a sub point</li>
						<li>Point 2.2 is a sub point</li>
					</ul>
				</li>
				<li>Point 3 is the third point
					<ul>
						<li>Point 3.1 is a sub point</li>
						<li>Point 3.2 is a sub point</li>
					</ul>
				</li>
				<li>Point 4 is the lone fourth point without any children</li>
			</ul>
		</div></li>
	<li><a href="javascript:void(0)"><warehouse:message code="shipping.label" /></a>
		<div style="width: 520px;">
			<p id="paragraph-content">Lorem ipsum dolor sit amet,
				consectetur adipisicing elit, sed do eiusmod tempor incididunt ut
				labore et dolore magna aliqua. Ut enim ad minim veniam, quis
				nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo
				consequat. Duis aute irure dolor in reprehenderit in voluptate
				velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint
				occaecat cupidatat non proident, sunt in culpa qui officia
				deserunt mollit anim id est laborum.</p>
			<blockquote>Sed ut perspiciatis unde omnis iste natus
				error sit voluptatem accusantium doloremque laudantium, totam rem
				aperiam, eaque ipsa quae ab illo inventore veritatis et quasi
				architecto beatae vitae dicta sunt explicabo.</blockquote>
		</div></li>
	<li><a href="javascript:void(0)"><warehouse:message code="receiving.label" /></a>
		<div style="width: 500px;">See how the position of the menu
			gets adjusted to stay within the outer bounds.</div></li>
	<li><a href="javascript:void(0)"><warehouse:message code="report.label" /></a></li>
</ul>
<!--MegaMenu Ends-->