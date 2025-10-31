import fileDownload from 'js-file-download';
import queryString from 'query-string';

import MimeType from 'consts/mimeType';
import apiClient from 'utils/apiClient';

const exportFileFromAPI = ({
  url,
  params,
  format = 'csv',
  filename: customFilename,
  afterExporting,
}) => apiClient.get(url, {
  responseType: 'blob',
  params: {
    format,
    ...params,
  },
  paramsSerializer: queryString.stringify,
})
  .then((res) => {
    const filenameFromHeader = res.headers['content-disposition']
      .split('filename=')[1]
      .replaceAll('"', '')
      .split('.')[0];

    const filename = customFilename || filenameFromHeader || 'file';

    fileDownload(res.data, `${filename}.${format}`, MimeType[format]);
    afterExporting?.();
    return res;
  });

export default exportFileFromAPI;

export const extractFilenameFromHeader = (header) => {
  if (!header) {
    return null;
  }

  return header.split('filename=')[1]?.split(';')[0]?.replaceAll('"', '');
};
