import { UPDATE_RECEIVING_HEADER } from 'actions/types';

const initialState = {
  headerInfo: [],
  isShipmentFromPurchaseOrder: false,
};

export default function partialReceivingReducer(state = initialState, action) {
  switch (action.type) {
    case UPDATE_RECEIVING_HEADER:
      if (!action.payload) {
        return state;
      }

      return {
        ...state,
        headerInfo: action.payload.headerInfo,
        isShipmentFromPurchaseOrder: action.payload.isShipmentFromPurchaseOrder,
      };

    default:
      return state;
  }
}
