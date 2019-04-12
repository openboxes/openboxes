import _ from 'lodash';
import { FETCH_REASONCODES } from '../actions/types';

const initialState = {
  data: [],
  fetched: false,
};

export default function (state = initialState, action) {
  switch (action.type) {
    case FETCH_REASONCODES:
      if (action.payload.data !== undefined) {
        const reasonCodes = _.map(action.payload.data.data, reasonCode => (
          { value: reasonCode.id, label: reasonCode.name }
        ));
        return { ...state, data: reasonCodes, fetched: true };
      }
      return state;
    default:
      return state;
  }
}
