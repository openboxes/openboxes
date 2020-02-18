import { colorLineChart, colorBarChart, colorHorizontalBarChart,doughnutChart } from './chartColors'

function loadColors (data, chart) {
    var datasets = data['datasets'][0];
    
    if(chart == "line") {
        datasets['borderColor'] = colorLineChart['borderColor'];
        datasets['pointBorderColor'] = colorLineChart['pointBorderColor'];
        datasets['pointBorderWidth'] = colorLineChart['pointBorderWidth'];
        datasets['pointBackgroundColor'] = colorLineChart['pointBackgroundColor'];
        datasets['pointHoverBackgroundColor'] = colorLineChart['pointHoverBackgroundColor'];
        datasets['pointHoverBorderColor'] = colorLineChart['pointHoverBorderColor'];
        datasets['pointHoverBorderWidth'] = colorLineChart['pointHoverBorderWidth'];
        datasets['lineTension'] = colorLineChart['lineTension'];
    }
    
    else if(chart == "horizontalBar")  {
        datasets['backgroundColor'] = colorHorizontalBarChart['backgroundColor'];
    }
    

    else if(chart == "bar") {
        datasets['backgroundColor'] = colorBarChart['backgroundColor'];
    }

    else if(chart=="doughnut") {
        datasets['backgroundColor'] = doughnutChart['backgroundColor'];
    }
    

    return datasets;
    } 
    
    export { loadColors } ; 