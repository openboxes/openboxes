import { FETCH_LOT_NUMBERS_BY_PRODUCT_IDS } from 'actions/types';

const initialState = {
  lotNumbers: {},
};

export default function lotNumbersReducer(state = initialState, action) {
  switch (action.type) {
    case FETCH_LOT_NUMBERS_BY_PRODUCT_IDS: {
      const lotNumbers = action.payload?.reduce((acc, product) => {
        acc[product.productId] = product.lotNumbers;
        return acc;
      }, {});
      return {
        lotNumbers: {
          ...state.lotNumbers,
          ...lotNumbers,
        },
      };
    }
    default:
      return state;
  }
}
