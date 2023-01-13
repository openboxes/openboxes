import { FETCH_PURCHASE_ORDER_STATUSES } from 'actions/types';

const initialState = {
  statuses: [],
  sessionVersion: 0,
};

export default function purchaseOrderReducer(state = initialState, action) {
  switch (action.type) {
    case FETCH_PURCHASE_ORDER_STATUSES:
      return {
        ...state,
        statuses: action.payload.statuses,
        sessionVersion: action.payload.sessionVersion,
      };
    default:
      return state;
  }
}
