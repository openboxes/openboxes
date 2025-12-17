import { getTranslate } from 'react-localize-redux';
import { getCountWorkflowEntityById } from 'selectors';

import { setErrorsById } from 'actions';
import { UPDATE_FIELD_VALUE } from 'actions/types';
import cycleCountSchemes from 'schemes/cycleCountSchemes';
import { translateWithDefaultMessage } from 'utils/Translate';

const validateCycleCount = (store, cycleCountId) => {
  const { dispatch, getState } = store;
  const state = getState();
  const translate = translateWithDefaultMessage(getTranslate(state.localize));
  const countEntitySchema = cycleCountSchemes.getCountEntitySchema(translate);
  const cycleCount = getCountWorkflowEntityById(state, cycleCountId);
  const parsedSchema = countEntitySchema.safeParse(cycleCount);
  dispatch(setErrorsById(cycleCountId, parsedSchema.error?.format()));
};

/**
 * Middleware to validate cycle count on field value update. It's done here because
 * we need to synchronize dispatch of the validation errors with the field value update.
 * Additionally, this approach reduces the number of validations caused by re-renders.
 * It's also beneficial for UI responsiveness as the UI doesn't need to wait for validation
 * to complete before updating the field value in the store. The validation takes longer because
 * we can't validate only one field / row, due to the fact that error can be displayed based
 * on multiple fields (e.g. unique bin locations across all items).
*/
const validateCycleCountHandler = (store) => (next) => (action) => {
  const result = next(action);

  if (action.type === UPDATE_FIELD_VALUE) {
    validateCycleCount(store, action.payload.id);
  }

  return result;
};

export default { validateCycleCountHandler };
