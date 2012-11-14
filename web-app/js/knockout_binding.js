 ko.bindingHandlers.date_picker = {
      init: function (element, params) {
        var item =  $(element);
        var minDate= item.attr("min-date");
        var maxDate = item.attr("max-date");
        var date =  $.datepicker.parseDate('mm/dd/yy', item.prev().val())
        item.val($.datepicker.formatDate('dd/M/yy', date));
        item.datepicker({
          minDate: minDate && new Date(minDate),
          maxDate: maxDate && new Date(maxDate), 
          dateFormat:'dd/M/yy',
          buttonImageOnly: true
        });
        item.change(function(event) {
          var picker = $(this);
          try {
            var d = $.datepicker.parseDate('dd/M/yy', picker.val());
            picker.prev().val($.datepicker.formatDate('mm/dd/yy', d));
          } catch(err) {
            picker.val("");
            picker.prev().val("");
          }
          picker.prev().trigger("change");
        });
      }
    };


ko.bindingHandlers.search_product = {
  init: function (element, params) {
      $(element).autocomplete({
          delay: 300,
          minLength: 3,
          dataType: 'json',
          source: function(req, add){
            $.getJSON(params().source, { term: req.term}, function(data) {
              var items = [];
              $.each(data, function(i, item) {
                items.push(item);
              });
              add(items);
            });
          },
          focus: function(event, ui) {
            return false;
          },
          change: function(event, ui) {
            if (!ui.item) {
              $(this).prev().val("");  
              $(this).val("");			
            }
            $(this).prev().trigger("change");
            return false;
          },
          select: function(event, ui) {
            if (ui.item) {
              $(this).prev().val(ui.item.value);
              $(this).val(ui.item.label);
            }
            $(this).prev().trigger("change");
            return false;
          }
      })
      .data("autocomplete" )._renderItem = function( ul, item ) {
            var li = $("<li>").data("item.autocomplete", item );
            if(item.type == 'Product'){
                var text = item.quantity == null ? item.label : item.label + " QTY: " + item.quantity;
                li.append("<a>" + text + "</a>" );
            }else{
                li.append("<span class='product-group'>" + item.label + "</span>" );
            }
            li.appendTo(ul);
            return li;
      };
  }
};

ko.bindingHandlers.autocomplete = {
  init: function (element, params) {
          $(element).autocomplete({
            delay: 300,
            dataType: 'json',
            source: function(req, add){
              $.getJSON(params().source, { term: req.term}, function(data) {
                var items = [];
                $.each(data, function(i, item) {
                  items.push(item);
                });
                add(items);
              });
            },
            select: function(event, ui){
              $(this).val(ui.item.value);
              $(this).trigger("selected");
              return false;
            }
          });

        }
};


