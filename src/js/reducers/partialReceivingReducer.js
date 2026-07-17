import { UPDATE_RECEIVING_BIN_LOCATIONS, UPDATE_RECEIVING_HEADER } from 'actions/types';

const initialState = {
  headerInfo: [],
  isShipmentFromPurchaseOrder: false,
  shipmentNumber: null,
  binLocations: [],
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
        shipmentNumber: action.payload.shipmentNumber,
      };

    case UPDATE_RECEIVING_BIN_LOCATIONS:
      return {
        ...state,
        binLocations: action.payload.binLocations,
      };

    default:
      return state;
  }
}
