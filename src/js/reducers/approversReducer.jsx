import _ from 'lodash';

import { FETCH_APPROVERS } from 'actions/types';

const initialState = {
  data: [],
  fetched: false,
};

export default function (state = initialState, action) {
  switch (action.type) {
    case FETCH_APPROVERS: {
      const users = _.map(action.payload, (user) => (
        {
          value: user.id,
          id: user.id,
          label: user.name,
          name: user.name,
        }
      ));
      return {
        ...state,
        data: users,
        fetched: true,
      };
    }
    default:
      return state;
  }
}
