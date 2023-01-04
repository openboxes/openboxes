import _ from 'lodash';
import { confirmAlert } from 'react-confirm-alert';
import { getTranslate } from 'react-localize-redux';
import { useDispatch, useSelector } from 'react-redux';
import Alert from 'react-s-alert';

import { hideSpinner, showSpinner } from 'actions';
import stockTransferApi from 'api/services/StockTransferApi';
import { STOCK_TRANSFER_API } from 'api/urls';
import useTableData from 'hooks/list-pages/useTableData';
import { translateWithDefaultMessage } from 'utils/Translate';

const useStockTransferListTableData = (filterParams) => {
  const messageId = 'react.stockTransfer.fetch.fail.label';
  const defaultMessage = 'Unable to fetch stock transfers';
  const defaultSorting = {
    sort: 'dateCreated',
    order: 'desc',
  };
  const getParams = (offset, currentLocation, tableState, sortingParams) => _.omitBy({
    location: currentLocation?.id,
    offset: `${offset}`,
    max: `${tableState.pageSize}`,
    ...sortingParams,
    ...filterParams,
    createdBy: filterParams.createdBy?.id,
    status: filterParams.status && filterParams.status.map(({ value }) => value),
  }, _.isEmpty);

  const {
    tableRef,
    fireFetchData,
    loading,
    tableData,
    onFetchHandler,
  } = useTableData({
    filterParams,
    url: STOCK_TRANSFER_API,
    messageId,
    defaultMessage,
    defaultSorting,
    getParams,
  });

  const dispatch = useDispatch();
  const { translate } = useSelector(state => ({
    translate: translateWithDefaultMessage(getTranslate(state.localize)),
  }));

  const deleteStockTransfer = async (id) => {
    dispatch(showSpinner());
    try {
      const { status } = await stockTransferApi.deleteStockTransfer(id);
      if (status === 204) {
        const successMessage = translate('react.stockTransfer.delete.success.label', 'Stock transfer has been deleted successfully');
        Alert.success(successMessage);
      }
    } finally {
      dispatch(hideSpinner());
      fireFetchData();
    }
  };

  const deleteHandler = (id) => {
    confirmAlert({
      title: translate('react.default.areYouSure.label', 'Are you sure?'),
      message: translate(
        'react.stockTransfer.delete.confirm.label',
        'Are you sure you want to delete this stock transfer?',
      ),
      buttons: [
        {
          label: translate('react.default.yes.label', 'Yes'),
          onClick: () => deleteStockTransfer(id),
        },
        {
          label: translate('react.default.no.label', 'No'),
        },
      ],
    });
  };

  return {
    onFetchHandler,
    deleteHandler,
    loading,
    tableData,
    tableRef,
  };
};

export default useStockTransferListTableData;
