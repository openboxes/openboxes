import _ from 'lodash';
import { FETCH_USERS } from '../actions/types';

const initialState = {
  data: [],
  fetched: false,
};

export default function (state = initialState, action) {
  switch (action.type) {
    case FETCH_USERS:
      if (action.payload.data !== undefined) {
        const users = _.map(action.payload.data.data, user => (
          { value: user.id, label: user.name, name: user.name }
        ));
        return { ...state, data: users, fetched: true };
      }
      return state;
    default:
      return state;
  }
}
