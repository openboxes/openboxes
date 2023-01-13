import _ from 'lodash';

import { FETCH_REASONCODES } from 'actions/types';

const initialState = {
  data: [],
  fetched: false,
  sessionVersion: 0,
};

export default function (state = initialState, action) {
  switch (action.type) {
    case FETCH_REASONCODES:
      if (action.payload !== undefined) {
        const reasonCodes = _.map(action.payload.data, reasonCode => (
          { value: reasonCode.id, id: reasonCode.id, label: reasonCode.name }
        ));
        return {
          ...state,
          data: reasonCodes,
          fetched: true,
          sessionVersion: action.payload.sessionVersion,
        };
      }
      return state;
    default:
      return state;
  }
}
