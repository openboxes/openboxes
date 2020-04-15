import ColorPalette from '../../components/tablero/ColorPalette.scss';

/* global _ */
function getColor(color = 'default', type = 'normal', index = 0) {
  const colors = {
    default: ColorPalette.colorDefault,
    primary: {
      normal: ColorPalette.colorPrimaryNormal,
      dark: ColorPalette.colorPrimaryDark,
      light: ColorPalette.colorPrimaryLight,
    },
    secondary: {
      normal: ColorPalette.colorSecondaryNormal,
      dark: ColorPalette.colorSecondaryDark,
      light: ColorPalette.colorSecondaryLight,
    },
    tertiary: {
      normal: ColorPalette.colorTertiaryNormal,
      dark: ColorPalette.colorTertiaryDark,
      light: ColorPalette.colorTertiaryLight,
    },
    states: {
      normal: ColorPalette.normalState,
      dark: ColorPalette.darkState,
      light: ColorPalette.lightState,
    },
  };

  if (color === 'default') {
    return colors.states.normal[_.random(0, 8)];
  } try {
    return colors[color][type][index];
  } catch (error) {
    return colors.default;
  }
  // if can't return desired color, returns gray
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
  // Index makes sure that index is between 0 and 8
  // That following indicators have different colors
  // And a smooth color change

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
