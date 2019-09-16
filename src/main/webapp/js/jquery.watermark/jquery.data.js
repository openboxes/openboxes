// December 3, 2010
// The following code has been pulled out of the Watermark plugin because
// of conflicts with jQuery UI, which also defines a [less-sophisticated]
// :data() extension.
// 
// I have inserted an exact duplicate of the jQuery UI :data() extension
// into the Watermark plugin, so there shouldn't be any conflicts.


// Extends jQuery with a custom selector - ":data(...)"
// :data(<name>)  Includes elements that have a specific name defined in the jQuery data collection. (Only the existence of the name is checked; the value is ignored.)
// :data(<name>=<value>)  Includes elements that have a specific jQuery data name defined, with a specific value associated with it.
// :data(<name>!=<value>)  Includes elements that have a specific jQuery data name defined, with a value that is not equal to the value specified.
// :data(<name>^=<value>)  Includes elements that have a specific jQuery data name defined, with a value that starts with the value specified.
// :data(<name>$=<value>)  Includes elements that have a specific jQuery data name defined, with a value that ends with the value specified.
// :data(<name>*=<value>)  Includes elements that have a specific jQuery data name defined, with a value that contains the value specified.
$.extend($.expr[":"], {
	"search": function (elem) {
		return "search" === (elem.type || "");
	},
	
	"data": function (element, index, matches, set) {
		var data, parts = /^((?:[^=!^$*]|[!^$*](?!=))+)(?:([!^$*]?=)(.*))?$/.exec(matches[3]);

		if (parts) {
			data = $.data(element, parts[1]);
			
			if (data !== undefined) {

				if (parts[2]) {
					data = "" + data;
				
					switch (parts[2]) {
						case "=":
							return (data == parts[3]);
						case "!=":
							return (data != parts[3]);
						case "^=":
							return (data.slice(0, parts[3].length) == parts[3]);
						case "$=":
							return (data.slice(-parts[3].length) == parts[3]);
						case "*=":
							return (data.indexOf(parts[3]) !== -1);
					}
				}

				return true;
			}
		}
		
		return false;
	}
});

