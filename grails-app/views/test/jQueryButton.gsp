<html>
<head>
    <title>Simple JQuery Button</title>
	<meta name="layout" content="custom" />    	

</head> 
<body>
		
	<g:render template="menu"/>
	
	<table style="height: 70%">
		<tr>
			<th>Data</th>
		</tr>
		<tr>
			<td></td>
		</tr>
		
	</table>
	
	<div class="action-menu">
		<span class="action-btn">
			<img src="${resource(dir: 'images/icons/silk', file: 'cog.png')}" style="vertical-align: middle"/>							
			<img src="${resource(dir: 'images/icons/silk', file: 'bullet_arrow_down.png')}" style="vertical-align: middle"/>
		</span>
		<div class="actions">
			<div class="action-menu-item" class="actionButton">														
				<a href="javascript:void(0);">
					Link #1
				</a>
			</div>		
			<div class="action-menu-item">														
				<a href="javascript:void(0);" class="actionButton">
					Link #2
				</a>
			</div>		
			<div class="action-menu-item">														
				<a href="javascript:void(0);" class="actionButton">
					Link #3
				</a>
			</div>		
			<div class="action-menu-item">														
				<a href="javascript:void(0);" class="actionButton">
					Link #4
				</a>
			</div>		
		</div>
	</div>	
</body> 
</html>