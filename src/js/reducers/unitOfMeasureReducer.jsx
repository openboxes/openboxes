import _ from 'lodash';

import { FETCH_UNIT_OF_MEASURE } from 'actions/types';

const initialState = {
  unitsOfMeasure: [],
};

export default function (state = initialState, action) {
  switch (action.type) {
    case FETCH_UNIT_OF_MEASURE:
      if (action.payload !== undefined) {
        const unitsOfMeasure = action.payload.data;
        return { ...state, unitsOfMeasure };
      }
      return state;
    default:
      return state;
  }
}
