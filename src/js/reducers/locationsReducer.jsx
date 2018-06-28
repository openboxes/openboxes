import _ from 'lodash';
import { FETCH_LOCATIONS } from '../actions/types';

const initialState = {
  data: [],
  fetched: false,
};

export default function (state = initialState, action) {
  switch (action.type) {
    case FETCH_LOCATIONS:
      if (action.payload.data !== undefined) {
        const locations = _.map(action.payload.data.data, location => (
          {
            value: { id: location.id, type: location.locationTypeCode, name: location.name },
            label: `${location.name} [${location.locationTypeCode}]`,
          }
        ));
        return { ...state, data: locations, fetched: true };
      }
      return state;
    default:
      return state;
  }
}
