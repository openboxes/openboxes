import { FETCH_UNIT_OF_MEASURE_CURRENCY, FETCH_UNIT_OF_MEASURE_QUANTITY } from 'actions/types';

const initialState = {
  currency: [],
  quantity: [],
};

export default function (state = initialState, action) {
  switch (action.type) {
    case FETCH_UNIT_OF_MEASURE_CURRENCY:
      if (action.payload !== undefined) {
        const unitsOfMeasure = action.payload.data;
        return { ...state, currency: unitsOfMeasure };
      }
      return state;
    case FETCH_UNIT_OF_MEASURE_QUANTITY:
      if (action.payload !== undefined) {
        const unitsOfMeasure = action.payload.data;
        return { ...state, quantity: unitsOfMeasure };
      }
      return state;
    default:
      return state;
  }
}
