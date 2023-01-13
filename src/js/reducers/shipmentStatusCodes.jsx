import { FETCH_SHIPMENT_STATUS_CODES } from 'actions/types';

const initialState = {
  data: [],
  fetched: false,
  sessionVersion: 0,
};

export default function (state = initialState, action) {
  switch (action.type) {
    case FETCH_SHIPMENT_STATUS_CODES:
      if (action.payload) {
        return {
          data: action.payload.statuses,
          fetched: true,
          sessionVersion: action.payload.sessionVersion,
        };
      }
      return state;
    default:
      return state;
  }
}
