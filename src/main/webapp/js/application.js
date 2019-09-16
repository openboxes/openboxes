var Ajax;
if (Ajax && (Ajax != null)) {
	Ajax.Responders.register({
	  onCreate: function() {
        if($('spinner') && Ajax.activeRequestCount>0)
          Effect.Appear('spinner',{duration:0.5,queue:'end'});
	  },
	  onComplete: function() {
        if($('spinner') && Ajax.activeRequestCount==0)
          Effect.Fade('spinner',{duration:0.5,queue:'end'});
	  }
	});
}


function selectCombo(comboBoxElem, value) {
	if (comboBoxElem != null) {
		if (comboBoxElem.options) { 
			for (var i = 0; i < comboBoxElem.options.length; i++) {
	        	if (comboBoxElem.options[i].value == value &&
	                comboBoxElem.options[i].value != "") { //empty string is for "noSelection handling as "" == 0 in js
	                comboBoxElem.options[i].selected = true;
	                break
	        	}
			}
		}
	}
}			


