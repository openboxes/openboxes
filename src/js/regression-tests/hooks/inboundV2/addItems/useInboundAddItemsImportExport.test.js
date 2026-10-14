import { renderHook } from '@testing-library/react-hooks';
import fileDownload from 'js-file-download';

import stockMovementApi from 'api/services/StockMovementApi';
import useInboundAddItemsImportExport from 'hooks/inboundV2/addItems/useInboundAddItemsImportExport';

jest.mock('js-file-download', () => jest.fn());
jest.mock('api/services/StockMovementApi', () => ({
  importCsv: jest.fn(() => Promise.resolve({})),
  exportCsv: jest.fn(() => Promise.resolve({ data: 'code,qty\nP1,2' })),
}));
const mockSpinner = { show: jest.fn(), hide: jest.fn() };
jest.mock('hooks/useSpinner', () => jest.fn(() => mockSpinner));

describe('useInboundAddItemsImportExport', () => {
  const lineItems = [{ product: { id: 'p-1' }, quantityRequested: 2 }];
  let fetchLineItems;
  let saveRequisitionItemsInCurrentStep;

  const renderImportExport = (values) => renderHook(() => useInboundAddItemsImportExport({
    getValues: jest.fn((path) => (path === 'values' ? values : undefined)),
    fetchLineItems,
    saveRequisitionItemsInCurrentStep,
  })).result.current;

  beforeEach(() => {
    jest.clearAllMocks();
    fetchLineItems = jest.fn(() => Promise.resolve());
    saveRequisitionItemsInCurrentStep = jest.fn(() => Promise.resolve());
  });

  it('uploads the chosen file as CSV and shows only the imported lines', async () => {
    const { importTemplate } = renderImportExport({ stockMovementId: 'sm-1' });
    const file = new File(['code,qty\nP1,2'], 'items.csv');

    await importTemplate([file]);

    const [id, formData] = stockMovementApi.importCsv.mock.calls[0];
    expect(id).toBe('sm-1');
    const uploaded = formData.get('importFile');
    expect(uploaded.type).toBe('text/csv');
    expect(uploaded.size).toBe(file.size);
    expect(fetchLineItems).toHaveBeenCalledWith(true);
    expect(mockSpinner.hide).toHaveBeenCalled();
  });

  it('does not refetch lines when the import fails', async () => {
    stockMovementApi.importCsv.mockRejectedValueOnce(new Error('bad file'));
    const { importTemplate } = renderImportExport({ stockMovementId: 'sm-1' });
    await expect(importTemplate([new File(['x'], 'x.csv')])).rejects.toThrow('bad file');
    expect(fetchLineItems).not.toHaveBeenCalled();
    expect(mockSpinner.hide).toHaveBeenCalled();
  });

  it('saves the current lines before downloading them named after the identifier', async () => {
    const { exportTemplate } = renderImportExport({ stockMovementId: 'sm-1', identifier: 'IN-7', lineItems });
    await exportTemplate();
    expect(saveRequisitionItemsInCurrentStep).toHaveBeenCalledWith(lineItems);
    expect(stockMovementApi.exportCsv).toHaveBeenCalledWith('sm-1');
    expect(fileDownload).toHaveBeenCalledWith('code,qty\nP1,2', 'ItemList-IN-7.csv', 'text/csv');
  });

  it('uses a plain file name without an identifier', async () => {
    const { exportTemplate } = renderImportExport({ stockMovementId: 'sm-1', lineItems });
    await exportTemplate();
    expect(fileDownload).toHaveBeenCalledWith(expect.anything(), 'ItemList.csv', 'text/csv');
  });

  it('does not export when saving fails', async () => {
    saveRequisitionItemsInCurrentStep.mockRejectedValue(new Error('save failed'));
    const { exportTemplate } = renderImportExport({ stockMovementId: 'sm-1', lineItems });
    await expect(exportTemplate()).rejects.toThrow('save failed');
    expect(stockMovementApi.exportCsv).not.toHaveBeenCalled();
    expect(mockSpinner.hide).toHaveBeenCalled();
  });
});
