import { FETCH_PAYMENT_TERMS, FETCH_PURCHASE_ORDER_STATUSES } from 'actions/types';

const initialState = {
  statuses: [],
  paymentTerms: [],
};

export default function purchaseOrderReducer(state = initialState, action) {
  switch (action.type) {
    case FETCH_PURCHASE_ORDER_STATUSES:
      return {
        ...state,
        statuses: action.payload,
      };
    case FETCH_PAYMENT_TERMS:
      return {
        ...state,
        paymentTerms: action.payload,
      };
    default:
      return state;
  }
}
