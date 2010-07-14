<html>
  <head>
    <title>Simple JQuery Tabs example</title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="jquery" />
	<title>JQuery Tabs</title>        

	
	<!-- tab pane styling -->
	<style>
			
		/* tab pane styling */
		.panes div {
			display:none;		
			padding:15px 10px;
			border:1px solid #999;
			border-top:0;
			height:100px;
			font-size:14px;
			background-color:#fff;
		}

	</style>
	


</head> 
<body>
	<div>
		<p>Between <input type="text" id="datepicker"> </p> 
	</div>

	<div class="body">
	<!-- the tabs -->
	<ul class="tabs">
		<li><a href="#">Tab 1</a></li>
		<li><a href="#">Tab 2</a></li>
		<li><a href="#">Tab 3</a></li>
	</ul>
	
	<!-- tab "panes" -->
	<div class="panes">
		<div>First tab content. Tab contents are called "panes"</div>
		<div>Second tab content</div>
		<div>Third tab content</div>
	</div>
	



	<script type="text/javascript"> 
		$(document).ready(function() { 
			//$("#datepicker").datepicker({dateFormat: 'yy/mm/dd'}); 
		
			// setup ul.tabs to work as tabs for each div directly under div.panes
			$("ul.tabs").tabs("div.panes > div");

		});		
	</script>		
		
	</div>
</body> 
</html>