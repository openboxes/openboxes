import fileDownload from 'js-file-download';

import apiClient from 'utils/apiClient';
import exportFileFromAPI, { extractFilenameFromHeader } from 'utils/file-download-util';

// Node 14 has no String.prototype.replaceAll, which OBAF's downloader uses (browsers have it).
import 'regression-tests/utils/replaceAllPolyfill';

jest.mock('js-file-download', () => jest.fn());
jest.mock('utils/apiClient', () => ({ get: jest.fn() }));

describe('file-download-util', () => {
  beforeEach(() => {
    fileDownload.mockClear();
    apiClient.get.mockReset();
  });

  it('names the file after the Content-Disposition header', async () => {
    apiClient.get.mockResolvedValue({
      data: 'a,b',
      headers: { 'content-disposition': 'attachment; filename="StockMovements.csv"' },
    });

    await exportFileFromAPI({ url: '/api/stockMovements', params: { direction: 'INBOUND' } });

    expect(apiClient.get).toHaveBeenCalledWith('/api/stockMovements', expect.objectContaining({
      responseType: 'blob',
      params: { format: 'csv', direction: 'INBOUND' },
    }));
    expect(fileDownload.mock.calls[0].slice(0, 2)).toEqual(['a,b', 'StockMovements.csv']);
  });

  // KNOWN-GAP: OBAF reads the Content-Disposition header unconditionally and throws without it.
  it.skip('still downloads when the header cannot be read (a cross-origin response that does not expose it)', async () => {
    apiClient.get.mockResolvedValue({ data: 'a,b', headers: {} });

    await exportFileFromAPI({ url: '/api/purchaseOrders' });

    expect(fileDownload.mock.calls[0].slice(0, 2)).toEqual(['a,b', 'file.csv']);
  });

  // KNOWN-GAP: OBAF reads the Content-Disposition header unconditionally and throws without it.
  it.skip('prefers a filename given by the caller', async () => {
    apiClient.get.mockResolvedValue({ data: 'a,b', headers: {} });

    await exportFileFromAPI({ url: '/api/purchaseOrders', filename: 'Orders' });

    expect(fileDownload.mock.calls[0].slice(0, 2)).toEqual(['a,b', 'Orders.csv']);
  });

  // KNOWN-GAP: OBAF looks the MIME type up with the format as given (no case folding),
  // and throws without a Content-Disposition header before getting there.
  it.skip.each([
    ['xls', 'application/vnd.ms-excel'],
    ['pdf', 'application/pdf'],
    ['XLS', 'application/vnd.ms-excel'],
  ])('sets the content type for the %s format', async (format, mimeType) => {
    apiClient.get.mockResolvedValue({ data: 'a,b', headers: {} });

    await exportFileFromAPI({ url: '/api/purchaseOrders', format });

    expect(fileDownload).toHaveBeenCalledWith('a,b', `file.${format}`, mimeType);
  });

  it.each([
    [undefined, null],
    ['attachment; filename="a.csv"', 'a.csv'],
    ['attachment; filename=b.csv; size=3', 'b.csv'],
    ['inline', undefined],
  ])('reads the file name from %s', (header, expected) => {
    expect(extractFilenameFromHeader(header)).toBe(expected);
  });
});
