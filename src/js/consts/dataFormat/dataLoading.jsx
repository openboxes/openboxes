// eslint-disable-next-line no-unused-vars
import datalabels from 'chartjs-plugin-datalabels';
import colorsData from './colorMapping';

/* global _ */

function getRandomColor(index = null, palette = 'default') {
  const paletteLength = colorsData.state[palette].length;

  if (!index) {
    return colorsData.state.default[_.random(0, paletteLength)];
  }

  try {
    // index % length makes sure that index is in range
    return colorsData.state[palette][index % paletteLength];
  } catch (error) {
    // if error, returns a random normal color
    return colorsData.state.default[_.random(0, paletteLength)];
  }
}

function getColorByName(name, palette) {
  const gyrMatch = name.match(/(success|warning|error)/);
  const stateMatch = name.match(/state([0-9]+)/);

  if (gyrMatch) {
    let gyrIndex = 0;
    if (gyrMatch[1].toLowerCase() === 'warning') {
      gyrIndex = 1;
    } else if (gyrMatch[1].toLowerCase() === 'error') {
      gyrIndex = 2;
    }
    return colorsData.gyr[palette][gyrIndex];
  }
  if (stateMatch) {
    const stateIndex = stateMatch[1] - 1;
    return colorsData.state[palette][stateIndex];
  }

  // If no match, return random color
  return getRandomColor(_.random(0, 8), palette);
}

function getColor(index, config, hover = false) {
  let { palette } = config;
  if (hover) {
    const palettes = ['default', 'dark', 'light'];
    palette = palettes[(palettes.indexOf(palette) + 1) % palettes.length];
  }

  if (!config.data) {
    return getRandomColor(index, palette);
  } else if (Array.isArray(config.data)) {
    return getColorByName(config.data[index], palette);
  }
  return getColorByName(config.data, palette);
}

function getArrayOfColors(length, config, hover = false) {
  const colorsArray = [];
  for (let index = 0; index < length; index += 1) {
    const color = getColor(index, config, hover);
    colorsArray.push(color);
  }
  return colorsArray;
}

function loadColorDataset(index, data, type, subtype, colorConfig) {
  const dataset = data;

  if (type === 'line') {
    dataset.borderColor = getColor(index, colorConfig);
    dataset.pointBackgroundColor = getColor(index, colorConfig);
    dataset.pointHoverBorderColor = getColor(index, colorConfig, true);
    dataset.pointHoverBackgroundColor = getColor(index, colorConfig, true);
    dataset.lineTension = 0;
    dataset.fill = !subtype;
  } if (type === 'bar') {
    dataset.backgroundColor = getColor(index, colorConfig);
    dataset.hoverBackgroundColor = getColor(index, colorConfig, true);
  } if (type === 'horizontalBar') {
    dataset.backgroundColor = getArrayOfColors(dataset.data.length, colorConfig);
    dataset.hoverBackgroundColor = getArrayOfColors(dataset.data.length, colorConfig, true);
  } if (type === 'doughnut') {
    dataset.backgroundColor = getColor(index, colorConfig);
  }

  return dataset;
}

function loadGraphColors(payload) {
  const { datasets } = payload.data;

  const colorConfig = {
    palette: 'default',
    data: null,
  };
  if (payload.config.colors && payload.config.colors.palette) {
    colorConfig.palette = payload.config.colors.palette;
  }

  for (let i = 0; i < datasets.length; i += 1) {
    const type = datasets[i].type || payload.type;

    if (payload.config.colors && payload.config.colors.labels) {
      colorConfig.data = payload.data.labels.map(label => payload.config.colors.labels[label]);
    }

    if (payload.config.colors && payload.config.colors.datasets) {
      colorConfig.data = payload.config.colors.datasets[datasets[i].label];
    }

    datasets[i] = loadColorDataset(payload.id + i, datasets[i], type, datasets[i].type, colorConfig);
  }
  return datasets;
}

// === GRAPH OPTIONS ===

function loadDatalabel(context) {
  const { datasets } = context.chart.data;

  // If this is the last visible dataset of the chart
  if (datasets.indexOf(context.dataset) === datasets.length - 1) {
    let sum = 0;
    datasets.map((dataset) => {
      sum += dataset.data[context.dataIndex];
      return sum;
    });
    return sum;
  }
  return '';
}

function getOptions(isStacked = false, hasDataLabel = false, alignLabel = '', maxValue = null) {
  const options = {
    scales: {
      xAxes: [{
        gridLines: {
          color: 'transparent',
        },
      }],
      yAxes: [{}],
    },
    plugins: {
      datalabels: {
        display: false,
      },
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

  if (isStacked) {
    options.scales.xAxes[0].stacked = true;
    options.scales.yAxes[0].stacked = true;
  }

  if (hasDataLabel) {
    options.plugins.datalabels = {
      anchor: 'end',
      align: alignLabel,
      offset: 10,
      color(context) {
        return context.dataset.backgroundColor;
      },
      formatter(value, context) {
        if (isStacked) return loadDatalabel(context);
        return value;
      },
    };

    if (alignLabel === 'right' && maxValue) {
      options.scales.xAxes[0].ticks = {
        suggestedMax: maxValue + 1,
      };
    }

    if (alignLabel === 'top' && maxValue) {
      options.scales.yAxes[0].ticks = {
        suggestedMax: maxValue + 1,
      };
    }
  }

  return options;
}

function loadGraphOptions(payload) {
  let labelAlignment = null;
  let maxValue = null;

  if (payload.config.datalabel) {
    labelAlignment = (payload.type === 'horizontalBar') ? 'right' : 'top';

    let sumDatasets = 0;
    if (!payload.config.stacked) {
      sumDatasets = payload.data.datasets[0].data;
    } else {
      sumDatasets = payload.data.datasets.reduce((sum, value) => {
        if (sum.data) {
          return sum.data.map((s, index) => s + value.data[index]);
        }
        return sum.map((s, index) => s + value.data[index]);
      });
    }
    maxValue = Math.max(...sumDatasets);
  }

  return getOptions(payload.config.stacked, payload.config.datalabel, labelAlignment, maxValue);
}

export { loadGraphColors, getRandomColor, loadGraphOptions };
