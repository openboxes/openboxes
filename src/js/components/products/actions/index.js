import { FETCH_PRODUCTS_CATALOGS, FETCH_PRODUCTS_CATEGORIES, FETCH_PRODUCTS_TAGS } from 'components/products/actions/types';
import apiClient from 'utils/apiClient';

export function fetchProductsCategories(dispatch) {
  apiClient.get('/openboxes/api/categoryOptions').then((res) => {
    dispatch({
      type: FETCH_PRODUCTS_CATEGORIES,
      payload: res.data.data,
    });
  });
}

export function fetchProductsCatalogs(dispatch) {
  apiClient.get('/openboxes/api/catalogOptions').then((res) => {
    dispatch({
      type: FETCH_PRODUCTS_CATALOGS,
      payload: res.data.data,
    });
  });
}

export function fetchProductsTags(dispatch) {
  apiClient.get('/openboxes/api/tagOptions').then((res) => {
    dispatch({
      type: FETCH_PRODUCTS_TAGS,
      payload: res.data.data,
    });
  });
}
