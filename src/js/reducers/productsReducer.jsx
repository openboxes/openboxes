import _ from 'lodash';
import { FETCH_PRODUCTS } from '../actions/types';

const initialState = {
  data: [],
  fetched: false,
};

export default function (state = initialState, action) {
  switch (action.type) {
    case FETCH_PRODUCTS:
      if (action.payload.data !== undefined) {
        const products = _.map(action.payload.data.data, product => (
          { value: product, label: product.name }
        ));
        return { ...state, data: products, fetched: true };
      }
      return state;
    default:
      return state;
  }
}
