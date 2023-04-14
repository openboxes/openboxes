import {
  ADD_INFO_BAR,
  CLOSE_INFO_BAR,
  HIDE_INFO_BAR,
  HIDE_INFO_BAR_MODAL,
  SHOW_INFO_BAR,
  SHOW_INFO_BAR_MODAL,
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
          propertyToChange: { show: false, isModalOpen: false },
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
    case SHOW_INFO_BAR_MODAL:
      return {
        ...state,
        bars: editProperty({
          bars: state.bars,
          name: action.payload.name,
          propertyToChange: { isModalOpen: true },
        }),
      };
    case HIDE_INFO_BAR_MODAL:
      return {
        ...state,
        bars: editProperty({
          bars: state.bars,
          name: action.payload.name,
          propertyToChange: { isModalOpen: false },
        }),
      };
    default:
      return state;
  }
}
