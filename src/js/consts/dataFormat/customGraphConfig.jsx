import { getColorByName } from 'consts/dataFormat/colorMapping';

function loadNumbersOptions(payload) {
  const {
    colors,
    columnsSize,
    truncationLength,
    disableTruncation,
  } = payload.config;
  const palette = (colors && colors.palette) ? colors.palette : 'default';

  const options = {
    colors: {
      first: getColorByName('default'),
      second: getColorByName('default'),
      third: getColorByName('default'),
    },
    columnsSize,
    truncationLength,
    disableTruncation,
  };

  if (colors && colors.datasets) {
    Object.entries(colors.datasets).forEach(([key, value]) => {
      options.colors[value] = getColorByName(key, palette);
    });
  }

  return options;
}

// eslint-disable-next-line import/prefer-default-export
export { loadNumbersOptions };
