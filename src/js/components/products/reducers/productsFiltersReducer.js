import { FETCH_PRODUCTS_CATALOGS, FETCH_PRODUCTS_CATEGORIES, FETCH_PRODUCTS_TAGS } from 'components/products/actions/types';

const initialState = {
  categories: [],
  catalogs: [],
  tags: [],
};

export default function productsFiltersReducer(state = initialState, action) {
  switch (action.type) {
    case FETCH_PRODUCTS_CATEGORIES:
      return {
        ...state,
        categories: action.payload,
      };
    case FETCH_PRODUCTS_CATALOGS:
      return {
        ...state,
        catalogs: action.payload,
      };
    case FETCH_PRODUCTS_TAGS:
      return {
        ...state,
        tags: action.payload,
      };
    default:
      return state;
  }
}
