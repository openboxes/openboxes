<script type="text/javascript">
    var childCount = ${productInstance?.categories.size()} + 0;
 
	function addCategory(){
      var clone = $("#category_clone").clone();
      var htmlId = 'categoriesList['+childCount+'].';
      var categoryInput = clone.find("input[id$=number]");
 
      clone.find("input[id$=id]")
             .attr('id',htmlId + 'id')
             .attr('name',htmlId + 'id');
      clone.find("input[id$=deleted]")
              .attr('id',htmlId + 'deleted')
              .attr('name',htmlId + 'deleted');
      clone.find("input[id$=new]")
              .attr('id',htmlId + 'new')
              .attr('name',htmlId + 'new')
              .attr('value', 'true');
      categoryInput.attr('id',htmlId + 'number')
              .attr('name',htmlId + 'number');
      clone.find("select[id$=type]")
              .attr('id',htmlId + 'type')
              .attr('name',htmlId + 'type');
      clone.find("select[id$=id]")
	      .attr('id',htmlId + 'id')
	      .attr('name',htmlId + 'id');

            
      clone.attr('id', 'category'+childCount);
      $("#childList").append(clone);
      clone.show();
      categoryInput.focus();
      childCount++;
    }
 
    //bind click event on delete buttons using jquery live
    $('.del-category').live('click', function() {
        //find the parent div
        var prnt = $(this).parents(".category-selector");
        //find the deleted hidden input
        var delInput = prnt.find("input[id$=deleted]");
        //check if this is still not persisted
        var newValue = prnt.find("input[id$=new]").attr('value');
        //if it is new then i can safely remove from dom
        if(newValue == 'true'){
            prnt.remove();
        }else{
            //set the deletedFlag to true
            delInput.attr('value','true');
            //hide the div
            prnt.hide();
        }
    });
 
</script>
 
<div id="childList">
    <g:each var="category" in="${productInstance.categories}" status="i">
        <g:render template='category' model="['category':category,'i':i,'hidden':false]"/>
    </g:each>
</div>
<div style="padding: 5px;">
	<img src="${createLinkTo(dir:'images/icons/silk',file:'add.png')}" />
	<a href="#" onclick="addCategory();" class="middle"><warehouse:message code="product.addAnotherCategory.label"/></a>
</div>