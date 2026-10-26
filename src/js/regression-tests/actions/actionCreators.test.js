// Not covered on OBAF (OBAF lacks the code): the receiving action
// creators (updateReceivingHeader, updateReceivingBinLocations, updateReceivingView,
// updateReceivingSort, resetReceivingSort, updateReceivingDateDelivered,
// updateReceivingPutawayEnabled, removeReceivingPutawayEnabled) and their it.each rows,
// plus 'updateReceivingHeader picks the header fields of the shipment'.
import configureStore from 'redux-mock-store';
import thunk from 'redux-thunk';

import {
  addEmptyRow,
  addStockMovementDraft,
  clearCountWorkflow,
  clearErrorsData,
  closeInfoBar,
  createInfoBar,
  eraseDraft,
  hideInfoBar,
  hideInfoBarModal,
  hideSpinner,
  hideUserActions,
  markAllItemsAsNotUpdated,
  markAllItemsAsUpdated,
  removeStockMovementDraft,
  reorderIndicators,
  resetIndicators,
  setErrors,
  setErrorsById,
  setOffline,
  setOnline,
  setScrollToBottom,
  setShouldRebuildFilterParams,
  setUpdated,
  showInfoBar,
  showInfoBarModal,
  showSpinner,
  showUserActions,
  submitForm,
  updateCountedBy,
  updateDateCounted,
  updateFieldValue,
  updateWorkflowHeader,
} from 'actions';
import {
  ADD_EMPTY_ROW,
  ADD_INFO_BAR,
  ADD_STOCK_MOVEMENT_DRAFT,
  CLEAR_COUNT_WORKFLOW,
  CLEAR_ERRORS_DATA,
  CLOSE_INFO_BAR,
  ERASE_DRAFT,
  FILTER_FORM_PARAMS_BUILT,
  HIDE_INFO_BAR,
  HIDE_INFO_BAR_MODAL,
  HIDE_SPINNER,
  MARK_ALL_AS_UPDATED,
  REBUILD_FILTER_FORM_PARAMS,
  REMOVE_FROM_INDICATORS,
  REMOVE_STOCK_MOVEMENT_DRAFT,
  REORDER_INDICATORS,
  RESET_INDICATORS,
  SET_ERRORS,
  SET_ERRORS_BY_ID,
  SET_OFFLINE,
  SET_ONLINE,
  SET_SCROLL_TO_BOTTOM,
  SET_UPDATED,
  SHOW_INFO_BAR,
  SHOW_INFO_BAR_MODAL,
  SHOW_SPINNER,
  SUBMIT_FORM,
  TOGGLE_USER_ACTION_MENU,
  UPDATE_COUNTED_BY,
  UPDATE_DATE_COUNTED,
  UPDATE_FIELD_VALUE,
  UPDATE_WORKFLOW_HEADER,
} from 'actions/types';
import { TO_COUNT_TAB } from 'consts/cycleCount';
import { InfoBar } from 'consts/infoBar';

// No request is made here; the mock only keeps the real client (and the store it imports) out.
jest.mock('utils/apiClient', () => ({
  __esModule: true,
  default: {
    get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn(),
  },
  parseResponse: jest.fn(),
}));

const createStore = () => configureStore([thunk])({});

describe('plain action creators', () => {
  it.each([
    ['showSpinner', showSpinner(), { type: SHOW_SPINNER, payload: true }],
    ['hideSpinner', hideSpinner(), { type: HIDE_SPINNER, payload: false }],
    ['setOnline', setOnline(), { type: SET_ONLINE }],
    ['setOffline', setOffline(), { type: SET_OFFLINE }],
    ['showUserActions', showUserActions(), { type: TOGGLE_USER_ACTION_MENU, payload: true }],
    ['hideUserActions', hideUserActions(), { type: TOGGLE_USER_ACTION_MENU, payload: false }],
    ['resetIndicators', resetIndicators(), { type: RESET_INDICATORS }],
    ['hideInfoBar', hideInfoBar('BAR'), { type: HIDE_INFO_BAR, payload: { name: 'BAR' } }],
    ['closeInfoBar', closeInfoBar('BAR'), { type: CLOSE_INFO_BAR, payload: { name: 'BAR' } }],
    ['showInfoBar', showInfoBar('BAR'), { type: SHOW_INFO_BAR, payload: { name: 'BAR' } }],
    ['showInfoBarModal', showInfoBarModal('BAR'), { type: SHOW_INFO_BAR_MODAL, payload: { name: 'BAR' } }],
    ['hideInfoBarModal', hideInfoBarModal('BAR'), { type: HIDE_INFO_BAR_MODAL, payload: { name: 'BAR' } }],
    ['setScrollToBottom', setScrollToBottom(true), { type: SET_SCROLL_TO_BOTTOM, payload: true }],
    ['eraseDraft', eraseDraft('loc1', TO_COUNT_TAB), {
      type: ERASE_DRAFT, payload: { locationId: 'loc1', tab: TO_COUNT_TAB },
    }],
    ['updateWorkflowHeader', updateWorkflowHeader([{ text: 'h' }], 'PENDING'), {
      type: UPDATE_WORKFLOW_HEADER, payload: { headerInfo: [{ text: 'h' }], headerStatus: 'PENDING' },
    }],
  ])('%s', (name, action, expected) => {
    expect(action).toEqual(expected);
  });

  it('createInfoBar keeps only the known bar fields and always shows the bar', () => {
    const redirect = { label: 'try', link: '/x' };

    expect(createInfoBar({
      name: InfoBar.FULL_OUTBOUND_IMPORT,
      versionLabel: { label: 'v' },
      title: { label: 't' },
      isCloseable: true,
      hasModalToDisplay: false,
      redirect,
      unrelated: 'dropped',
    })).toEqual({
      type: ADD_INFO_BAR,
      payload: {
        name: InfoBar.FULL_OUTBOUND_IMPORT,
        versionLabel: { label: 'v' },
        title: { label: 't' },
        isCloseable: true,
        hasModalToDisplay: false,
        redirect,
        show: true,
      },
    });
  });

  describe('reorderIndicators', () => {
    it('removes the indicator when it is dropped on the archive', () => {
      expect(reorderIndicators({ oldIndex: 2, newIndex: 0 }, { target: { id: 'archive' } }, 'graph'))
        .toEqual({ type: REMOVE_FROM_INDICATORS, payload: { index: 2, type: 'graph' } });
    });

    it('moves the indicator when it is dropped anywhere else', () => {
      expect(reorderIndicators({ oldIndex: 2, newIndex: 0 }, { target: { id: 'number-3' } }, 'number'))
        .toEqual({ type: REORDER_INDICATORS, payload: { oldIndex: 2, newIndex: 0, type: 'number' } });
    });
  });
});

describe('thunks that only dispatch', () => {
  it('setShouldRebuildFilterParams asks for a rebuild by default and resets on false', () => {
    const store = createStore();

    store.dispatch(setShouldRebuildFilterParams());
    store.dispatch(setShouldRebuildFilterParams(false));

    expect(store.getActions()).toEqual([
      { type: REBUILD_FILTER_FORM_PARAMS },
      { type: FILTER_FORM_PARAMS_BUILT },
    ]);
  });

  it('addStockMovementDraft keeps only the draft fields', () => {
    const store = createStore();

    store.dispatch(addStockMovementDraft({
      workflow: 'outbound', lineItems: [{ id: 'li1' }], id: 'sm1', statusCode: 'PENDING', extra: 1,
    }));
    store.dispatch(removeStockMovementDraft('sm1'));

    expect(store.getActions()).toEqual([
      {
        type: ADD_STOCK_MOVEMENT_DRAFT,
        payload: {
          workflow: 'outbound', lineItems: [{ id: 'li1' }], statusCode: 'PENDING', id: 'sm1',
        },
      },
      { type: REMOVE_STOCK_MOVEMENT_DRAFT, payload: { id: 'sm1' } },
    ]);
  });

  it('setUpdated and addEmptyRow target one cycle count', () => {
    const store = createStore();

    store.dispatch(setUpdated('cc1', false));
    store.dispatch(addEmptyRow('cc1'));

    expect(store.getActions()).toEqual([
      { type: SET_UPDATED, payload: { id: 'cc1', updated: false } },
      { type: ADD_EMPTY_ROW, payload: { id: 'cc1' } },
    ]);
  });

  it.each([
    ['updateDateCounted', updateDateCounted('cc1', '2026-01-01'), {
      type: UPDATE_DATE_COUNTED, payload: { id: 'cc1', dateCounted: '2026-01-01' },
    }],
    ['updateCountedBy', updateCountedBy('cc1', { id: 'u1' }), {
      type: UPDATE_COUNTED_BY, payload: { id: 'cc1', countedBy: { id: 'u1' } },
    }],
    ['updateFieldValue', updateFieldValue({
      cycleCountId: 'cc1', rowId: 'i1', field: 'quantityCounted', value: 3,
    }), {
      type: UPDATE_FIELD_VALUE,
      payload: {
        id: 'cc1', rowId: 'i1', field: 'quantityCounted', value: 3,
      },
    }],
  ])('%s records the change and marks the cycle count as updated', (name, thunkAction, expected) => {
    const store = createStore();

    store.dispatch(thunkAction);

    expect(store.getActions()).toEqual([
      expected,
      { type: SET_UPDATED, payload: { id: 'cc1', updated: true } },
    ]);
  });

  it('markAllItemsAsUpdated and markAllItemsAsNotUpdated set the flag on every listed cycle count', () => {
    const store = createStore();

    store.dispatch(markAllItemsAsUpdated(['cc1', 'cc2']));
    store.dispatch(markAllItemsAsNotUpdated(['cc3']));

    expect(store.getActions()).toEqual([
      { type: MARK_ALL_AS_UPDATED, payload: { cycleCountIds: ['cc1', 'cc2'], updated: true } },
      { type: MARK_ALL_AS_UPDATED, payload: { cycleCountIds: ['cc3'], updated: false } },
    ]);
  });

  it('setErrors and setErrorsById pass the errors through', () => {
    const store = createStore();

    store.dispatch(setErrors({ cc1: { a: 'required' } }));
    store.dispatch(setErrorsById('cc2', { b: 'too big' }));

    expect(store.getActions()).toEqual([
      { type: SET_ERRORS, payload: { cc1: { a: 'required' } } },
      { type: SET_ERRORS_BY_ID, payload: { id: 'cc2', errors: { b: 'too big' } } },
    ]);
  });

  it('submitForm, clearErrorsData and clearCountWorkflow are called with dispatch itself', () => {
    const dispatch = jest.fn();

    submitForm(dispatch);
    clearErrorsData(dispatch);
    clearCountWorkflow(dispatch);

    expect(dispatch.mock.calls).toEqual([
      [{ type: SUBMIT_FORM }],
      [{ type: CLEAR_ERRORS_DATA }],
      [{ type: CLEAR_COUNT_WORKFLOW }],
    ]);
  });
});
