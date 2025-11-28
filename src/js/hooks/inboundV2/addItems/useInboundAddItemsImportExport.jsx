import fileDownload from 'js-file-download';
import _ from 'lodash';

import stockMovementApi from 'api/services/StockMovementApi';
import useSpinner from 'hooks/useSpinner';

const useInboundAddItemsImportExport = ({
  getValues,
  setValue,
  fetchLineItems,
  saveRequisitionItemsInCurrentStep,
  defaultTableRow,
}) => {
  const spinner = useSpinner();

  const importTemplate = async (event) => {
    try {
      spinner.show();
      const formData = new FormData();
      const file = event.target.files[0];
      const { stockMovementId } = getValues('values');

      formData.append('importFile', file.slice(0, file.size, 'text/csv'));
      const config = {
        headers: {
          'content-type': 'multipart/form-data',
        },
      };

      await stockMovementApi.importCsv(stockMovementId, formData, config);

      fetchLineItems(true);
      const { lineItems } = getValues('values');
      const lastLineItem = _.last(lineItems);
      const isLastProductNil = _.isNil(lastLineItem?.product);

      if (isLastProductNil) {
        setValue('values.lineItems', defaultTableRow);
      }
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
