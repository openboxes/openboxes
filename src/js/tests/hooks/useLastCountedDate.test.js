import { renderHook } from '@testing-library/react-hooks';

import productApi from 'api/services/ProductApi';
import useLastCountedDate from 'hooks/cycleCount/useLastCountedDate';

jest.mock('api/services/ProductApi');

describe('useLastCountedDate', () => {
  const mockTableData = {
    data: [
      { product: { id: '1234' } },
      { product: { id: '5678' } },
    ],
  };

  const mockData = {
    1234: '2025-08-20',
    5678: '2025-08-21',
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('does not call API when tableData.data is empty', () => {
    const { result } = renderHook(() =>
      useLastCountedDate({ data: [] }, false));

    expect(productApi.getLatestInventoryCountDate).not.toHaveBeenCalled();
    expect(result.current.lastCountedDateMap).toEqual({});
  });

  it('does not fetch data and resets map when loading=true', () => {
    const { result } = renderHook(() =>
      useLastCountedDate(mockTableData, true));

    expect(productApi.getLatestInventoryCountDate).not.toHaveBeenCalled();
    expect(result.current.lastCountedDateMap).toEqual({});
  });

  it('calls API when loading = false', () => {
    productApi.getLatestInventoryCountDate.mockResolvedValue({
      data: { data: mockData },
    });

    renderHook(() =>
      useLastCountedDate(mockTableData, false));

    expect(productApi.getLatestInventoryCountDate).toHaveBeenCalledWith([
      mockTableData.data[0].product.id,
      mockTableData.data[1].product.id,
    ]);
  });
});
