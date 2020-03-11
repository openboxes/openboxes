import {
    colorLineChart,
    colorBarChart,
    colorHorizontalBarChart,
    doughnutChart,
    graphMultipleChart0,
    graphMultipleChart1,
    graphMultipleChart2,
    graphMultipleChart3,
} from './chartColors'

function loadColors(data, chart) {
    var datasets = data['datasets'][0];
    var datasets1 = data['datasets'][1];
    var datasets2 = data['datasets'][2];
    var datasets3 = data['datasets'][3];

    if (chart === 'line') {
        datasets['borderColor'] = colorLineChart['borderColor'];
        datasets['pointBorderColor'] = colorLineChart['pointBorderColor'];
        datasets['pointBorderWidth'] = colorLineChart['pointBorderWidth'];
        datasets['pointBackgroundColor'] = colorLineChart['pointBackgroundColor'];
        datasets['pointHoverBackgroundColor'] = colorLineChart['pointHoverBackgroundColor'];
        datasets['pointHoverBorderColor'] = colorLineChart['pointHoverBorderColor'];
        datasets['pointHoverBorderWidth'] = colorLineChart['pointHoverBorderWidth'];
        datasets['lineTension'] = colorLineChart['lineTension'];
    }

    if (chart == "bar") {
        if (datasets1 == null && datasets2 == null && datasets3 == null) {
            datasets['backgroundColor'] = "#444444"
        }
        else {
            datasets['borderColor'] = graphMultipleChart0['borderColor'];
            datasets['pointBorderColor'] = graphMultipleChart0['pointBorderColor'];
            datasets['pointBorderWidth'] = graphMultipleChart0['pointBorderWidth'];
            datasets['pointBackgroundColor'] = graphMultipleChart0['pointBackgroundColor'];
            datasets['pointHoverBackgroundColor'] = graphMultipleChart0['pointHoverBackgroundColor'];
            datasets['pointHoverBorderColor'] = graphMultipleChart0['pointHoverBorderColor'];
            datasets['pointHoverBorderWidth'] = graphMultipleChart0['pointHoverBorderWidth'];
            datasets['fill'] = false;

            datasets['backgroundColor'] = "#444444"
        }


        if (datasets1 != null) {
            datasets1['borderColor'] = graphMultipleChart1['borderColor'];
            datasets1['pointBorderColor'] = graphMultipleChart1['pointBorderColor'];
            datasets1['pointBorderWidth'] = graphMultipleChart1['pointBorderWidth'];
            datasets1['pointBackgroundColor'] = graphMultipleChart1['pointBackgroundColor'];
            datasets1['pointHoverBackgroundColor'] = graphMultipleChart1['pointHoverBackgroundColor'];
            datasets1['pointHoverBorderColor'] = graphMultipleChart1['pointHoverBorderColor'];
            datasets1['pointHoverBorderWidth'] = graphMultipleChart1['pointHoverBorderWidth'];
            datasets1['fill'] = false;

            datasets1['backgroundColor'] = "#cf455c";
            datasets1['hoverBackgroundColor'] = "#cf455c";

            data['datasets'][1] = datasets1;
        }

        if (datasets2 != null) {
            datasets2['backgroundColor'] = graphMultipleChart2['backgroundColor'];

            datasets2['backgroundColor'] = "#e89da2";
            datasets2['hoverBackgroundColor'] = "#e89da2";

            data['datasets'][2] = datasets2;
        }

        if (datasets3 != null) {
            datasets3['backgroundColor'] = graphMultipleChart3['backgroundColor'];

            datasets3['backgroundColor'] = "#e0b623";

            data['datasets'][3] = datasets3;
        }
    }

    else if (chart == "horizontalBar") {
        datasets['backgroundColor'] = colorHorizontalBarChart['backgroundColor'];
    }

    else if (chart == "doughnut") {
        datasets['backgroundColor'] = doughnutChart['backgroundColor'];
    }

    data['datasets'][0] = datasets;

    return data['datasets'];
}

export { loadColors }; 