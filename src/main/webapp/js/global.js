function stripe(){
	jQuery('.stripe tbody tr:nth-child(even)').addClass("even"); 
	jQuery('.stripe tbody tr:nth-child(odd)').addClass("odd"); 
}

var alternateRowColors = function($table) {
	$('tbody tr:odd', $table).removeClass('even').addClass('odd');
	$('tbody tr:even', $table).removeClass('odd').addClass('even');
};	

var renameRowFields = function(table) { 
	var i = -1;  // Need to start at -1 because otherwise the initial index is 1 (not sure why).
	// Iterate over each row and change the name attribute of all input/select fields
	table.find("tr").each(function() { 
		$(this).find("input,select").each(function() {
		    var oldName = $(this).attr('name');
		    var newName = oldName.replace(/(transactionEntries\[)(\d+)(\])/, function(f, p1, p2, p3) {
		        return p1 + i + p3;
		    });
	        $(this).attr('name', newName);
	        $(this).attr('id', newName);
			//console.log(oldName + " -> " + newName);
		});
		i++;
	});
}

$.fn.quantity = function(){ 
	alert("test");
    return this.each(function(){ 
        $(this).text( $(this).text().replace(/(\d)(?=(\d\d\d)+(?!\d))/g, "$1,") ); 
    })
}



