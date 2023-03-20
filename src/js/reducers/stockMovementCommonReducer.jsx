import { FETCH_SHIPMENT_TYPES } from 'actions/types';

const initialState = {
  shipmentTypes: [],
};

export default function stockMovementCommonReducer(state = initialState, action) {
  switch (action.type) {
    case FETCH_SHIPMENT_TYPES:
      return {
        ...state,
        shipmentTypes: action.payload,
      };
    default:
      return state;
  }
}
