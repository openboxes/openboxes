import { renderHook } from '@testing-library/react-hooks';

import useInboundAddItemsActions from 'hooks/inboundV2/addItems/useInboundAddItemsActions';
import useInboundAddItemsColumns from 'hooks/inboundV2/addItems/useInboundAddItemsColumns';
import useInboundAddItemsForm from 'hooks/inboundV2/addItems/useInboundAddItemsForm';
import useInboundAddItemsImportExport from 'hooks/inboundV2/addItems/useInboundAddItemsImportExport';
import useHandleModalAction from 'hooks/useHandleModalAction';
import useTranslation from 'hooks/useTranslation';

jest.mock('hooks/useTranslation', () => jest.fn());

const mockFormState = {
  control: { id: 'control' },
  getValues: jest.fn(),
  errors: { values: {} },
  trigger: jest.fn(),
  setValue: jest.fn(),
  defaultTableRow: [{ sortOrder: 100 }],
};
jest.mock('hooks/inboundV2/addItems/useInboundAddItemsFormState', () => jest.fn(() => mockFormState));
const mockModal = {
  isOpen: true,
  data: [{ code: 'A' }],
  type: 'duplicates',
  openModal: jest.fn(),
  handleResponse: jest.fn(),
};
jest.mock('hooks/useHandleModalAction', () => jest.fn(() => mockModal));
const mockActions = {
  loading: false,
  lineItemsArrayFields: [{ id: 'f-1' }],
  addNewLine: jest.fn(),
  removeRow: jest.fn(),
  removeAllRows: jest.fn(),
  saveRequisitionItemsInCurrentStep: jest.fn(),
  fetchLineItems: jest.fn(),
  removeSavedRow: jest.fn(),
  nextPage: jest.fn(),
  previousPage: jest.fn(),
  save: jest.fn(),
  saveAndExit: jest.fn(),
};
jest.mock('hooks/inboundV2/addItems/useInboundAddItemsActions', () => jest.fn(() => mockActions));
const mockImportExport = { importTemplate: jest.fn(), exportTemplate: jest.fn() };
jest.mock('hooks/inboundV2/addItems/useInboundAddItemsImportExport', () => jest.fn(() => mockImportExport));
jest.mock('hooks/inboundV2/addItems/useInboundAddItemsColumns', () => jest.fn(() => ({ columns: ['column'] })));

describe('useInboundAddItemsForm', () => {
  const next = jest.fn();
  const previous = jest.fn();

  it('wires the form state, actions, import/export and columns together', () => {
    const { result } = renderHook(() => useInboundAddItemsForm({ next, previous }));

    expect(useTranslation).toHaveBeenCalledWith('stockMovement');
    expect(useHandleModalAction).toHaveBeenCalled();
    expect(useInboundAddItemsActions).toHaveBeenCalledWith({
      control: mockFormState.control,
      getValues: mockFormState.getValues,
      setValue: mockFormState.setValue,
      trigger: mockFormState.trigger,
      defaultTableRow: mockFormState.defaultTableRow,
      next,
      previous,
      openModal: mockModal.openModal,
    });
    expect(useInboundAddItemsImportExport).toHaveBeenCalledWith({
      getValues: mockFormState.getValues,
      fetchLineItems: mockActions.fetchLineItems,
      saveRequisitionItemsInCurrentStep: mockActions.saveRequisitionItemsInCurrentStep,
    });
    expect(useInboundAddItemsColumns).toHaveBeenCalledWith({
      errors: mockFormState.errors,
      control: mockFormState.control,
      removeSavedRow: mockActions.removeSavedRow,
      trigger: mockFormState.trigger,
      getValues: mockFormState.getValues,
      setValue: mockFormState.setValue,
      removeRow: mockActions.removeRow,
      addNewLine: mockActions.addNewLine,
    });

    expect(result.current.form)
      .toEqual({ control: mockFormState.control, errors: mockFormState.errors });
    expect(result.current.table).toEqual({ lineItemsArrayFields: [{ id: 'f-1' }], columns: ['column'] });
    expect(result.current.actions).toMatchObject({
      loading: false,
      addNewLine: mockActions.addNewLine,
      removeAllRows: mockActions.removeAllRows,
      nextPage: mockActions.nextPage,
      previousPage: mockActions.previousPage,
      save: mockActions.save,
      saveAndExit: mockActions.saveAndExit,
    });
    expect(result.current.modal).toEqual({
      isModalOpen: true,
      modalData: [{ code: 'A' }],
      modalType: 'duplicates',
      handleModalResponse: mockModal.handleResponse,
    });
    expect(result.current.importExport).toEqual(mockImportExport);
  });
});
