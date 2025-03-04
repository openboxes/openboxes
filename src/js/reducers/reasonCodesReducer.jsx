import { FETCH_REASONCODES } from 'actions/types';

const initialState = {
  data: [],
  fetched: false,
};

export default function (state = initialState, action) {
  switch (action.type) {
    case FETCH_REASONCODES:
      return {
        ...state,
        data: action.payload,
        fetched: true,
      };
    default:
      return state;
  }
}
