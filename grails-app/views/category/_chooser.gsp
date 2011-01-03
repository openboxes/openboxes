
<input type="hidden" id=categoryid" name="category.id">
<span id="categoryname" name="category.name" style="padding: 0px;"> <!-- enter value --> </span>
<a href="#" id="showCategories" style="padding-left: 10px">choose</a>
<script>
	$(document).ready(function() {
		$(".selectableCategory").click(function() {
			$('#categoryid').val(this.id);
			$('#categoryname').html(this.name);
			$('#categories').hide();
	 	});
	 	$("#showCategories").click(function() {
 	 	$('#categories').show();
	});
	});                               	 		
</script>
<style>
.parentCategory {
	list-style: square;
	padding-left: 25px;
}

.childCategory {
	list-style: square;
	padding-left: 25px;
}
</style>
<div id="categories"
	style="background-color: #fafafa; display: none; overflow: auto; height: 200px; width: 500px;">
	<g:menu rootNode="${productInstance?.rootCategory}" /></div>