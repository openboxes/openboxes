import { act, renderHook } from '@testing-library/react-hooks';

import ProductApi from 'api/services/ProductApi';
import useConfirmExpirationDateChange from 'hooks/receiving/v2/useConfirmExpirationDateChange';

jest.mock('api/services/ProductApi', () => ({
  getAvailabilityInAllDepots: jest.fn(),
}));

const mockSpinner = { show: jest.fn(), hide: jest.fn() };
jest.mock('hooks/useSpinner', () => () => mockSpinner);

const product = { id: 'product-1', productCode: '10001' };

// The lots as the availability endpoint reports them: which lot it is, its date, what is in stock
// and where. Lots that are not in inventory yet are absent from the response.
const mockLotAvailabilities = (...lotAvailabilities) =>
  ProductApi.getAvailabilityInAllDepots.mockResolvedValue({
    data: {
      data: lotAvailabilities.map(({
        productId = product.id,
        lotNumber = 'LOT-1',
        expirationDate = null,
        quantityOnHand,
        depots = [],
      }) => ({
        productId, lotNumber, expirationDate, quantityOnHand, depots,
      })),
    },
  });

const mockLotAvailability = (lotAvailability) => mockLotAvailabilities(lotAvailability);

// The modal opens from a promise chain, so the pending microtasks are drained before the
// state it sets is asserted (React 16.8 has no async act).
const flushPromises = async () => {
  for (let i = 0; i < 20; i += 1) {
    // eslint-disable-next-line no-await-in-loop
    await Promise.resolve();
  }
};

// A row of the form. Rows loaded with the modal carry a receipt item id and are matched against
// the line items the modal was opened with (below), rows added in the modal do not.
const buildLineItem = (overrides) => ({
  product,
  lotNumber: 'LOT-1',
  expirationDate: '01/Mar/2028',
  ...overrides,
});

const renderConfirmation = () => renderHook(() => useConfirmExpirationDateChange());

describe('useConfirmExpirationDateChange', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should not ask when the entered date matches the one the lot carries', async () => {
    mockLotAvailability({ expirationDate: '2028-03-01', quantityOnHand: 10 });
    const { result } = renderConfirmation();

    const lineItems = [buildLineItem()];
    await expect(result.current.confirmExpirationDateChange(lineItems)).resolves.toBe(true);

    expect(result.current.isExpirationModalOpen).toBe(false);
  });

  it('should not ask for confirmation for a lot that does not exist yet', async () => {
    mockLotAvailabilities();
    const { result } = renderConfirmation();

    const lineItems = [buildLineItem()];
    await expect(result.current.confirmExpirationDateChange(lineItems)).resolves.toBe(true);

    expect(result.current.isExpirationModalOpen).toBe(false);
  });

  it('should not ask for confirmation for an existing lot with nothing on hand', async () => {
    mockLotAvailability({ expirationDate: '2028-05-01', quantityOnHand: 0 });
    const { result } = renderConfirmation();

    const lineItems = [buildLineItem()];
    await expect(result.current.confirmExpirationDateChange(lineItems)).resolves.toBe(true);

    expect(result.current.isExpirationModalOpen).toBe(false);
  });

  it('should ask before clearing the date of a lot that carries one', async () => {
    mockLotAvailability({ expirationDate: '2028-05-01', quantityOnHand: 10 });
    const { result } = renderConfirmation();

    // An emptied field clears the date the lot carries, so it is a change like any other.
    result.current.confirmExpirationDateChange([buildLineItem({ expirationDate: '' })]);
    await flushPromises();

    expect(result.current.isExpirationModalOpen).toBe(true);
    expect(result.current.lotChangesToConfirm).toEqual([expect.objectContaining({
      previousExpiry: '2028-05-01',
      newExpiry: null,
    })]);

    act(() => result.current.handleExpirationModalResponse(true));
  });

  it('should look up every lot the rows carry once, in a single request', async () => {
    mockLotAvailability({ expirationDate: '2028-03-01', quantityOnHand: 10 });
    const { result } = renderConfirmation();

    const otherProduct = { id: 'product-2', productCode: '10002' };
    await result.current.confirmExpirationDateChange([
      buildLineItem({ lotNumber: 'LOT-2' }),
      buildLineItem({ lotNumber: 'LOT-2' }),
      buildLineItem({ product: otherProduct }),
    ]);

    expect(ProductApi.getAvailabilityInAllDepots).toHaveBeenCalledTimes(1);
    expect(ProductApi.getAvailabilityInAllDepots).toHaveBeenCalledWith([
      { product: { id: 'product-1' }, lotNumber: 'LOT-2' },
      { product: { id: 'product-2' }, lotNumber: 'LOT-1' },
    ]);
  });

  it('should match each row against the lot it asked about', async () => {
    mockLotAvailabilities(
      {
        productId: 'product-1', lotNumber: 'LOT-1', expirationDate: '2028-03-01', quantityOnHand: 10,
      },
      {
        productId: 'product-2', lotNumber: 'LOT-1', expirationDate: '2028-05-01', quantityOnHand: 7,
      },
    );
    const { result } = renderConfirmation();

    const otherProduct = { id: 'product-2', productCode: '10002' };
    result.current.confirmExpirationDateChange([
      buildLineItem(),
      buildLineItem({ product: otherProduct }),
    ]);
    await flushPromises();

    // Only the second row re-dates its lot, the first one enters the date its lot already carries.
    expect(result.current.lotChangesToConfirm).toEqual([expect.objectContaining({
      code: '10002',
      previousExpiry: '2028-05-01',
      newExpiry: '2028-03-01',
    })]);

    act(() => result.current.handleExpirationModalResponse(true));
  });

  it('should confirm a lot once when several rows re-date it the same way', async () => {
    mockLotAvailability({ expirationDate: '2028-05-01', quantityOnHand: 10 });
    const { result } = renderConfirmation();

    result.current.confirmExpirationDateChange([buildLineItem(), buildLineItem()]);
    await flushPromises();

    expect(result.current.lotChangesToConfirm).toEqual([expect.objectContaining({
      lotNumber: 'LOT-1',
      previousExpiry: '2028-05-01',
      newExpiry: '2028-03-01',
    })]);

    act(() => result.current.handleExpirationModalResponse(true));
  });

  it('should ask about a lot re-dated on one row while another row keeps its date', async () => {
    mockLotAvailability({ expirationDate: '2028-05-01', quantityOnHand: 10 });
    const { result } = renderConfirmation();

    // The backend applies every row, so the row leaving the lot alone must not hide the one
    // changing it - otherwise the lot gets re-dated across all depots with nothing asked.
    result.current.confirmExpirationDateChange([
      buildLineItem({ expirationDate: '01/May/2028' }),
      buildLineItem({ expirationDate: '01/Mar/2028' }),
    ]);
    await flushPromises();

    expect(result.current.isExpirationModalOpen).toBe(true);
    expect(result.current.lotChangesToConfirm).toEqual([expect.objectContaining({
      previousExpiry: '2028-05-01',
      newExpiry: '2028-03-01',
    })]);

    act(() => result.current.handleExpirationModalResponse(true));
  });

  it('should not swallow a failed lookup', async () => {
    ProductApi.getAvailabilityInAllDepots.mockRejectedValue(new Error('Not found'));
    const { result } = renderConfirmation();

    const lineItems = [buildLineItem()];
    await expect(result.current.confirmExpirationDateChange(lineItems)).rejects.toThrow('Not found');

    expect(result.current.isExpirationModalOpen).toBe(false);
    expect(mockSpinner.hide).toHaveBeenCalledTimes(1);
  });

  it('should show the lot, both dates and the depots holding it, and continue saving on confirm', async () => {
    const depots = [
      { depot: { id: 'depot-1', name: 'Kono Pharmacy Warehouse' }, quantityOnHand: 145 },
      { depot: { id: 'depot-2', name: 'Lisungwi Warehouse' }, quantityOnHand: 743 },
    ];
    mockLotAvailability({ expirationDate: '2028-05-01', quantityOnHand: 888, depots });
    const { result } = renderConfirmation();

    const confirmation = result.current.confirmExpirationDateChange([buildLineItem()]);
    await flushPromises();

    expect(result.current.isExpirationModalOpen).toBe(true);
    expect(result.current.lotChangesToConfirm).toEqual([{
      code: '10001',
      product,
      lotNumber: 'LOT-1',
      previousExpiry: '2028-05-01',
      newExpiry: '2028-03-01',
      depots,
    }]);

    act(() => result.current.handleExpirationModalResponse(true));

    await expect(confirmation).resolves.toBe(true);
  });

  it('should stop saving when the user cancels the confirmation', async () => {
    mockLotAvailability({ expirationDate: '2028-05-01', quantityOnHand: 10 });
    const { result } = renderConfirmation();

    const confirmation = result.current.confirmExpirationDateChange([buildLineItem()]);
    await flushPromises();

    act(() => result.current.handleExpirationModalResponse(false));

    await expect(confirmation).resolves.toBe(false);
    expect(result.current.isExpirationModalOpen).toBe(false);
  });
});
