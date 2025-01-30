import { UPDATE_INBOUND_DATA } from 'actions/types';

const initialState = {
  initialValues: {
  },
};

export default function inboundReducer(state = initialState, action) {
  switch (action.type) {
    case UPDATE_INBOUND_DATA:
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
