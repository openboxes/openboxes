/* eslint no-param-reassign: ["error", { "props": false }] */
// eslint-disable-next-line no-unused-vars
import datalabels from 'chartjs-plugin-datalabels';
import { getColor, getArrayOfColors } from './colorMapping';

// === COLOR OPTIONS ===

function loadColorDataset(index, data, type, subtype, colorConfig) {
  const dataset = data;

  if (type === 'line') {
    // for a line graph we want a single color for all unless specified
    if (colorConfig.data.colorsArray && colorConfig.data.colorsArray.length) {
      dataset.pointBackgroundColor = getArrayOfColors(dataset.data.length, colorConfig);
      dataset.pointBorderColor = getArrayOfColors(dataset.data.length, colorConfig);
      dataset.pointHoverBackgroundColor = getArrayOfColors(dataset.data.length, colorConfig, true);
      dataset.pointHoverBorderColor = getArrayOfColors(dataset.data.length, colorConfig, true);
    } else {
      dataset.pointBackgroundColor = getColor(index, colorConfig);
      dataset.pointBorderColor = getColor(index, colorConfig);
      dataset.pointHoverBackgroundColor = getColor(index, colorConfig, true);
      dataset.pointHoverBorderColor = getColor(index, colorConfig, true);
    }
    colorConfig.data.colorsArray = null;
    dataset.borderColor = getColor(index, colorConfig);
    dataset.lineTension = 0;
    dataset.fill = !subtype;
  } if (type === 'bar') {
    dataset.backgroundColor = getColor(index, colorConfig);
    dataset.hoverBackgroundColor = getColor(index, colorConfig, true);
  } if (type === 'horizontalBar') {
    dataset.backgroundColor = getArrayOfColors(dataset.data.length, colorConfig);
    dataset.hoverBackgroundColor = getArrayOfColors(dataset.data.length, colorConfig, true);
  } if (type === 'doughnut') {
    dataset.backgroundColor = getArrayOfColors(dataset.data.length, colorConfig);
    dataset.hoverBackgroundColor = getArrayOfColors(dataset.data.length, colorConfig, true);
  }

  return dataset;
}

function loadGraphColors(payload) {
  const { datasets } = payload.data;

  const colorConfig = {
    palette: 'default',
    data: {
      color: null,
      colorsArray: null,
    },
  };
  if (payload.config.colors && payload.config.colors.palette) {
    colorConfig.palette = payload.config.colors.palette;
  }

  for (let i = 0; i < datasets.length; i += 1) {
    const type = datasets[i].type || payload.type;

    if (payload.config.colors && payload.config.colors.datasets) {
      colorConfig.data.color = Object.keys(payload.config.colors.datasets)
        .find(key => payload.config.colors.datasets[key].includes(datasets[i].label));
    }

    if (payload.config.colors && payload.config.colors.labels) {
      const datasetColor = colorConfig.data.color;
      colorConfig.data.colorsArray = payload.data.labels.map(() => datasetColor);
      payload.data.labels.forEach((label, index) => {
        const labelColor = Object.keys(payload.config.colors.labels)
          .find((key) => {
            // We shouldn't use that line once the config of all graphs color will be with map.
            let response = payload.config.colors.labels[key].includes(label);

            payload.config.colors.labels[key].forEach((labelConfig) => {
              if (labelConfig.code && labelConfig.message && label.code && label.message) {
                if (labelConfig.code === label.code && labelConfig.message === label.message) {
                  response = true;
                }
              }
            });
            return response;
          });
        if (labelColor) {
          colorConfig.data.colorsArray[index] = labelColor;
        }
      });
    }

    datasets[i] = loadColorDataset(
      payload.id + i,
      datasets[i],
      type,
      datasets[i].type,
      colorConfig,
    );
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
      if (dataset.type !== 'line') {
        sum += dataset.data[context.dataIndex] || 0;
      }
      return sum;
    });
    return sum;
  }
  return '';
}

function getOptions(isStacked = false, hasDataLabel = false, alignLabel = '', maxValue = null, minValue = null, isDoughnut = false, legend = false, doubleAxeY = false, sizeGraph = null) {
  const options = {
    legend: {
      display: (isDoughnut || legend)
      && (window.innerWidth > 1000 || (window.innerWidth < 865 && window.innerWidth > 590) || (sizeGraph === 'big' && window.innerWidth > 500)),
      labels: {
        fontSize: 12,
        usePointStyle: true,
      },
    },
    onResize(chart, size) {
      if (size.width < 400) {
        chart.options.legend.display = false;
      } else chart.options.legend.display = legend === true;
      chart.update();
    },
    scales: {
      xAxes: [{
        display: !isDoughnut,
        gridLines: {
          color: 'transparent',
        },
        ticks: {
          precision: 0,
        },
      }],
      yAxes: doubleAxeY === true ? [
        {
          position: 'left',
          id: 'left-y-axis',
          ticks: {
            precision: 0,
          },
          display: !isDoughnut,
        },
        {
          position: 'right',
          id: 'right-y-axis',
          ticks: {
            precision: 0,
            max: 100,
            callback(value) {
              return `${value.toFixed(0)} %`; // convert it to percentage
            },
          },
          display: !isDoughnut,
        },
      ] : [
        {
          position: 'left',
          ticks: {
            precision: 0,
          },
          display: !isDoughnut,
        },
      ],
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

  if (hasDataLabel && !isDoughnut) {
    options.plugins.datalabels = {
      anchor(context) {
        const value = context.dataset.data[context.dataIndex];
        return value >= 0 ? 'end' : 'start';
      },
      align(context) {
        const value = context.dataset.data[context.dataIndex];
        if (alignLabel === 'vertical') {
          return value >= 0 ? 'top' : 'bottom';
        }
        return value >= 0 ? 'right' : 'left';
      },
      offset: 5,
      color(context) {
        return context.dataset.backgroundColor;
      },
      formatter(value, context) {
        if (isStacked) return loadDatalabel(context);
        return value;
      },
    };

    // Add Math.ceil(maxValue / maxTicks) to try to ensure an extra tick will be added
    // maxTicks = 11
    if (alignLabel === 'horizontal' && maxValue) {
      options.scales.xAxes[0].ticks = {
        suggestedMax: maxValue + Math.ceil(maxValue / 11),
      };
    }
    if (alignLabel === 'horizontal' && minValue) {
      options.scales.xAxes[0].ticks = {
        suggestedMin: minValue - Math.ceil(minValue / 11),
      };
    }
    if (alignLabel === 'vertical' && maxValue) {
      options.scales.yAxes[0].ticks = {
        suggestedMax: maxValue + Math.ceil(maxValue / 11),
      };
    }
    if (alignLabel === 'vertical' && minValue) {
      options.scales.yAxes[0].ticks = {
        suggestedMin: minValue - Math.ceil(minValue / 11),
      };
    }
  }

  if (hasDataLabel && isDoughnut) {
    options.plugins.datalabels = {
      display: 'auto',
      formatter: (value, ctx) => {
        let sum = 0;
        const dataArr = ctx.chart.data.datasets[0].data;
        dataArr.forEach((data) => {
          sum += data;
        });
        const percentageValue = sum > 0 ? (value / sum) * 100 : 0;
        return `${Math.round(percentageValue * 100) / 100}%`;
      },
      color: '#fff',
    };
  }
  return options;
}

function loadGraphOptions(payload) {
  let labelAlignment = null;
  let maxValue = null;
  let minValue = null;
  const isDoughnut = payload.type === 'doughnut';
  const legend = payload.legend === true;
  const doubleAxeY = payload.doubleAxeY === true;
  const sizeGraph = payload.size;

  if (payload.config.datalabel) {
    labelAlignment = (payload.type === 'horizontalBar') ? 'horizontal' : 'vertical';

    let sumDatasets = 0;
    if (!payload.config.stacked || payload.data.datasets.length === 1) {
      sumDatasets = payload.data.datasets[0].data;
    } else if (payload.data.datasets.length) {
      sumDatasets = payload.data.datasets.reduce((sum, value) => {
        if (sum.data) {
          return sum.data.map((s, index) => s + value.data[index]);
        }
        return sum.map((s, index) => s + value.data[index]);
      });
    }
    maxValue = Math.max(...sumDatasets) > 0 ? Math.max(...sumDatasets) : null;
    minValue = Math.min(...sumDatasets) < 0 ? Math.min(...sumDatasets) : null;
  }

  return getOptions(
    payload.config.stacked,
    payload.config.datalabel,
    labelAlignment,
    maxValue,
    minValue,
    isDoughnut,
    legend,
    doubleAxeY,
    sizeGraph,
  );
}

export { loadGraphColors, loadGraphOptions };
