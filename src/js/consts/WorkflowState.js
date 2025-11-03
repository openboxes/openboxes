const OutboundWorkflowState = {
  CREATE_HEADER: 1,
  ADD_ITEMS: 2,
  REVISE_ITEMS: 3,
  PICK_ITEMS: 4,
  PACK_ITEMS: 5,
  SEND_SHIPMENT: 6,
};

const InboundWorkflowState = {
  CREATE_HEADER: 1,
  ADD_ITEMS: 2,
  SEND_SHIPMENT: 6,
};

// eslint-disable-next-line import/prefer-default-export
export { InboundWorkflowState, OutboundWorkflowState };
