import { UPDATE_WORKFLOW_HEADER } from 'actions/types';

const initialState = {
  headerInfo: [],
  headerStatus: {},
  workflowName: 'Inbound',
};

export default function inboundReducer(state = initialState, action) {
  switch (action.type) {
    case UPDATE_WORKFLOW_HEADER:
      if (!action.payload || action.payload.workflowName !== 'Inbound') {
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
