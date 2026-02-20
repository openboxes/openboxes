import fileDownload from 'js-file-download';

import stockMovementApi from 'api/services/StockMovementApi';
import useSpinner from 'hooks/useSpinner';

const useInboundAddItemsImportExport = ({
  getValues,
  fetchLineItems,
  saveRequisitionItemsInCurrentStep,
}) => {
  const spinner = useSpinner();

  const importTemplate = async (importFile) => {
    try {
      spinner.show();
      const formData = new FormData();
      const file = importFile[0];
      const { stockMovementId } = getValues('values');

      formData.append('importFile', file.slice(0, file.size, 'text/csv'));

      await stockMovementApi.importCsv(stockMovementId, formData);
      await fetchLineItems(true);
    } finally {
      spinner.hide();
    }
  };

  const exportTemplate = async () => {
    const { lineItems, identifier, stockMovementId } = getValues('values');
    try {
      spinner.show();
      await saveRequisitionItemsInCurrentStep(lineItems);
      const response = await stockMovementApi.exportCsv(stockMovementId);
      fileDownload(response.data, `ItemList${identifier ? `-${identifier}` : ''}.csv`, 'text/csv');
    } finally {
      spinner.hide();
    }
  };

  return {
    importTemplate,
    exportTemplate,
  };
};

export default useInboundAddItemsImportExport;
