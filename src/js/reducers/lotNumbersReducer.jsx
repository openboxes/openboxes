import { FETCH_LOT_NUMBERS_BY_PRODUCT_IDS } from 'actions/types';

const initialState = {
  lotNumbers: {},
};

export default function lotNumbersReducer(state = initialState, action) {
  switch (action.type) {
    case FETCH_LOT_NUMBERS_BY_PRODUCT_IDS: {
      return {
        ...state,
        lotNumbers: {
          ...state.lotNumbers,
          ...action.payload,
        },
      };
    }
    default:
      return state;
  }
}
