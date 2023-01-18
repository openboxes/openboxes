import { FETCH_STOCK_TRANSFER_STATUSES } from 'actions/types';

const initialState = {
  statuses: [],
};

export default function stockTransferReducer(state = initialState, action) {
  switch (action.type) {
    case FETCH_STOCK_TRANSFER_STATUSES:
      return {
        ...state,
        statuses: action.payload,
      };
    default:
      return state;
  }
}
