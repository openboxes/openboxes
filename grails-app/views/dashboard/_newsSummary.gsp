<div class="box">
    <h2>
        <img src="${resource(dir:'images/icons/silk',file:'newspaper.png')}" />
        <warehouse:message code="dashboard.newSummary.label" args="[session.warehouse.name]"/>
    </h2>

    <div class="widget-content" style="padding: 0; margin: 0">
        <div id="news-summary">
            <table>
                <tbody>
                <g:each var="newsItem" in="${newsItems }" status="status">
                    <tr class="${status%2?'even':'odd' } prop">
                        <td class="center top" width="1%">
                            <div class="nailthumb-container">
                                <img src="${newsItem.icon}" class="middle"/>
                            </div>
                        </td>
                        <td class="middle">
                            ${newsItem.text}
                            <a href="${newsItem.uri}">${newsItem.label }</a>
                        </td>
                        <td class="nowrap middle">
                            <div class='fade'>${format.date(obj:new Date(),format:'MMM d hh:mma')}</div>
                        </td>
                    </tr>
                </g:each>
                <g:unless test="${newsItems }">
                    <tr class="">
                        <td class="center">
                            <span class="fade"><warehouse:message code="dashboard.noNewsItemsFound.message"/></span>
                        </td>
                    </tr>
                </g:unless>
                </tbody>
            </table>
        </div>
    </div>
</div>
<script>
	$(function() {
		$('.nailthumb-container img').nailthumb({width : 20, height : 20});
	});
</script>
