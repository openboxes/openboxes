import { FETCH_SHIPMENT_STATUS_CODES } from 'actions/types';

const initialState = {
  data: [],
  fetched: false,
};

export default function (state = initialState, action) {
  switch (action.type) {
    case FETCH_SHIPMENT_STATUS_CODES:
      if (action.payload) {
        return {
          data: action.payload,
          fetched: true,
        };
      }
      return state;
    default:
      return state;
  }
}
