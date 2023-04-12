import {
  ADD_FEATURE_BAR,
  CLOSE_FEATURE_BAR,
  HIDE_FEATURE_BAR,
  SHOW_FEATURE_BAR,
} from 'actions/types';

const initialState = {
  features: {},
};

const editProperty = ({ features, name, propertyToChange }) => ({
  ...features,
  [name]: {
    ...features[name],
    ...propertyToChange,
  },
});

export default function newFeaturesReducer(state = initialState, action) {
  switch (action.type) {
    case ADD_FEATURE_BAR:
      return {
        ...state,
        features: {
          ...state.features,
          [action.payload.name]: action.payload,
        },
      };
    case HIDE_FEATURE_BAR:
      return {
        ...state,
        features: editProperty({
          features: state.features,
          name: action.payload.name,
          propertyToChange: { show: false },
        }),
      };
    case CLOSE_FEATURE_BAR:
      return {
        ...state,
        features: editProperty({
          features: state.features,
          name: action.payload.name,
          propertyToChange: { show: false, closed: true },
        }),
      };
    case SHOW_FEATURE_BAR:
      return {
        ...state,
        features: editProperty({
          features: state.features,
          name: action.payload.name,
          propertyToChange: { show: true },
        }),
      };
    default:
      return state;
  }
}
