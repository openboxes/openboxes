<style type="text/css">
	body { font-size:62.5%; }
	#menuLog { font-size:1.4em; margin:10px 20px 20px; }
	.hidden { position:absolute; top:0; left:-9999px; width:1px; height:1px; overflow:hidden; }
	
	.fg-button { clear:left; margin:0 4px 40px 20px; padding: .4em 1em; text-decoration:none !important; cursor:pointer; position: relative; text-align: center; zoom: 1; }
	.fg-button .ui-icon { position: absolute; top: 50%; margin-top: -8px; left: 50%; margin-left: -8px; }
	a.fg-button { float:left;  }
	button.fg-button { width:auto; overflow:visible; } /* removes extra button width in IE */
	
	.fg-button-icon-left { padding-left: 2.1em; }
	.fg-button-icon-right { padding-right: 2.1em; }
	.fg-button-icon-left .ui-icon { right: auto; left: .2em; margin-left: 0; }
	.fg-button-icon-right .ui-icon { left: auto; right: .2em; margin-left: 0; }
	.fg-button-icon-solo { display:block; width:8px; text-indent: -9999px; }	 /* solo icon buttons must have block properties for the text-indent to work */	
	
	.fg-button.ui-state-loading .ui-icon { background: url(spinner_bar.gif) no-repeat 0 0; }
</style>

<script type="text/javascript">    
    $(function(){
    	// BUTTONS
    	$('.fg-button').hover(
    		function(){ $(this).removeClass('ui-state-default').addClass('ui-state-focus'); },
    		function(){ $(this).removeClass('ui-state-focus').addClass('ui-state-default'); }
    	);
		$('#flyout').menu({ content: $('#flyout').next().html(), flyOut: true });
    	
    	// MENUS    	
		//$('#hierarchy').menu({
		//	content: $('#hierarchy').next().html(),
		//	crumbDefaultText: ' ',
		//	flyout: true
		//});
		
		//$('#hierarchybreadcrumb').menu({
		//	content: $('#hierarchybreadcrumb').next().html(),
		//	backLink: false
		//});
    });
</script>
<!-- <p id="menuLog">You chose: <span id="menuSelection"></span></p>-->
<a tabindex="0" href="#menu-items1" class="fg-button fg-button-icon-right ui-widget ui-state-default ui-corner-all" id="flyout">
<span class="ui-icon ui-icon-triangle-1-s"></span>choose</a>
<div id="menu-items1" class="hidden">
	<ul>
		<g:render template="../category/menuTreeOptions" model="[root:root, selected:selected, level: 0]"/>
	</ul>
</div>


<%-- 
<a tabindex="0" href="#menu-items1" class="fg-button fg-button-icon-right ui-widget ui-state-default ui-corner-all" id="hierarchy"><span class="ui-icon ui-icon-triangle-1-s"></span>choose category</a>
<div id="menu-items1" class="hidden">
	<ul>
		<g:render template="../category/menuTreeOptions" model="[root:root, selected:selected, level: 0]"/>
	</ul>
</div>

<a tabindex="0" href="#menu-items2" class="fg-button fg-button-icon-right ui-widget ui-state-default ui-corner-all" id="hierarchybreadcrumb"><span class="ui-icon ui-icon-triangle-1-s"></span>choose category</a>
<div id="menu-items2" class="hidden">
	<ul>
		<g:render template="../category/menuTreeOptions" model="[root:root, selected:selected, level: 0]"/>
	</ul>
</div>
--%>
