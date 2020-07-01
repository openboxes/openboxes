import ColorPalette from '../../components/tablero/ColorPalette.scss';

/* global _ */

function formatColorPalette(colorPalette) {
  const formattedPalette = {
    state: {
      default: [],
      dark: [],
      light: [],
    },
    gyr: {
      // [ success, warning, error ]
      default: [],
      dark: [],
      light: [],
    },
    default: null,
  };

  Object.entries(colorPalette).forEach(([key, value]) => {
    const stateMatch = key.match(/^(light|dark)?[sS]tate[0-9]+$/);
    const gyrMatch = key.match(/^(light|dark)?([sS]uccess|[wW]arning|[eE]rror)$/);

    if (stateMatch) {
      formattedPalette.state[stateMatch[1] || 'default'].push(value);
    }
    if (gyrMatch) {
      let index = 0;
      if (gyrMatch[2].toLowerCase() === 'warning') {
        index = 1;
      } else if (gyrMatch[2].toLowerCase() === 'error') {
        index = 2;
      }
      formattedPalette.gyr[gyrMatch[1] || 'default'][index] = value;
    }
    if (key === 'default') {
      formattedPalette.default = value;
    }
  });

  return formattedPalette;
}

const COLORS = formatColorPalette(ColorPalette);

function getRandomColor(index = null, palette = 'default') {
  const paletteLength = COLORS.state[palette].length;

  if (!index) {
    return COLORS.state[palette][_.random(0, paletteLength - 1)];
  }

  try {
    // index % length makes sure that index is in range
    return COLORS.state[palette][index % paletteLength];
  } catch (error) {
    // if error, returns a random normal color
    return COLORS.state[palette][_.random(0, paletteLength - 1)];
  }
}

function getColorByName(name, palette) {
  const gyrMatch = name.match(/(success|warning|error)/);
  const stateMatch = name.match(/state([0-9]+)/);

  if (name === 'default') {
    return COLORS.default;
  }
  if (gyrMatch) {
    let gyrIndex = 0;
    if (gyrMatch[1].toLowerCase() === 'warning') {
      gyrIndex = 1;
    } else if (gyrMatch[1].toLowerCase() === 'error') {
      gyrIndex = 2;
    }
    return COLORS.gyr[palette][gyrIndex];
  }
  if (stateMatch) {
    const stateIndex = stateMatch[1] - 1;
    return COLORS.state[palette][stateIndex];
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

  if (config.data.colorsArray && config.data.colorsArray.length) {
    return getColorByName(config.data.colorsArray[index], palette);
  }
  if (config.data.color) {
    return getColorByName(config.data.color, palette);
  }
  return getRandomColor(index, palette);
}

function getArrayOfColors(length, config, hover = false) {
  const colorsArray = [];
  for (let index = 0; index < length; index += 1) {
    const color = getColor(index, config, hover);
    colorsArray.push(color);
  }
  return colorsArray;
}

export { getColor, getArrayOfColors, getRandomColor, getColorByName };
