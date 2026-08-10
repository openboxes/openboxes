import {
  REMOVE_RECEIVING_PUTAWAY_ENABLED,
  UPDATE_RECEIVING_BIN_LOCATIONS,
  UPDATE_RECEIVING_HEADER,
  UPDATE_RECEIVING_PUTAWAY_ENABLED,
  UPDATE_RECEIVING_VIEW,
} from 'actions/types';
import { ReceivingView } from 'consts/receivingViewOptions';

const initialState = {
  headerInfo: [],
  isShipmentFromPurchaseOrder: false,
  shipmentId: null,
  shipmentNumber: null,
  shipmentDetails: {},
  // Bin locations keyed by the shipment they were fetched for. The slice is persisted, so the
  // key tells whether they belong to the receiving being opened or to an earlier one.
  binLocationsByShipmentId: {},
  view: ReceivingView.TABLE,
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
        shipmentId: action.payload.shipmentId,
        shipmentNumber: action.payload.shipmentNumber,
        shipmentDetails: action.payload.shipmentDetails,
      };

    case UPDATE_RECEIVING_BIN_LOCATIONS:
      return {
        ...state,
        // Only the current shipment keeps its bin locations and the
        // state doesn't grow with every receiving opened.
        binLocationsByShipmentId: {
          [action.payload.shipmentId]: action.payload.binLocations,
        },
      };

    case UPDATE_RECEIVING_VIEW:
      return {
        ...state,
        view: action.payload.view,
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
