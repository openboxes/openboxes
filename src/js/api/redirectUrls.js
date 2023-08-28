const CONTEXT_PATH = '/openboxes';
const STOCK_MOVEMENT = `${CONTEXT_PATH}/stockMovement`;

// REQUESTS
export const CREATE_REQUEST = `${STOCK_MOVEMENT}/createRequest`;
export const EDIT_REQUEST = id => `${CREATE_REQUEST}/${id}`;

export const VERIFY_REQUEST = id => `${CONTEXT_PATH}/verifyRequest/${id}`;

// LOCATION CONFIGURATION
const LOCATION_CONFIGURATION = `${CONTEXT_PATH}/locationsConfiguration`;
export const LOCATION_CREATE = id => `${LOCATION_CONFIGURATION}/create/${id}`;
