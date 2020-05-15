<div class="box">
    <h2>
        <img src="${createLinkTo(dir:'images/icons/silk',file:'newspaper.png')}" />
        <warehouse:message code="dashboard.newSummary.label" args="[session.warehouse.name]"/>
    </h2>
    <div class="widget-content" style="padding: 0; margin: 0; overflow: auto; max-height: 200px; max-width: 600px">
        <table id="new-summary-rss" class="zebra">
        </table>
    </div>
</div>
<script>

  $(function () {

    $('.thumbnail').livequery(function() {
      $(this)
      .nailthumb({
        width: 32,
        height: 32
      });
    });

    $("#new-summary-rss")
    .rss("${grailsApplication.config.openboxes.dashboard.newsSummary.rssUrl}", {
      limit: "${grailsApplication.config.openboxes.dashboard.newsSummary.limit?:0}",
      layoutTemplate: "{entries}",
      entryTemplate: `<tr class=\"prop\">
            <td><img src=\"{teaserImageUrlOrDefault}\" class=\"thumbnail\"/></td>
            <td class=\"top\">
              <a href=\"{url}\">{title}</a>
              <span class=\"fade\">&bull;</span>
              {shortBodyPlain}
            </td>
            <td><p class=\"nowrap\" title=\"{date}\">{relativeDate}</p></td>
          </tr>`,
      tokens: {
        "relativeDate": function(entry, tokens) {
          console.log(entry);
          console.log(tokens)
          return moment(entry.publishedDate).fromNow();
        },
        "teaserImageUrlOrDefault": function(entry, tokens) {
          if (!tokens.teaserImageUrl) {
            return "${createLinkTo(dir:'images/icons/silk',file:'newspaper.png')}"
          }
          else {
            return tokens.teaserImageUrl
          }
        }
      },
      dateFormatFunction: function (date) {
        try {
          return new Date(date).toLocaleString()
        } catch (err) {
          return date
        }
      }
    });

  });
</script>
