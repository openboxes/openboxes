<input type="text" id="${attrs.id }" name="${attrs.name }" value="${attrs.value }" class="medium text" />									
<ul id="mcdropdown" class="mcdropdown_menu">									
	<g:selectCategory_v2  value="${attrs.value }"/>
</ul>
<script>
$(document).ready(function() {			
	$("#${attrs.id}").mcDropdown("#mcdropdown", {allowParentSelect:true}); 
});
</script>
