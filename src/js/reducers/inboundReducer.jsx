import { UPDATE_INBOUND_HEADER } from 'actions/types';

const initialState = {
  headerInfo: [],
  headerStatus: {},
};

export default function inboundReducer(state = initialState, action) {
  switch (action.type) {
    case UPDATE_INBOUND_HEADER:
      if (!action.payload) {
        return state;
      }

      return {
        ...state,
        headerInfo: action.payload.headerInfo ?? state.headerInfo,
        headerStatus: action.payload.headerStatus ?? state.headerStatus,
      };

    default:
      return state;
  }
}
