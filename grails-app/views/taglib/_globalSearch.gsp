<g:form method="GET" controller="dashboard" action="globalSearch">
    <div id="global-search" class="d-flex position-absolute global-search global-search--hidden">
        <i class="ri-search-line"></i>
        <g:textField
                id="global-search-input"
                class="global-search-input"
                name="searchTerms"
                type="text"
                placeholder="${warehouse.message(code: 'globalSearch.placeholder.label')}"
                value="${attrs.value}"/>
        <div id="global-search-loading" class="dot-flashing-wrapper global-search-loading d-none">
            <div class="dot-flashing"></div>
        </div>
        <span class="ri-close-line" id="global-search-close"></span>
    </div>
</g:form>
<script>
  $(document)
    .ready(function () {
      $("#${attrs.buttonId}")
        .click(function () {
          const globalSearchElement = $("#global-search").get(0);
          const hiddenClassName = 'global-search--hidden';
          if (globalSearchElement.classList.contains(hiddenClassName)) {
            globalSearchElement.classList.remove(hiddenClassName)
            $("#global-search-input").focus();
          } else {
            globalSearchElement.classList.add(hiddenClassName)
          }
        })

      const splitMatchingStr = (data, str) => {
        const indexOfMatched = data.indexOf(str);
        if (indexOfMatched < 0) {
          return { before: data };
        }
        const before = data.slice(0, indexOfMatched);
        const matched = data.slice(indexOfMatched, indexOfMatched + str.length);
        const after = data.slice(indexOfMatched + str.length, data.length);

        return { before, matched, after };
      };

      function closeInputHandler () {
        const hiddenClassName = 'global-search--hidden';
        $("#global-search").get(0).classList.add(hiddenClassName);
        $("#global-search-input").val('');
      }

      $("#global-search-close").click(closeInputHandler)

      $("#global-search-input")
        .blur(closeInputHandler)
        .autocomplete({
          delay: ${grailsApplication.config.openboxes.typeahead.delay},
          minLength: ${grailsApplication.config.openboxes.typeahead.minLength},
          position: { of: "#global-search" },
          source: function (req, resp) {
            $("#global-search-loading").get(0).classList.remove('d-none')
            $.getJSON('${attrs.jsonUrl}', req, function (data) {
              var suggestions = [];
              $.each(data, function (i, item) {
                suggestions.push(item);
              });
              resp(suggestions);
              $("#global-search-loading").get(0).classList.add('d-none');
            })
          },
          select: function (event, ui) {
            window.location = ui.item.url;
            return false;
          },
          focus: function (event, ui) {
            this.value = ui.item.label;
            event.preventDefault(); // Prevent the default focus behavior.
          }
        })
        .data("autocomplete")._renderItem = function (ul, item) {
        const { before, matched, after } = splitMatchingStr(item.label, $("#global-search-input").val());
        var link = $("<a></a>").css("color", item.color);
        // If we display translated name, we want to have tooltip with original name of the product
        if (item.displayName) {
          link.attr('title', item.value)
        }
        if (before) link.append("<span>" + before + "</span>");
        if (matched) link.append("<strong class='font-weight-bold'>" + matched + "</strong>");
        if (after) link.append("<span>" + after + "</span>");

        ul.addClass("global-search-results")
          .addClass("scrollbar");
        return $("<li></li>")
          .data("item.autocomplete", item)
          .append(link)
          .appendTo(ul);
      };
    });
</script>
