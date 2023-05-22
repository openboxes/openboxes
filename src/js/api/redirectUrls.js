const CONTEXT_PATH = '/openboxes';
const STOCK_MOVEMENT = `${CONTEXT_PATH}/stockMovement`;

export const CREATE_REQUEST = `${STOCK_MOVEMENT}/createRequest`;
export const EDIT_REQUEST = id => `${CREATE_REQUEST}/${id}`;

export const VERIFY_REQUEST = id => `${CONTEXT_PATH}/verifyRequest/${id}`;
