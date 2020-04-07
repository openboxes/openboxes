/* global _ */
function getColor(color = 'default', type = 'normal', index = 0) {
  const colors = {
    default: '#8798ad',
    primary: {
      normal: ['#0ddbdd'],
      dark: ['#08c6c8'],
      light: ['#52e8e8'],
    },
    secondary: {
      normal: ['#ea6227'],
      dark: ['#d05622'],
      light: ['#ff9c72'],
    },
    tertiary: {
      normal: ['#0c60e5'],
      dark: ['#0a58d5'],
      light: ['#186cf2'],
    },
    states: {
      normal: [
        '#f57c00',
        '#fbc02d',
        '#689f38',
        '#00796b',
        '#0288d1',
        '#303f9f',
        '#7b1fa2',
        '#d32f2f',
      ],
      dark: [
        '#e65100',
        '#f57f17',
        '#33691e',
        '#004d40',
        '#01579b',
        '#1a237e',
        '#4a148c',
        '#b71c1c',
      ],
      light: [
        '#ffa726',
        '#ffee58',
        '#9ccc65',
        '#26a69a',
        '#29b6f6',
        '#5c6bc0',
        '#ab47bc',
        '#ef5350',
      ],
    },
  };

  if (color === 'default') {
    return colors.states.normal[_.random(0, 8)];
  } try {
    return colors[color][type][index];
  } catch (error) {
    return colors.default;
  }
}

function getHorizontalBarColors(type, index) {
  const horizontalColors = [];
  for (let i = 0; i < 5; i += 1) {
    horizontalColors.push(getColor('states', type, i + (index % 8)));
  }
  return horizontalColors;
}

let index = 5;
function loadColorDataset(data, chart, subtype) {
  const datasets = data;
  index = index > 7 ? index % 8 : index;

  if (chart === 'line') {
    datasets.borderColor = getColor('states', 'normal', index);
    datasets.pointBackgroundColor = getColor('states', 'normal', index);
    datasets.pointHoverBackgroundColor = '#fff';
    datasets.pointHoverBorderColor = getColor('states', 'normal', index);
    datasets.lineTension = 0;
    datasets.fill = !subtype;
  } if (chart === 'bar') {
    datasets.backgroundColor = getColor('states', 'normal', index);
    datasets.hoverBackgroundColor = getColor('states', 'dark', index);
  } if (chart === 'horizontalBar') {
    datasets.backgroundColor = getHorizontalBarColors('normal', index);
    datasets.hoverBackgroundColor = getHorizontalBarColors('dark', index);
  } if (chart === 'doughnut') {
    datasets.backgroundColor = getColor('states', 'normal', index);
  }

  index += 1;
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
        gridLines: {
          color: 'transparent',
        },
      }],
      yAxes: [{
        stacked: true,
      }],
    } : {
      xAxes: [{
        gridLines: {
          color: 'transparent',
        },
      }],
    },
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
