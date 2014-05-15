<div style="padding: 0px; text-align: center;">
	<div id="placeholder"
		style="width: 400px; height: 300px; text-align: center;"></div>
</div>
<%--
<script language="javascript" src="${createLinkTo(dir:'js/jquery.flot/', file:'jquery.flot.min.js')}" type="text/javascript" ></script>
<script language="javascript" type="text/javascript" src="${createLinkTo(dir:'js/jquery.flot/', file:'jquery.flot.pie.min.js')}"></script>
--%>
<script type="text/javascript">
$(function () {
    var data = [];
    //for (var i = 0; i < 14; i += 0.5)
    //    data.push([new Date(), Math.sin(i)]);
    
    //var data = {
    //    "label": "Europe (EU27)",
    //    "data": [[1999, 3.0], [2000, 3.9], [2001, 2.0], [2002, 1.2], [2003, 1.3], [2004, 2.5], [2005, 2.0], [2006, 3.1], [2007, 2.9], [2008, 0.9]]
    //};
    //$.plot($("#placeholder"), [ data ]);

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
