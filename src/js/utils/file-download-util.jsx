import fileDownload from 'js-file-download';
import queryString from 'query-string';

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
      .split('filename="')[1]
      .split('.')[0];

    const filename = customFilename || filenameFromHeader || 'file';

    fileDownload(res.data, `${filename}.${format}`);
    afterExporting?.();
    return res;
  });

export default exportFileFromAPI;
