import {
  colorLineChart,
  colorBarChart,
  colorHorizontalBarChart,
  doughnutChart,
  graphMultipleChart0,
  graphMultipleChart1,
  graphMultipleChart2,
  graphMultipleChart3,
} from './chartColors';

function loadColors(data, chart) {
  const dataLoaded = data;
  const datasets = data.datasets[0];
  const datasets1 = data.datasets[1];
  const datasets2 = data.datasets[2];
  const datasets3 = data.datasets[3];


  if (chart === 'line') {
    if (datasets1 == null && datasets2 == null && datasets3 == null) {
      datasets.borderColor = colorLineChart.borderColor;
      datasets.pointBorderColor = colorLineChart.pointBorderColor;
      datasets.pointBorderWidth = colorLineChart.pointBorderWidth;
      datasets.pointBackgroundColor = colorLineChart.pointBackgroundColor;
      datasets.pointHoverBackgroundColor = colorLineChart.pointHoverBackgroundColor;
      datasets.pointHoverBorderColor = colorLineChart.pointHoverBorderColor;
      datasets.pointHoverBorderWidth = colorLineChart.pointHoverBorderWidth;
      datasets.lineTension = colorLineChart.lineTension;
    } else {
      datasets.borderColor = graphMultipleChart0.borderColor;
      datasets.pointBorderColor = graphMultipleChart0.pointBorderColor;
      datasets.pointBorderWidth = graphMultipleChart0.pointBorderWidth;
      datasets.pointBackgroundColor = graphMultipleChart0.pointBackgroundColor;
      datasets.pointHoverBackgroundColor = graphMultipleChart0.pointHoverBackgroundColor;
      datasets.pointHoverBorderColor = graphMultipleChart0.pointHoverBorderColor;
      datasets.pointHoverBorderWidth = graphMultipleChart0.pointHoverBorderWidth;
    }


    if (datasets1 != null) {
      datasets1.borderColor = graphMultipleChart1.borderColor;
      datasets1.pointBorderColor = graphMultipleChart1.pointBorderColor;
      datasets1.pointBorderWidth = graphMultipleChart1.pointBorderWidth;
      datasets1.pointBackgroundColor = graphMultipleChart1.pointBackgroundColor;
      datasets1.pointHoverBackgroundColor = graphMultipleChart1.pointHoverBackgroundColor;
      datasets1.pointHoverBorderColor = graphMultipleChart1.pointHoverBorderColor;
      datasets1.pointHoverBorderWidth = graphMultipleChart1.pointHoverBorderWidth;

      dataLoaded.datasets[1] = datasets1;
    }

    if (datasets2 != null) {
      datasets2.backgroundColor = graphMultipleChart2.backgroundColor;

      dataLoaded.datasets[2] = datasets2;
    }

    if (datasets3 != null) {
      datasets3.backgroundColor = graphMultipleChart3.backgroundColor;

      dataLoaded.datasets[3] = datasets3;
    }
  } else if (chart === 'horizontalBar') {
    datasets.backgroundColor = colorHorizontalBarChart.backgroundColor;
  } else if (chart === 'bar') {
    datasets.backgroundColor = colorBarChart.backgroundColor;
  } else if (chart === 'doughnut') {
    datasets.backgroundColor = doughnutChart.backgroundColor;
  }

  dataLoaded.datasets[0] = datasets;

  return dataLoaded.datasets;
}

export default loadColors;
