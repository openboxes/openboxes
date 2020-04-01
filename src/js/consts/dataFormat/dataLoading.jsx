/* global _ */
function getColor() {
  const colors = [
    '#6fb98f',
    '#004445',
    '#2e5685',
    '#fcc169',
    '#cf455c',
    '#ff0000',
    '#e89da2',
    '#e0b623',
    '#444444',
  ];
  return colors[_.random(0, colors.length - 1)];
}

function loadColorDataset(data, chart, subtype) {
  const datasets = data;
  const color = getColor();

  if (chart === 'line') {
    datasets.borderColor = color;
    datasets.pointBackgroundColor = color;
    datasets.pointHoverBackgroundColor = '#fff';
    datasets.pointHoverBorderColor = color;
    datasets.lineTension = 0;
    datasets.fill = !subtype;
  } if (chart === 'bar') {
    datasets.backgroundColor = color;
    datasets.hoverBackgroundColor = color;
  } if (chart === 'horizontalBar') {
    datasets.backgroundColor = [getColor(), getColor(), getColor(), getColor(), getColor()];
    datasets.hoverBackgroundColor = datasets.backgroundColor;
  } if (chart === 'doughnut') {
    datasets.backgroundColor = color;
  }

  return datasets;
}

function loadColors(data, chart) {
  const dataset = data.datasets;
  for (let i = 0; i < dataset.length; i += 1) {
    const type = dataset[i].type || chart;
    dataset[i] = loadColorDataset(dataset[i], type, dataset[i].type);
  }
  return dataset;
}

function loadOptions(isStacked = false) {
  const options = {
    scales: isStacked ? {
      xAxes: [{
        stacked: true,
      }],
      yAxes: [{
        stacked: true,
      }],
    } : null,
    tooltips: {
      displayColors: false,
      enabled: true,
      yPadding: 5,
      xPadding: 15,
      cornerRadius: 4,
      titleAlign: 'center',
      titleFontSize: 13,
      bodyAlign: 'center',
      callbacks: {
        title: (tooltipItem, data) => {
          let title = data.datasets[tooltipItem[0].datasetIndex].label || '';

          if (title) {
            title += ': ';
          }
          title += data.datasets[tooltipItem[0].datasetIndex].data[tooltipItem[0].index];

          return title;
        },
        label: (tooltipItem, data) => data.labels[tooltipItem.index],
      },
    },
  };

  return options;
}

export { loadColors, getColor, loadOptions };
