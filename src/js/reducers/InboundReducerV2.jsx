import { UPDATE_INBOUNDV2_DATA } from 'actions/types';

const initialState = {
  initialValues: {
  },
};

export default function inboundV2Reducer(state = initialState, action) {
  switch (action.type) {
    case UPDATE_INBOUNDV2_DATA:
      return {
        ...state,
        initialValues: {
          ...state.initialValues,
          ...action.payload,
        },
      };

    default:
      return state;
  }
}
