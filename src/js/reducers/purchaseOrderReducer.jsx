import { FETCH_PURCHASE_ORDER_STATUSES } from 'actions/types';

const initialState = {
  statuses: [],
};

export default function purchaseOrderReducer(state = initialState, action) {
  switch (action.type) {
    case FETCH_PURCHASE_ORDER_STATUSES:
      return {
        ...state,
        statuses: action.payload,
      };
    default:
      return state;
  }
}
