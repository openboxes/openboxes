import { FETCH_INVOICE_STATUSES, FETCH_INVOICE_TYPE_CODES } from 'actions/types';

const initialState = {
  statuses: [],
  typeCodes: [],
  sessionVersion: 0,
};

export default function invoiceReducer(state = initialState, action) {
  switch (action.type) {
    case FETCH_INVOICE_STATUSES:
      return {
        ...state,
        statuses: action.payload.statuses,
        sessionVersion: action.payload.sessionVersion,
      };
    case FETCH_INVOICE_TYPE_CODES:
      return {
        ...state,
        typeCodes: action.payload.typeCodes,
        sessionVersion: action.payload.sessionVersion,
      };
    default:
      return state;
  }
}
