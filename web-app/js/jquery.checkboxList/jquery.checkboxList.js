(function($) {

    var methods = {
        init: function() {

            var $ul = $("<ul/>").insertAfter(this);
            var $container = $ul.prev().andSelf().wrapAll("<div class='multiselect-to-checkboxes'></div>");
            var baseId = "_" + $(this).attr("id");
            var disabled = $(this).is(':disabled');
            console.log(disabled);
            $(this).children("option").each(function(index) {
                var $option = $(this);
                var id = baseId + index;
                var $li = $("<li/>").appendTo($ul);
                var $checkbox = $("<input type='checkbox' id='" + id + "'/>").appendTo($li).change(function() {
                    var $option = $(this).parents('.multiselect-to-checkboxes').find('select option').eq(index);
                    if ($(this).is(":checked")) {
                        $option.attr("selected", "selected");
                    } else {
                        $option.removeAttr("selected");
                    }
                    if ($(this).is(":disabled")) {
                        $option.attr("disabled", "disabled");
                    } else {
                        $option.removeProp("selected");
                    }

                });
                if ($option.is(":selected")) {
                    $checkbox.attr("checked", "checked");
                }
                if (disabled) {
                    $checkbox.attr("disabled", "disabled");
                }
                $checkbox.after("<label for='" + id + "'> " + $option.text() + "</label>");
            });
            $(this).hide();
        }
    };

    $.fn.multiSelectToCheckboxes = function(method) {
        if (methods[method]) {
            return methods[method].apply(this, Array.prototype.slice.call(arguments, 1));
        } else if (typeof method === 'object' || !method) {
            return methods.init.apply(this, arguments);
        } else {
            $.error('Method ' + method + ' does not exist on jQuery.multiSelectToCheckboxes');
        }

    };

})(jQuery);