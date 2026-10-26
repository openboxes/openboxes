import { renderHook } from '@testing-library/react-hooks';

import useInboundAddItemsFormState from 'hooks/inboundV2/addItems/useInboundAddItemsFormState';

jest.mock('hooks/useTranslate', () => jest.fn(() => (id, defaultMessage) => defaultMessage || id));

const flushPromises = async () => {
  for (let i = 0; i < 20; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

const product = (overrides = {}) => ({
  id: 'p-1', value: 'p-1', label: 'P1 - Aspirin', ...overrides,
});

describe('useInboundAddItemsFormState', () => {
  // Sets the line items and returns the validation messages keyed by "row.field".
  const validate = async (lineItems) => {
    const { result } = renderHook(() => useInboundAddItemsFormState());
    result.current.setValue('values.lineItems', lineItems);
    const valid = await result.current.trigger();
    await flushPromises();
    const { errors } = result.current;
    const messages = {};
    (errors?.values?.lineItems || []).forEach((rowErrors, index) => {
      Object.entries(rowErrors || {}).forEach(([field, error]) => {
        messages[`${index}.${field}`] = error.message;
      });
    });
    return { valid, messages, result };
  };

  it('starts with one empty line and no saved lines', () => {
    const { result } = renderHook(() => useInboundAddItemsFormState());
    expect(result.current.defaultTableRow).toEqual([{
      palletName: '',
      boxName: '',
      product: undefined,
      lotNumber: '',
      expirationDate: '',
      quantityRequested: undefined,
      recipient: undefined,
      sortOrder: 100,
    }]);
    expect(result.current.getValues()).toEqual({
      currentLineItems: [],
      values: { lineItems: result.current.defaultTableRow },
    });
  });

  it('accepts complete lines and the empty default line', async () => {
    const { valid } = await validate([
      {
        palletName: 'P', boxName: 'B', product: product(), lotNumber: 'L', expirationDate: '01/Jan/2027', quantityRequested: 3,
      },
      {
        palletName: '', boxName: '', lotNumber: '', expirationDate: '',
      },
    ]);
    expect(valid).toBe(true);
  });

  it('reports each line rule on the right field', async () => {
    const { valid, messages } = await validate([
      { boxName: 'Box only', product: product(), quantityRequested: null },
      { expirationDate: '01/Jan/2027', lotNumber: '' },
      { product: product(), quantityRequested: -1 },
      { expirationDate: '31/Dec/1999', lotNumber: 'L' },
    ]);
    expect(valid).toBe(false);
    expect(messages).toEqual({
      '0.boxName': 'Please enter Pack level 1 before Pack level 2',
      '0.quantityRequested': 'Enter proper quantity',
      '1.lotNumber': 'Items with an expiry date must also have a lot number',
      '2.quantityRequested': 'Enter proper quantity',
      '3.expirationDate': 'This date is invalid. Please enter a date after 2000.',
    });
  });

  it('requires lot and expiry for lot-controlled products', async () => {
    const { messages } = await validate([
      { product: product({ lotAndExpiryControl: true }), quantityRequested: 1, lotNumber: '' },
    ]);
    expect(messages).toEqual({
      '0.expirationDate': 'Both lot number and expiry date are required for this item.',
      '0.lotNumber': 'Both lot number and expiry date are required for this item.',
    });
  });
});
