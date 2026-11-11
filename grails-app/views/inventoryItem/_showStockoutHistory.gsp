<%-- SPIKE (OBPIH-7375): prototype visualizations of a product's stockout history at the current location --%>
<style>
    #stockout-history { --out: #d9534f; --low: #f0ad4e; --in: #5cb85c; --none: #e8e8e8; }
    #stockout-history .so-body { padding: 12px 16px 16px 16px; }
    #stockout-history .so-section { margin: 0 0 16px 0; }
    #stockout-history .so-periods { display: flex; align-items: center; flex-wrap: wrap; gap: 6px; }
    #stockout-history .so-periods .so-note { margin: 0 0 0 8px; }
    #stockout-history h3 { margin: 0 0 6px 0; font-size: 13px; }
    #stockout-history .so-note { color: #777; font-size: 11px; margin-bottom: 6px; }
    #stockout-history .so-periods a { padding: 3px 10px; border: 1px solid #ccc; border-radius: 12px; text-decoration: none; color: #555; font-size: 12px; }
    #stockout-history .so-periods a.active { background: #555; border-color: #555; color: #fff; }
    #stockout-history .so-tiles { display: flex; flex-wrap: wrap; gap: 8px; }
    #stockout-history .so-tile { flex: 1 1 120px; border: 1px solid #ddd; border-radius: 4px; padding: 8px 12px; background: #fff; }
    #stockout-history .so-tile .so-value { font-size: 20px; font-weight: bold; }
    #stockout-history .so-tile .so-label { color: #666; font-size: 11px; }
    #stockout-history .so-tile.so-alert .so-value { color: var(--out); }
    #stockout-history .so-strip { display: flex; height: 18px; border: 1px solid #ddd; }
    #stockout-history .so-strip div { flex: 1 1 0; }
    #stockout-history .so-axis { display: flex; justify-content: space-between; color: #777; font-size: 11px; }
    #stockout-history .outOfStock { background: var(--out); }
    #stockout-history .low { background: var(--low); }
    #stockout-history .inStock { background: var(--in); }
    #stockout-history .noData { background: var(--none); }
    #stockout-history .so-legend span { display: inline-block; margin-right: 12px; font-size: 11px; color: #555; }
    #stockout-history .so-legend i { display: inline-block; width: 10px; height: 10px; margin-right: 4px; vertical-align: middle; }
    #stockout-history #so-chart { width: 100%; height: 300px; }
    #stockout-history .so-chart-legend table { width: auto; border: 0; margin-bottom: 4px; }
    #stockout-history .so-chart-legend td { padding: 0 6px 0 2px; border: 0; font-size: 11px; background: transparent; }
    #stockout-history .so-subtabs { border-bottom: 1px solid #ccc; margin: 4px 0 14px 0; padding: 0; }
    #stockout-history .so-subtabs a { display: inline-block; padding: 6px 12px; margin-bottom: -1px; border: 1px solid transparent; border-radius: 4px 4px 0 0; text-decoration: none; color: #555; }
    #stockout-history .so-subtabs a.active { border-color: #ccc #ccc #fff; background: #fff; color: #333; font-weight: bold; }
    #stockout-history .so-pane { display: none; }
    #stockout-history .so-pane.active { display: block; }
    #stockout-history .so-calendar { display: grid; grid-template-rows: repeat(7, 11px); grid-auto-flow: column; grid-auto-columns: 11px; gap: 2px; overflow-x: auto; padding-bottom: 4px; }
    #stockout-history .so-calendar div { border-radius: 2px; }
    #stockout-history .so-calendar .so-blank { background: transparent; }
    #stockout-history .so-bars { display: flex; align-items: flex-end; gap: 4px; height: 120px; border-bottom: 1px solid #ccc; }
    #stockout-history .so-bar { flex: 1 1 0; display: flex; flex-direction: column; justify-content: flex-end; align-items: center; height: 100%; }
    #stockout-history .so-bar div.so-fill { width: 100%; background: var(--out); min-height: 1px; }
    #stockout-history .so-bar span { font-size: 10px; color: #555; }
    #stockout-history .so-bar-labels { display: flex; gap: 4px; }
    #stockout-history .so-bar-labels span { flex: 1 1 0; text-align: center; font-size: 10px; color: #777; }
</style>

<div id="stockout-history" class="box">
    <h2>
        <g:message code="stockCard.stockoutHistory.label" default="Stockout History"/>
        <small style="color:#999">(prototype)</small>
    </h2>
    <div class="so-body">

    <div class="so-section so-periods">
        <g:each in="${[90, 180, 365, 730]}" var="period">
            <a href="#" data-days="${period}" class="${period == days ? 'active' : ''}">Last ${period} days</a>
        </g:each>
        <span class="so-note">
            <g:formatDate date="${history.startDate}" format="dd MMM yyyy"/> to <g:formatDate date="${history.endDate}" format="dd MMM yyyy"/>,
            calculated from transactions
        </span>
    </div>

    <g:set var="summary" value="${history.summary}"/>
    <div class="so-section">
        <g:if test="${!summary.daysWithData}">
            <div class="so-note">No transactions for this product at this location before the end of the period.</div>
        </g:if>
        <div class="so-tiles">
            <div class="so-tile ${summary.outOfStockNow ? 'so-alert' : ''}">
                <div class="so-value">${summary.outOfStockNow ? 'Out of stock' : (summary.quantityOnHand != null ? 'In stock' : '–')}</div>
                <div class="so-label">
                    <g:if test="${summary.outOfStockNow}">since <g:formatDate date="${summary.outOfStockSince}" format="dd MMM yyyy"/></g:if>
                    <g:elseif test="${summary.daysSinceLastStockout != null}">${summary.daysSinceLastStockout} days since last stockout</g:elseif>
                    <g:else>no stockouts in period</g:else>
                </div>
            </div>
            <div class="so-tile ${summary.stockoutDays ? 'so-alert' : ''}">
                <div class="so-value">${summary.stockoutDays}</div>
                <div class="so-label">stockout days</div>
            </div>
            <div class="so-tile">
                <div class="so-value">${summary.availability != null ? summary.availability + '%' : '–'}</div>
                <div class="so-label">of days in stock</div>
            </div>
            <div class="so-tile">
                <div class="so-value">${summary.episodes}</div>
                <div class="so-label">stockouts</div>
            </div>
            <div class="so-tile">
                <div class="so-value">${summary.longestEpisode} <small>days</small></div>
                <div class="so-label">longest stockout</div>
            </div>
            <div class="so-tile">
                <div class="so-value">${summary.averageDaysToRestock != null ? summary.averageDaysToRestock : '–'} <small>days</small></div>
                <div class="so-label">average time to restock</div>
            </div>
            <div class="so-tile">
                <div class="so-value">${summary.lowStockDays}</div>
                <div class="so-label">days below minimum${history.inventoryLevel.min ? '' : ' (no minimum set)'}</div>
            </div>
        </div>
    </div>

    <div class="so-subtabs">
        <a href="#" data-pane="chart" class="active">Stock level</a>
        <a href="#" data-pane="strip">Strip</a>
        <a href="#" data-pane="calendar">Calendar</a>
        <a href="#" data-pane="months">By month</a>
        <a href="#" data-pane="episodes">Stockouts (${history.episodes.size()})</a>
    </div>

    <div class="so-section so-pane" data-pane="strip">
        <h3>In stock / out of stock by day</h3>
        <div class="so-strip" id="so-strip"></div>
        <div class="so-axis"><span><g:formatDate date="${history.startDate}" format="dd MMM yyyy"/></span><span>Today</span></div>
        <div class="so-legend">
            <span><i class="inStock"></i>In stock</span>
            <span><i class="low"></i>Below minimum</span>
            <span><i class="outOfStock"></i>Out of stock</span>
            <span><i class="noData"></i>No transactions yet</span>
        </div>
    </div>

    <div class="so-section so-pane active" data-pane="chart">
        <h3>Quantity on hand at the end of each day</h3>
        <div class="so-note">Red bands are stockouts. Green points are days with stock received. Horizontal lines are the minimum, reorder and maximum levels.</div>
        <div id="so-chart-legend" class="so-chart-legend"></div>
        <div id="so-chart"></div>
    </div>

    <div class="so-section so-pane" data-pane="calendar">
        <h3>Calendar</h3>
        <div class="so-note">One square per day, a week per column, Sunday at the top.</div>
        <div class="so-calendar" id="so-calendar"></div>
        <div class="so-legend">
            <span><i class="inStock"></i>In stock</span>
            <span><i class="low"></i>Below minimum</span>
            <span><i class="outOfStock"></i>Out of stock</span>
            <span><i class="noData"></i>No transactions yet</span>
        </div>
    </div>

    <div class="so-section so-pane" data-pane="months">
        <h3>Stockout days per month</h3>
        <div class="so-bars" id="so-bars"></div>
        <div class="so-bar-labels" id="so-bar-labels"></div>
    </div>

    <div class="so-section so-pane" data-pane="episodes">
        <h3>Stockouts</h3>
        <table>
            <thead>
            <tr class="odd">
                <th>Out of stock from</th>
                <th>Back in stock</th>
                <th>Days</th>
                <th>Ran out with</th>
                <th>Restocked by</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${history.episodes}" var="episode" status="i">
                <tr class="${i % 2 ? 'odd' : 'even'}">
                    <td>
                        <g:if test="${episode.onOrBefore}">On or before </g:if>
                        <g:formatDate date="${episode.start}" format="dd MMM yyyy"/>
                    </td>
                    <td>
                        <g:if test="${episode.ongoing}"><span style="color: var(--out)">Still out of stock</span></g:if>
                        <g:else><g:formatDate date="${episode.end + 1}" format="dd MMM yyyy"/></g:else>
                    </td>
                    <td>${episode.days}${episode.onOrBefore || episode.ongoing ? '+' : ''}</td>
                    <td>
                        <g:if test="${episode.startTransaction}">
                            <g:link controller="inventory" action="showTransaction" id="${episode.startTransaction.id}">
                                ${episode.startTransaction.number ?: episode.startTransaction.type}
                            </g:link>
                            ${episode.startTransaction.type} (${episode.startTransaction.quantityChange})
                        </g:if>
                        <g:else>–</g:else>
                    </td>
                    <td>
                        <g:if test="${episode.endTransaction}">
                            <g:link controller="inventory" action="showTransaction" id="${episode.endTransaction.id}">
                                ${episode.endTransaction.number ?: episode.endTransaction.type}
                            </g:link>
                            ${episode.endTransaction.type} (+${episode.endTransaction.quantityChange})
                        </g:if>
                        <g:else>–</g:else>
                    </td>
                </tr>
            </g:each>
            <g:unless test="${history.episodes}">
                <tr><td colspan="5" class="so-note">No stockouts in this period.</td></tr>
            </g:unless>
            </tbody>
        </table>
    </div>
    </div>
</div>

<script src="${resource(dir:'js/flot/', file:'jquery.flot.js')}" type="text/javascript"></script>
<script src="${resource(dir:'js/flot/', file:'jquery.flot.time.js')}" type="text/javascript"></script>
<script src="${resource(dir:'js/flot/', file:'jquery.flot.resize.js')}" type="text/javascript"></script>
<script type="text/javascript">
  $(function () {
    var data = ${raw(chartJson)};
    var root = $('#stockout-history');
    var DAY = 24 * 60 * 60 * 1000;
    var toMs = function (s) { var p = s.split('-'); return Date.UTC(+p[0], +p[1] - 1, +p[2]); };
    var title = function (d) { return d[0] + ': ' + (d[1] == null ? 'no transactions yet' : d[1] + ' on hand'); };

    // Change the period by reloading the tab panel
    root.find('.so-periods a').on('click', function (e) {
      e.preventDefault();
      root.parent().load('${request.contextPath}/inventoryItem/showStockoutHistory/${product.id}?days=' + $(this).data('days'));
    });

    // Strip: one cell per day
    var strip = $('#so-strip');
    data.days.forEach(function (d) {
      $('<div/>').addClass(d[4]).attr('title', title(d)).appendTo(strip);
    });

    // Calendar: weeks as columns, Sunday at the top
    var calendar = $('#so-calendar');
    var firstDay = new Date(toMs(data.days[0][0])).getUTCDay();
    for (var i = 0; i < firstDay; i++) { $('<div class="so-blank"/>').appendTo(calendar); }
    data.days.forEach(function (d) {
      $('<div/>').addClass(d[4]).attr('title', title(d)).appendTo(calendar);
    });

    // Monthly bars
    var maxDays = 31;
    data.months.forEach(function (m) {
      var bar = $('<div class="so-bar"/>').attr('title', m.month + ': ' + m.stockoutDays + ' of ' + m.daysWithData + ' days out of stock');
      $('<span/>').text(m.stockoutDays || '').appendTo(bar);
      $('<div class="so-fill"/>').css('height', (100 * m.stockoutDays / maxDays) + '%')
        .css('background', m.stockoutDays ? '' : 'transparent').appendTo(bar);
      bar.appendTo('#so-bars');
      $('<span/>').text(m.month.substring(2)).appendTo('#so-bar-labels');
    });

    // Step chart of quantity on hand
    var balances = data.days.map(function (d) { return [toMs(d[0]), d[1]]; });
    var receipts = data.days.filter(function (d) { return d[2] > 0 && d[1] != null; })
      .map(function (d) { return [toMs(d[0]), d[1]]; });
    var markings = data.episodes.map(function (e) {
      return { xaxis: { from: toMs(e[0]), to: toMs(e[1]) + DAY }, color: 'rgba(217, 83, 79, 0.2)' };
    });
    var levels = [['min', '#d9534f'], ['reorder', '#f0ad4e'], ['max', '#5bc0de']];
    var levelSeries = [];
    levels.forEach(function (l) {
      var value = data.inventoryLevel[l[0]];
      if (value) {
        levelSeries.push({
          data: [[balances[0][0], value], [balances[balances.length - 1][0], value]],
          color: l[1], label: l[0] + ' (' + value + ')', lines: { show: true, lineWidth: 1 }, shadowSize: 0
        });
      }
    });
    var plotChart = function () { $.plot($('#so-chart'), [
      { data: balances, color: '#337ab7', label: 'Quantity on hand', lines: { show: true, steps: true, fill: 0.1 }, shadowSize: 0 },
      { data: receipts, color: '#5cb85c', label: 'Stock received', points: { show: true, radius: 3, fill: true, fillColor: '#5cb85c' }, lines: { show: false } }
    ].concat(levelSeries), {
      xaxis: { mode: 'time', timeformat: '%d %b %y' },
      yaxis: { min: 0 },
      grid: { markings: markings, borderWidth: 1, borderColor: '#ccc', hoverable: true },
      legend: { container: $('#so-chart-legend'), noColumns: 5 }
    }); };
    plotChart();

    // Sub-tabs
    root.find('.so-subtabs a').on('click', function (e) {
      e.preventDefault();
      var pane = $(this).data('pane');
      root.find('.so-subtabs a').removeClass('active');
      $(this).addClass('active');
      root.find('.so-pane').removeClass('active').filter('[data-pane="' + pane + '"]').addClass('active');
      if (pane === 'chart') { plotChart(); }
    });
  });
</script>
