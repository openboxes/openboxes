import { FETCH_REQUISITION_STATUS_CODES } from 'actions/types';

const initialState = {
  data: [],
  availableStatuses: [],
  fetched: false,
};

export default function (state = initialState, action) {
  switch (action.type) {
    case FETCH_REQUISITION_STATUS_CODES:
      if (action.payload) {
        return {
          data: action.payload.data,
          availableStatuses: action.payload.availableStatuses,
          fetched: true,
        };
      }
      return state;
    default:
      return state;
  }
}
