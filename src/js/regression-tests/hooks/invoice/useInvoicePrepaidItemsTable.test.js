import { act, renderHook } from '@testing-library/react-hooks';

import { PREPAYMENT_INVOICE_INVOICE_ITEMS, PREPAYMENT_INVOICE_ITEM_BY_ID } from 'api/urls';
import InvoiceStatus from 'consts/invoiceStatus';
import useInvoicePrepaidItemsTable from 'hooks/invoice/useInvoicePrepaidItemsTable';
import apiClient from 'utils/apiClient';

const mockDispatch = jest.fn();
jest.mock('react-redux', () => ({
  useDispatch: () => mockDispatch,
  connect: jest.fn(() => (Component) => Component),
}));
// eslint-disable-next-line global-require
jest.mock('utils/apiClient', () => require('regression-tests/__mocks__/apiClient').default());
jest.mock('hooks/useTranslate', () => () => (id, defaultMessage) => defaultMessage);

const items = [
  { id: 'i1', quantity: 2, unitPrice: 5 },
  {
    id: 'i2', orderAdjustment: { id: 'adj' }, unitPrice: 10, amount: 20,
  },
  { id: 'i3', quantity: 1, isCanceled: true },
];

describe('useInvoicePrepaidItemsTable', () => {
  let props;

  beforeEach(() => {
    jest.clearAllMocks();
    apiClient.post.mockResolvedValue({});
    apiClient.delete.mockResolvedValue({});
    props = {
      invoiceItems: items,
      updateInvoiceItemData: jest.fn(() => (id, field) => (value) => {
        props.updates.push([id, field, value]);
      }),
      invoiceId: 'inv-1',
      refetchData: jest.fn(),
      invoiceItemsMap: new Map([[0, items[0]]]),
      updates: [],
    };
  });

  const render = () => renderHook(
    (hookProps) => useInvoicePrepaidItemsTable(hookProps),
    { initialProps: props },
  );

  it('offers edit and remove, or only remove for canceled and zero priced adjustments', () => {
    const { result } = render();

    expect(result.current.actions(items[0]).map((action) => action.defaultLabel)).toEqual(['Edit', 'Remove']);
    expect(result.current.actions(items[2]).map((action) => action.defaultLabel)).toEqual(['Remove']);
    expect(result.current.actions({ id: 'x', orderAdjustment: {}, unitPrice: 0 }).map((a) => a.defaultLabel))
      .toEqual(['Remove']);
  });

  it('marks a row editable from the edit action and sends only edited rows on update', async () => {
    const { result } = render();

    act(() => result.current.actions(items[0])[0].onClick());
    act(() => result.current.actions(items[1])[0].onClick());

    expect(result.current.isEditable('i1')).toBe(true);
    expect(result.current.isEditable('i3')).toBe(false);
    expect(result.current.editableRows).toEqual({
      i1: { quantity: 2, unitPrice: 5 },
      i2: { quantity: undefined, unitPrice: 10 },
    });

    const callback = jest.fn();
    await result.current.updateInvoiceItem(callback);

    expect(apiClient.post).toHaveBeenCalledWith(PREPAYMENT_INVOICE_INVOICE_ITEMS('inv-1'), [
      { id: 'i1', quantity: 2 },
      { id: 'i2', unitPrice: 10 },
    ]);
    expect(callback).toHaveBeenCalledTimes(1);
  });

  it('skips the request when nothing is edited and hides the spinner on failure', async () => {
    const { result } = render();
    const callback = jest.fn();

    await result.current.updateInvoiceItem(callback);

    expect(apiClient.post).not.toHaveBeenCalled();
    expect(callback).toHaveBeenCalled();

    act(() => result.current.actions(items[0])[0].onClick());
    apiClient.post.mockRejectedValueOnce(new Error('fail'));
    await result.current.updateInvoiceItem(callback);

    expect(callback).toHaveBeenCalledTimes(1);
    expect(mockDispatch).toHaveBeenCalledWith({ type: 'HIDE_SPINNER', payload: false });
  });

  it('merges row updates into the edited state', () => {
    const { result } = render();

    act(() => result.current.updateRow('i1', { quantity: 4 }));
    act(() => result.current.updateRow('i1', { unitPrice: 3 }));

    expect(result.current.editableRows).toEqual({ i1: { quantity: 4, unitPrice: 3 } });
  });

  it('deletes an item, forgets its edits and refetches with override', async () => {
    const { result } = render();
    act(() => result.current.actions(items[0])[0].onClick());

    await result.current.actions(items[0])[1].onClick();

    expect(apiClient.delete).toHaveBeenCalledWith(PREPAYMENT_INVOICE_ITEM_BY_ID('i1'));
    expect(result.current.editableRows).toEqual({});
    expect(props.refetchData).toHaveBeenCalledWith({ overrideInvoiceItems: true });
    expect(mockDispatch.mock.calls.map(([action]) => action.type)).toEqual(['SHOW_SPINNER', 'HIDE_SPINNER']);
  });

  it('reapplies edited values when the item count changes after a refetch', () => {
    const { result, rerender } = render();
    act(() => result.current.updateRow('i1', { quantity: 9, unitPrice: 1 }));

    rerender({ ...props, invoiceItems: items.slice(0, 2) });

    expect(props.updates).toEqual([['i1', 'quantity', 9], ['i1', 'unitPrice', 1]]);
  });

  describe('validate', () => {
    it('ignores rows that are not being edited', () => {
      const { result } = render();

      expect(result.current.validate(items[0])).toBeNull();
      expect(result.current.isValid).toBe(true);
    });

    it('checks the quantity against what is still available to invoice', () => {
      const { result } = render();
      act(() => result.current.updateRow('i1', {}));

      let message;
      act(() => {
        message = result.current.validate({ id: 'i1', quantity: 5, quantityAvailableToInvoice: 4 });
      });
      expect(message).toBe('Wrong quantity to invoice value');
      expect(result.current.isValid).toBe(false);

      act(() => {
        message = result.current.validate({ id: 'i1', quantity: 0, quantityAvailableToInvoice: 4 });
      });
      expect(message).toBe('Wrong quantity to invoice value');

      act(() => {
        message = result.current.validate({ id: 'i1', quantity: 4, quantityAvailableToInvoice: 4 });
      });
      expect(message).toBeNull();
      expect(result.current.isValid).toBe(true);
    });

    it('checks adjustment unit prices for amount, sign and presence', () => {
      const { result } = render();
      act(() => result.current.updateRow('i2', {}));
      const adjustment = {
        id: 'i2', orderAdjustment: {}, amount: 20, unitPriceAvailableToInvoice: 10,
      };
      const validate = (unitPrice) => {
        let message;
        act(() => {
          message = result.current.validate({ ...adjustment, unitPrice });
        });
        return message;
      };

      expect(validate(30)).toBeNull();
      expect(validate(31)).toBe('Wrong amount to invoice value');
      expect(validate(-5)).toBe('Wrong amount to invoice value');
      expect(validate(0)).toBe('Wrong amount to invoice value');
      expect(result.current.isValid).toBe(false);
      expect(validate(5)).toBeNull();
      expect(result.current.isValid).toBe(true);

      act(() => {
        result.current.validate({ ...adjustment, amount: -20, unitPrice: 5 });
      });
      expect(result.current.isValid).toBe(false);
    });
  });

  it('shows the action menu only for editable invoice states on regular items', () => {
    const { result } = render();

    expect(result.current.isActionMenuVisible(InvoiceStatus.PENDING, false, false)).toBe(true);
    expect(result.current.isActionMenuVisible(InvoiceStatus.SUBMITTED, false, false)).toBe(true);
    expect(result.current.isActionMenuVisible(InvoiceStatus.POSTED, false, false)).toBe(false);
    expect(result.current.isActionMenuVisible(InvoiceStatus.PENDING, true, false)).toBe(false);
    expect(result.current.isActionMenuVisible(InvoiceStatus.PENDING, false, true)).toBe(false);
  });

  it('reports which rows are loaded from the items map', () => {
    const { result } = render();

    expect(result.current.isRowLoaded({ index: 0 })).toBe(true);
    expect(result.current.isRowLoaded({ index: 1 })).toBe(false);
  });

  it('saves edits and clears the edited state once the refetch completes', async () => {
    const { result } = render();
    act(() => result.current.updateRow('i1', { quantity: 2 }));

    await result.current.save();

    expect(apiClient.post).toHaveBeenCalledWith(PREPAYMENT_INVOICE_INVOICE_ITEMS('inv-1'), [{ id: 'i1', quantity: 2 }]);
    const [{ overrideInvoiceItems, callback }] = props.refetchData.mock.calls[0];
    expect(overrideInvoiceItems).toBe(true);

    act(() => callback());

    expect(result.current.editableRows).toEqual({});
    expect(result.current.isValid).toBe(true);
  });
});
