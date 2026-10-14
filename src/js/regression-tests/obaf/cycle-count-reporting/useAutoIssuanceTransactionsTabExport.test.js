// OBAF-new: the Auto-Issuance Transactions export (GET
// /api/reports/auto-issuance-transactions.csv).
import { renderHook } from '@testing-library/react-hooks';

import useAutoIssuanceTransactionsTabExport from 'hooks/cycleCount/useAutoIssuanceTransactionsTabExport';
import exportFileFromAPI from 'utils/file-download-util';

jest.mock('react-redux', () => ({
  useSelector: jest.fn((selector) => selector({ locationId: 'loc-1' })),
}));
jest.mock('selectors', () => ({
  getCurrentLocationId: (state) => state.locationId,
}));
const mockSpinner = { show: jest.fn(), hide: jest.fn() };
jest.mock('hooks/useSpinner', () => jest.fn(() => mockSpinner));
jest.mock('utils/file-download-util', () => ({ __esModule: true, default: jest.fn(() => Promise.resolve()) }));

const filters = {
  startDate: '2026-01-01T00:00:00',
  endDate: '2026-01-31T00:00:00',
  products: [{ id: 'prod-1' }, { id: 'prod-2' }],
  binLocations: [{ id: 'bin-1' }],
};

describe('useAutoIssuanceTransactionsTabExport (OBAF)', () => {
  beforeEach(() => jest.clearAllMocks());

  it('offers a full and a filtered export', () => {
    const { result } = renderHook(() => useAutoIssuanceTransactionsTabExport(filters));
    expect(result.current.actions.map((action) => [action.label, action.defaultLabel])).toEqual([
      ['react.cycleCount.reporting.exportFullReport.label', 'Export Full Report'],
      ['react.cycleCount.reporting.exportFilteredReport.label', 'Export Filtered Report'],
    ]);
  });

  it('exports the full report with only the dates and the facility', async () => {
    const { result } = renderHook(() => useAutoIssuanceTransactionsTabExport(filters));
    await result.current.actions[0].onClick();

    expect(exportFileFromAPI).toHaveBeenCalledWith({
      url: '/api/reports/auto-issuance-transactions.csv',
      params: { startDate: '01/01/2026', endDate: '01/31/2026', facility: 'loc-1' },
    });
    expect(mockSpinner.show).toHaveBeenCalledTimes(1);
    expect(mockSpinner.hide).toHaveBeenCalledTimes(1);
  });

  it('exports the filtered report with product and bin ids', async () => {
    const { result } = renderHook(() => useAutoIssuanceTransactionsTabExport(filters));
    await result.current.actions[1].onClick();

    expect(exportFileFromAPI).toHaveBeenCalledWith({
      url: '/api/reports/auto-issuance-transactions.csv',
      params: {
        startDate: '01/01/2026',
        endDate: '01/31/2026',
        products: ['prod-1', 'prod-2'],
        binLocations: ['bin-1'],
        facility: 'loc-1',
      },
    });
  });

  it('drops empty product and bin lists from the filtered export', async () => {
    const { result } = renderHook(() => useAutoIssuanceTransactionsTabExport({
      ...filters, products: [], binLocations: undefined,
    }));
    await result.current.actions[1].onClick();

    const { params } = exportFileFromAPI.mock.calls[0][0];
    expect(params).not.toHaveProperty('products');
    expect(params).not.toHaveProperty('binLocations');
  });

  it('hides the spinner even when the download fails', async () => {
    exportFileFromAPI.mockRejectedValueOnce(new Error('500'));
    const { result } = renderHook(() => useAutoIssuanceTransactionsTabExport(filters));

    await expect(result.current.actions[0].onClick()).rejects.toThrow('500');
    expect(mockSpinner.hide).toHaveBeenCalledTimes(1);
  });
});
