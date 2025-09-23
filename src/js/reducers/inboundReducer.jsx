import { UPDATE_WORKFLOW_HEADER } from 'actions/types';

const initialState = {
  headerInfo: [],
  headerStatus: null,
};

export default function inboundReducer(state = initialState, action) {
  switch (action.type) {
    case UPDATE_WORKFLOW_HEADER:
      if (!action.payload) {
        return state;
      }

      return {
        ...state,
        headerInfo: action.payload.headerInfo,
        headerStatus: action.payload.headerStatus,
      };

    default:
      return state;
  }
}
