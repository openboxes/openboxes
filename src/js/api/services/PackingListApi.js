import { IMPORT_PACKING_LIST } from 'api/urls';
import apiClient from 'utils/apiClient';

export default {
  importPackingList: (file) => {
    const formData = new FormData();
    formData.append('importFile', file);
    const config = {
      headers: {
        'content-type': 'multipart/form-data',
      },
    };
    return apiClient.post(IMPORT_PACKING_LIST, formData, config);
  },
};
