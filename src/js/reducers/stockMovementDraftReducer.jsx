import _ from 'lodash';

import {
  ADD_STOCK_MOVEMENT_DRAFT,
  REMOVE_STOCK_MOVEMENT_DRAFT,
} from 'actions/types';

export default function (state = {}, action) {
  switch (action.type) {
    case ADD_STOCK_MOVEMENT_DRAFT:
      return {
        ...state,
        [action.payload.id]: {
          id: action.payload.id,
          lineItems: action.payload.lineItems,
          lastUpdated: new Date(),
          statusCode: action.payload.statusCode,
        },
      };
    case REMOVE_STOCK_MOVEMENT_DRAFT:
      return _.omit(state, [action.payload.id]);
    default:
      return state;
  }
}
