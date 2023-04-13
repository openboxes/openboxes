import {
  ADD_INFO_BAR,
  CLOSE_INFO_BAR,
  HIDE_INFO_BAR,
  SHOW_INFO_BAR,
} from 'actions/types';

const initialState = {
  bars: {},
};

const editProperty = ({ bars, name, propertyToChange }) => ({
  ...bars,
  [name]: {
    ...bars[name],
    ...propertyToChange,
  },
});

export default function infoBarReducer(state = initialState, action) {
  switch (action.type) {
    case ADD_INFO_BAR:
      return {
        ...state,
        bars: {
          ...state.bars,
          [action.payload.name]: action.payload,
        },
      };
    case HIDE_INFO_BAR:
      return {
        ...state,
        bars: editProperty({
          bars: state.bars,
          name: action.payload.name,
          propertyToChange: { show: false },
        }),
      };
    case CLOSE_INFO_BAR:
      return {
        ...state,
        bars: editProperty({
          bars: state.bars,
          name: action.payload.name,
          propertyToChange: { show: false, closed: true },
        }),
      };
    case SHOW_INFO_BAR:
      return {
        ...state,
        bars: editProperty({
          bars: state.bars,
          name: action.payload.name,
          propertyToChange: { show: true },
        }),
      };
    default:
      return state;
  }
}
