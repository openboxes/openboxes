import { FILTER_FORM_PARAMS_BUILT, REBUILD_FILTER_FORM_PARAMS } from 'actions/types';

const initialState = {
  shouldRebuildParams: false,
};

export default function (state = initialState, action) {
  switch (action.type) {
    case REBUILD_FILTER_FORM_PARAMS:
      return {
        ...state,
        shouldRebuildParams: true,
      };
    case FILTER_FORM_PARAMS_BUILT:
      return {
        ...state,
        shouldRebuildParams: false,
      };
    default:
      return state;
  }
}

