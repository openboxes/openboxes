import { waitFor } from '@testing-library/react';
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
    productApi.getLatestInventoryCountDate.mockResolvedValue({
      data: { data: mockData },
    });
  });

  it('calls API and updates map when loading=false', async () => {
    const { result } = renderHook(() =>
      useLastCountedDate(mockTableData, false));

    expect(productApi.getLatestInventoryCountDate).toHaveBeenCalledWith(['1234', '5678']);

    await waitFor(() => {
      expect(result.current.lastCountedDateMap).toEqual(mockData);
    });
  });

  it('resets map when loading is true', () => {
    const { result, rerender } = renderHook(
      ({ tableData, loading }) => useLastCountedDate(tableData, loading),
      {
        initialProps: { tableData: mockTableData, loading: true },
      },
    );

    expect(result.current.lastCountedDateMap).toEqual({});

    rerender({ tableData: mockTableData, loading: true });
    expect(result.current.lastCountedDateMap).toEqual({});
  });

  it('does not call API when tableData.data is empty', async () => {
    const { result } = renderHook(() =>
      useLastCountedDate({ data: [] }, false));

    expect(productApi.getLatestInventoryCountDate).not.toHaveBeenCalled();
    expect(result.current.lastCountedDateMap).toEqual({});
  });
});
