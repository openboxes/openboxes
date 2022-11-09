<g:form method="GET" controller="dashboard" action="globalSearch">
    <div id="global-search-static" class="global-search w-100 my-2">
        <i class="ri-search-line"></i>
        <g:textField
            id="global-search-static-input"
            class="global-search-input"
            name="searchTerms"
            type="text"
            placeholder="${warehouse.message(code: 'globalSearch.placeholder.label')}"
            value="${attrs.value}"/>
        <div id="global-search-static-loading" class="dot-flashing-wrapper global-search-loading d-none">
            <div class="dot-flashing"></div>
        </div>
        <span class="ri-close-line" id="global-search-close"></span>
    </div>
</g:form>
<script>
  $(document)
    .ready(function () {
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

      $("#global-search-static-input")
        .autocomplete({
          delay: ${grailsApplication.config.openboxes.typeahead.delay},
          minLength: ${grailsApplication.config.openboxes.typeahead.minLength},
          position: { of: "#global-search-static" },
          source: function (req, resp) {
            $("#global-search-static-loading").get(0).classList.remove('d-none')
            $.getJSON('${attrs.jsonUrl}', req, function (data) {
              var suggestions = [];
              $.each(data, function (i, item) {
                suggestions.push(item);
              });
              resp(suggestions);
              $("#global-search-static-loading").get(0).classList.add('d-none');
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
        const { before, matched, after } = splitMatchingStr(item.label, $("#global-search-static-input").val());
        var link = $("<a></a>").css("color", item.color);

        if (before) link.append("<span>" + before + "</span>");
        if (matched) link.append("<strong class='font-weight-bold'>" + matched + "</strong>");
        if (after) link.append("<span>" + after + "</span>");

        ul.addClass("global-search-results")
          .addClass("global-search-static-results")
          .addClass("scrollbar");
        return $("<li></li>")
          .data("item.autocomplete", item)
          .append(link)
          .appendTo(ul);
      };
    });
</script>
