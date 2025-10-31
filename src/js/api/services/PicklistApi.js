import {
  PICKLIST_CLEAR,
  PICKLIST_IMPORT,
  PICKLIST_ITEMS_EXPORT,
  PICKLIST_TEMPLATE_EXPORT,
} from 'api/urls';
import apiClient from 'utils/apiClient';
import exportFileFromAPI from 'utils/file-download-util';

export default {
  clearPicklist: (id) => apiClient.delete(PICKLIST_CLEAR(id)),
  exportPicklistItems: (id, { format, fileName }) =>
    exportFileFromAPI({
      url: PICKLIST_ITEMS_EXPORT(id),
      filename: fileName ?? 'PickListItems',
      format: format ?? 'csv',
    }),
  exportPicklistTemplate: (id, { format, fileName }) =>
    exportFileFromAPI({
      url: PICKLIST_TEMPLATE_EXPORT(id),
      filename: fileName ?? 'PickListItems-template',
      format: format ?? 'csv',
    }),
  importPicklist: (id, file) => {
    const formData = new FormData();
    formData.append('importFile', file.slice(0, file.size, 'text/csv'));
    const config = {
      headers: {
        'content-type': 'multipart/form-data',
      },
    };
    return apiClient.post(PICKLIST_IMPORT(id), formData, config);
  },
};
