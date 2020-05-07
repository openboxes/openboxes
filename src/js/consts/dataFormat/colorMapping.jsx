import ColorPalette from '../../components/tablero/ColorPalette.scss';

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
  });

  return formattedPalette;
}

export default formatColorPalette(ColorPalette);
