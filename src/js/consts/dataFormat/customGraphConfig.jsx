import { getColorByName } from './colorMapping';

function loadNumbersOptions(payload) {
  const palette = (payload.config.colors && payload.config.colors.palette) ?
    payload.config.colors.palette : 'default';

  const options = {
    colors: {
      first: getColorByName('default'),
      second: getColorByName('default'),
      third: getColorByName('default'),
    },
  };

  if (payload.config.colors && payload.config.colors.datasets) {
    Object.entries(payload.config.colors.datasets).forEach(([key, value]) => {
      options.colors[value] = getColorByName(key, palette);
    });
  }

  return options;
}

// eslint-disable-next-line import/prefer-default-export
export { loadNumbersOptions };
