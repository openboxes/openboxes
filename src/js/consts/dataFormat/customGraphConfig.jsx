import { getColorByName, getRandomColor } from './colorMapping';

function loadNumbersOptions(payload) {
  const palette = (payload.config.colors && payload.config.colors.palette) ?
    payload.config.colors.palette : 'default';

  const options = {
    colors: {
      first: getRandomColor(null, palette),
      second: getRandomColor(null, palette),
      third: getRandomColor(null, palette),
    },
  };

  if (payload.config.colors && payload.config.colors.datasets) {
    Object.entries(payload.config.colors.datasets).forEach(([key, value]) => {
      options.colors[key] = getColorByName(value, palette);
    });
  }

  return options;
}

// eslint-disable-next-line import/prefer-default-export
export { loadNumbersOptions };
