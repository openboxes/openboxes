import _ from 'lodash';
import { FETCH_CURRENCIES } from '../actions/types';

const initialState = {
  data: [],
  fetched: false,
};

export default function (state = initialState, action) {
  switch (action.type) {
    case FETCH_CURRENCIES:
      if (action.payload.data !== undefined) {
        const currencies = _.map(action.payload.data.data, currency => (
          { value: currency.id, label: currency.name }
        ));
        return { ...state, data: currencies, fetched: true };
      }
      return state;
    default:
      return state;
  }
}
