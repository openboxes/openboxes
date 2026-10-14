import { act, renderHook } from '@testing-library/react-hooks';

import { FETCH_ATTRIBUTES } from 'actions/types';
import { ATTRIBUTES } from 'api/urls';
import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import useProductSupplierAttributes from 'hooks/productSupplier/form/useProductSupplierAttributes';
import apiClient from 'utils/apiClient';

const mockDispatch = jest.fn();
let mockState;
jest.mock('react-redux', () => ({
  useSelector: jest.fn((selector) => selector(mockState)),
  useDispatch: () => mockDispatch,
  connect: jest.fn(() => (Component) => Component),
}));
// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());
jest.mock('hooks/useTranslate', () => () => (id, defaultMessage) => defaultMessage);
jest.mock('components/form-elements/v2/SelectField', () => 'SelectField');
jest.mock('components/form-elements/v2/TextInput', () => 'TextInput');

const selectNoOther = {
  id: 'a1', name: 'Color', options: ['Red', 'Blue'], allowOther: false,
};
const selectEmpty = {
  id: 'a2', name: 'Size', options: [], allowOther: false,
};
const textOnly = {
  id: 'a3', name: 'Note', options: [], allowOther: true, required: true,
};
const selectWithOther = {
  id: 'a4', name: 'Grade', options: ['A'], allowOther: true,
};
const allAttributes = [selectNoOther, selectEmpty, textOnly, selectWithOther];

describe('useProductSupplierAttributes', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockState = { productSupplier: { attributes: allAttributes } };
  });

  it('fetches product supplier attributes and aborts the request on unmount', async () => {
    const { unmount } = renderHook(() => useProductSupplierAttributes());
    apiClient.get.mockResolvedValue({ data: { data: ['attr'] } });
    const innerDispatch = jest.fn();

    await mockDispatch.mock.calls[0][0](innerDispatch);

    const [url, config] = apiClient.get.mock.calls[0];
    expect(url).toBe(ATTRIBUTES);
    expect(config.params).toEqual({ entityType: 'PRODUCT_SUPPLIER' });
    expect(innerDispatch).toHaveBeenCalledWith({ type: FETCH_ATTRIBUTES, payload: ['attr'] });

    unmount();
    expect(config.signal.aborted).toBe(true);
  });

  it('picks an input type for every attribute', () => {
    const { result } = renderHook(() => useProductSupplierAttributes());
    const [noOther, empty, text, withOther] = result.current.attributesWithInputTypes;

    expect(noOther.Input).toBe(SelectField);
    expect(noOther.inputParams).toBeUndefined();
    expect(noOther.attribute.options).toEqual([
      { id: 'a1', value: 'Red', label: 'Red' },
      { id: 'a1', value: 'Blue', label: 'Blue' },
    ]);
    expect(empty.Input).toBe(SelectField);
    expect(empty.inputParams.createNewFromModalLabel).toBe('Other');
    expect(text.Input).toBe(TextInput);
    expect(withOther.Input).toBe(SelectField);
    expect(withOther.attribute.options).toEqual([{ id: 'a4', value: 'A', label: 'A' }]);
    expect(withOther.inputParams.createNewFromModal).toBe(true);
  });

  it('opens the new value modal for the attribute and closes it', () => {
    const { result } = renderHook(() => useProductSupplierAttributes());

    act(() => result.current.attributesWithInputTypes[3].inputParams.newOptionModalOpen());

    expect(result.current.isAttributeModalOpen).toBe(true);
    expect(result.current.selectedAttribute).toBe(selectWithOther);

    act(() => result.current.closeAttributeModal());
    expect(result.current.isAttributeModalOpen).toBe(false);
  });

  it('classifies attributes as select or text types', () => {
    const { result } = renderHook(() => useProductSupplierAttributes());
    const { isSelectType, isTextType } = result.current;

    expect(allAttributes.map((a) => Boolean(isSelectType(a))))
      .toEqual([true, true, false, true]);
    expect(allAttributes.map((a) => Boolean(isTextType(a))))
      .toEqual([false, false, true, false]);
  });

  it('maps fetched values to select options or plain text by attribute type', () => {
    const { result } = renderHook(() => useProductSupplierAttributes());

    expect(result.current.mapFetchedAttributes([
      { attribute: { id: 'a1' }, value: 'Red' },
      { attribute: { id: 'a3' }, value: 'free text' },
    ])).toEqual({
      a1: { id: 'a1', value: 'Red', label: 'Red' },
      a3: 'free text',
    });
  });

  it('builds required and optional schemas for text and select inputs', () => {
    const { result } = renderHook(() => useProductSupplierAttributes());
    const errorProps = { required_error: 'Note is required', invalid_type_error: 'Note is required' };
    const { inputTypeSchema, selectTypeSchema } = result.current;

    const requiredText = inputTypeSchema(textOnly, errorProps);
    expect(requiredText.safeParse('   ').error.issues[0].message).toBe('Note is required');
    expect(requiredText.safeParse('x'.repeat(256)).error.issues[0].message).toBe('Max length of Note is 255');
    expect(requiredText.safeParse(' ok ').data).toBe('ok');
    expect(inputTypeSchema({ name: 'Note' }).safeParse(undefined).success).toBe(true);

    const requiredSelect = selectTypeSchema({ required: true }, errorProps);
    expect(requiredSelect.safeParse(null).error.issues[0].message).toBe('Note is required');
    expect(requiredSelect.safeParse({ value: 'a', label: 'a' }).success).toBe(true);
    expect(selectTypeSchema({}).safeParse(null).success).toBe(true);
  });
});
