import _ from 'lodash';

import { FETCH_SESSION_INFO, CHANGE_CURRENT_LOCATION } from '../actions/types';

const initialState = {
  currentLocation: {
    id: '',
    name: '',
    hasBinLocationSupport: true,
    locationType: { description: '', locationTypeCode: '' },
    isSuperuser: false,
  },
};

export default function (state = initialState, action) {
  switch (action.type) {
    case FETCH_SESSION_INFO:
      return {
        ...state,
        currentLocation: _.get(action, 'payload.data.data.location'),
        isSuperuser: _.get(action, 'payload.data.data.isSuperuser'),
      };
    case CHANGE_CURRENT_LOCATION:
      return { ...state, currentLocation: action.payload };
    default:
      return state;
  }
}
