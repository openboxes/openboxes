<div style="padding: 0px; text-align: center;">
	<div id="placeholder"
		style="width: 400px; height: 300px; text-align: center;"></div>
</div>
<script type="text/javascript">
$(function () {
    var data = [];
    var data = [[1167692400000,61.05], [1167778800000,58.32], [1167865200000,57.35]];
    var options = {
		series: {
			lines: { show: true },
			points: { show: true }
		},
        xaxes: [ { mode: 'time' } ],
        yaxes: [ { min: 0 } ],
        legend: { position: 'sw', show: false }		
	};
    
    $.plot($("#placeholder"), [{ data: data, label: "Consumption" }], options);
    
    
    
});
</script>
