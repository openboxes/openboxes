import {
  REMOVE_RECEIVING_PUTAWAY_ENABLED,
  UPDATE_RECEIVING_BIN_LOCATIONS,
  UPDATE_RECEIVING_DATE_DELIVERED,
  UPDATE_RECEIVING_HEADER,
  UPDATE_RECEIVING_PUTAWAY_ENABLED,
  UPDATE_RECEIVING_VIEW,
} from 'actions/types';
import { ReceivingView } from 'consts/receivingViewOptions';

const initialState = {
  headerInfo: [],
  isShipmentFromPurchaseOrder: false,
  shipmentNumber: null,
  shipmentDetails: {},
  binLocations: [],
  view: ReceivingView.TABLE,
  // The delivery date entered on the check step, kept per shipment so it survives a trip back
  // to the receiving step without prop drilling.
  dateDeliveredByShipment: {},
  // The putaway toggle is remembered per receiving, keyed by receipt id.
  putawayEnabledByReceipt: {},
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
        shipmentDetails: action.payload.shipmentDetails,
      };

    case UPDATE_RECEIVING_BIN_LOCATIONS:
      return {
        ...state,
        binLocations: action.payload.binLocations,
      };

    case UPDATE_RECEIVING_VIEW:
      return {
        ...state,
        view: action.payload.view,
      };

    case UPDATE_RECEIVING_DATE_DELIVERED:
      return {
        ...state,
        dateDeliveredByShipment: {
          ...state.dateDeliveredByShipment,
          [action.payload.shipmentId]: action.payload.dateDelivered,
        },
      };

    case UPDATE_RECEIVING_PUTAWAY_ENABLED:
      return {
        ...state,
        putawayEnabledByReceipt: {
          ...state.putawayEnabledByReceipt,
          [action.payload.receiptId]: action.payload.putawayEnabled,
        },
      };

    case REMOVE_RECEIVING_PUTAWAY_ENABLED: {
      const {
        [action.payload.receiptId]: removed,
        ...putawayEnabledByReceipt
      } = state.putawayEnabledByReceipt;
      return {
        ...state,
        putawayEnabledByReceipt,
      };
    }

    default:
      return state;
  }
}
