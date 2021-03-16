import _ from 'lodash';
import { FETCH_ORGANIZATIONS } from '../actions/types';

const initialState = {
  data: [],
  fetched: false,
};

export default function (state = initialState, action) {
  switch (action.type) {
    case FETCH_ORGANIZATIONS:
      if (action.payload.data !== undefined) {
        const organizations = _.map(action.payload.data.data, organization => (
          { value: organization.id, label: organization.name }
        ));
        return { ...state, data: organizations, fetched: true };
      }
      return state;
    default:
      return state;
  }
}
