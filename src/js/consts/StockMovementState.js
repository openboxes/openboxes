/**
 * These enums represent the individual steps in the inbound and outbound
 * stock movement workflows. The numeric values correspond to specific workflow
 * steps that are included in URL parameters sent to the backend.
 */

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
