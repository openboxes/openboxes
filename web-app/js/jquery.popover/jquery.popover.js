(function($) {
$.fn.popover = function(options) {
  var defaults = {
    openEvent: null,
    closeEvent: null,
    offsetX: 0,
    offsetY: 0
  }
  var options = $.extend(defaults, options);

  // HTML floater box
  var header = $(options.header).detach();
  var content = $(options.content).detach();
  var floater = $('<div class="popover">'
        + '<div class="triangle"></div>'
        + '<div class="header"></div>'
        + '<div class="content"></div>'
        + '</div>').appendTo('body');
  $('.header', floater).append(header);
  $('.content', floater).append(content);

  // Document click closes active popover
  $.fn.popover.openedPopup = null;
  $(document).bind("click", function(event) {
    if ($.fn.popover.openedPopup != null
        && ($(event.target).parents(".popover").length === 0)
        && (!$(event.target).hasClass('popover-button'))) {
      $.fn.popover.openedPopup.trigger('hidePopover');
    }
  });

  var showPopover = function(button) {
    // Already opened?
    if ($.fn.popover.openedPopup === button) {
      $.fn.popover.openedPopup.trigger('hidePopover');
      return false;
    } else if($.fn.popover.openedPopup != null){
      $.fn.popover.openedPopup.trigger('hidePopover');
    }
    var triangle = $('.triangle', floater).click(function() {
        button.trigger('hidePopover') });

    // Set this first for the layout calculations to work.
    floater.css('display', 'block');

    // position and resize
    var leftOff = 0;
    var topOff = 0;
    var docWidth = $(document).width();
    var docHeight = $(document).height();
    var triangleSize = parseInt(triangle.css("border-bottom-width"));
    var contentWidth = floater.outerWidth();
    var contentHeight = floater.outerHeight();
    var buttonWidth = button.outerWidth();
    var buttonHeight = button.outerHeight()
    var offset = button.offset();

    // Calculate topOff
    topOff = offset.top + buttonHeight + triangleSize;
    var diffHeight = docHeight - (topOff + contentHeight + triangleSize);
    if (diffHeight < 0){
      //resize the floater
      floater.height(floater.height() + diffHeight);
    }

    // Padding against document borders
    var padding = 18;

    // Calculate leftOff
    leftOff = offset.left + (buttonWidth - contentWidth)/2;
    var diffWidth = 0;
    if (leftOff < padding) {
      // out of the document at left
      diffWidth = leftOff - padding;
    } else if (leftOff + contentWidth > docWidth) {
      // left of the screen right
      diffWidth = leftOff + contentWidth - docWidth + padding;
    }

    // position triangle
    triangle.css("left", contentWidth/2 - triangleSize + diffWidth);

    floater.offset({
      top: topOff + options.offsetY,
      left: leftOff - diffWidth + options.offsetX
    });
    floater.show();
    //Timeout for webkit transitions to take effect
    window.setTimeout(function() {
      floater.addClass("active");
      // Fixes some browser bugs
      $(window).resize();
    }, 0);
    if ($.isFunction(options.openEvent)) options.openEvent();
    $.fn.popover.openedPopup = button;
    button.addClass('popover-on');
    return false;
  }

  this.each(function(){
    var button = $(this);
    button.addClass("popover-button");
    button.bind('click', function() { showPopover(button) });
    button.bind('showPopover', function() { showPopover(button) });
    button.bind('hidePopover', function() {
      button.removeClass('popover-on');
      floater.removeClass("active").attr("style", "").css('display', 'none');
      if ($.isFunction(options.closeEvent)) {
        options.closeEvent();
      }
      $.fn.popover.openedPopup = null;
      window.setTimeout(function() {
        // Fixes some browser bugs
        $(window).resize();
      }, 0);
      return false;
    });
  });
}
})(jQuery);