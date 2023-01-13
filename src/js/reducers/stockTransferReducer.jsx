import { FETCH_STOCK_TRANSFER_STATUSES } from 'actions/types';

const initialState = {
  statuses: [],
  sessionVersion: 0,
};

export default function stockTransferReducer(state = initialState, action) {
  switch (action.type) {
    case FETCH_STOCK_TRANSFER_STATUSES:
      return {
        ...state,
        statuses: action.payload.statuses,
        sessionVersion: action.payload.sessionVersion,
      };
    default:
      return state;
  }
}
