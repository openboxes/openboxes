import ColorPalette from '../../components/tablero/ColorPalette.scss';

/* global _ */
function getColor(index = 0, type = 'default') {
  const states = {
    normal: [
      ColorPalette.normalState1,
      ColorPalette.normalState2,
      ColorPalette.normalState3,
      ColorPalette.normalState4,
      ColorPalette.normalState5,
      ColorPalette.normalState6,
      ColorPalette.normalState7,
      ColorPalette.normalState8,
    ],
    dark: [
      ColorPalette.darkState1,
      ColorPalette.darkState2,
      ColorPalette.darkState3,
      ColorPalette.darkState4,
      ColorPalette.darkState5,
      ColorPalette.darkState6,
      ColorPalette.darkState7,
      ColorPalette.darkState8,
    ],
  };

  try {
    // index % 8 makes sure that index is between 0 and 8
    return states[type][index % 8];
  } catch (error) {
    // if can't return desired color, returns a random color
    return states.normal[_.random(0, 8)];
  }
}

function getHorizontalBarColors(index = 0, type = 'normal') {
  const horizontalColors = [];
  for (let i = 0; i < 5; i += 1) {
    horizontalColors.push(getColor(index + i, type));
  }
  return horizontalColors;
}

let index = 5;
function loadColorDataset(data, chart, subtype) {
  const datasets = data;
  index = index > 7 ? index % 8 : index;
  // Index makes sure that index is between 0 and 8
  // That following indicators have different colors
  // And a smooth color change

  if (chart === 'line') {
    datasets.borderColor = getColor(index, 'normal');
    datasets.pointBackgroundColor = getColor(index, 'normal');
    datasets.pointHoverBackgroundColor = '#fff';
    datasets.pointHoverBorderColor = getColor(index, 'normal');
    datasets.lineTension = 0;
    datasets.fill = !subtype;
  } if (chart === 'bar') {
    datasets.backgroundColor = getColor(index, 'normal');
    datasets.hoverBackgroundColor = getColor(index, 'dark');
  } if (chart === 'horizontalBar') {
    datasets.backgroundColor = getHorizontalBarColors(index, 'normal');
    datasets.hoverBackgroundColor = getHorizontalBarColors(index, 'dark');
  } if (chart === 'doughnut') {
    datasets.backgroundColor = getColor(index, 'normal');
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
