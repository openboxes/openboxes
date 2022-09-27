import { FETCH_PRODUCTS_FAIL, FETCH_PRODUCTS_START, FETCH_PRODUCTS_SUCCESS } from 'components/products/actions/types';

const INITIAL_STATE = {
  productsData: [],
  loading: false,
  pages: -1,
  totalCount: 0,
  currentParams: {},
};

export default function productsListReducer(state = INITIAL_STATE, action) {
  switch (action.type) {
    case FETCH_PRODUCTS_START:
      return {
        ...state,
        loading: true,
      };
    case FETCH_PRODUCTS_SUCCESS:
      return {
        ...state,
        productsData: action.payload.productsData,
        loading: false,
        pages: action.payload.pages,
        totalCount: action.payload.totalCount,
        currentParams: action.payload.currentParams,
      };
    case FETCH_PRODUCTS_FAIL:
      return {
        ...state,
        loading: false,
      };
    default:
      return state;
  }
}
