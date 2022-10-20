import _ from 'lodash';

import { FETCH_BUYERS, FETCH_ORGANIZATIONS, FETCH_SUPPLIERS } from 'actions/types';

const initialState = {
  data: [],
  fetched: false,
  suppliers: [],
  buyers: null,
};

export default function (state = initialState, action) {
  switch (action.type) {
    case FETCH_ORGANIZATIONS:
      if (action.payload.data !== undefined) {
        const organizations = _.map(action.payload.data.data, organization => (
          { value: organization.id, id: organization.id, label: organization.name }
        ));
        return { ...state, data: organizations, fetched: true };
      }
      return state;
    case FETCH_SUPPLIERS:
      return {
        ...state,
        suppliers: action.payload,
      };
    case FETCH_BUYERS:
      return {
        ...state,
        buyers: action.payload,
      };
    default:
      return state;
  }
}
