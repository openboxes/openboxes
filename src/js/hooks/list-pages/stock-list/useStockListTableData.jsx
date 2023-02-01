import _ from 'lodash';
import { confirmAlert } from 'react-confirm-alert';
import { getTranslate } from 'react-localize-redux';
import { useDispatch, useSelector } from 'react-redux';
import Alert from 'react-s-alert';

import { hideSpinner, showSpinner } from 'actions';
import stockListApi from 'api/services/StockListApi';
import { STOCKLIST_API, STOCKLIST_EXPORT } from 'api/urls';
import useTableData from 'hooks/list-pages/useTableData';
import exportFileFromAPI from 'utils/file-download-util';
import { translateWithDefaultMessage } from 'utils/Translate';

const useStockListTableData = (filterParams) => {
  const errorMessageId = 'react.stocklists.fetch.fail.label';
  const defaultErrorMessage = 'Unable to fetch stock transfers';

  const getParams = ({
    offset,
    state,
    sortingParams,
  }) => {
    const {
      isPublished, origin, destination, ...otherFilterParams
    } = filterParams;
    return _.omitBy({
      ...otherFilterParams,
      offset: `${offset}`,
      max: `${state.pageSize}`,
      includeUnpublished: isPublished,
      origin: origin && origin.map(({ id }) => id),
      destination: destination && destination.map(({ id }) => id),
      ...sortingParams,
    }, (value) => {
      if (typeof value === 'object' && _.isEmpty(value)) return true;
      return !value;
    });
  };

  const {
    tableRef,
    fireFetchData,
    loading,
    tableData,
    onFetchHandler,
  } = useTableData({
    filterParams,
    url: STOCKLIST_API,
    errorMessageId,
    defaultErrorMessage,
    getParams,
  });

  const dispatch = useDispatch();
  const { translate } = useSelector(state => ({
    translate: translateWithDefaultMessage(getTranslate(state.localize)),
  }));

  const exportStockList = () => {
    exportFileFromAPI({
      url: STOCKLIST_API,
      params: {
        ...tableData.currentParams,
      },
    });
  };

  const exportStockListItems = (id) => {
    exportFileFromAPI({
      url: STOCKLIST_EXPORT(id),
    });
  };


  const deleteStocklists = async (id) => {
    dispatch(showSpinner());
    try {
      const { status } = await stockListApi.deleteStockList(id);
      if (status === 204) {
        Alert.success(translate(
          'react.stocklists.delete.success.label',
          'Stock List has been deleted successfully',
        ));
        fireFetchData();
      }
    } finally {
      dispatch(hideSpinner());
    }
  };

  const onClickDeleteStocklists = (id) => {
    const confirmButton = {
      label: translate('react.default.yes.label', 'Yes'),
      onClick: () => deleteStocklists(id),
    };
    const cancelButton = {
      label: translate('react.default.no.label', 'No'),
    };
    confirmAlert({
      title: translate(
        'react.stocklists.delete.confirm.title.label',
        'Confirm delete of stock list',
      ),
      message: translate(
        'react.stocklists.delete.confirm.message.label',
        'Are you sure you want to delete this stock list ?',
      ),
      buttons: [confirmButton, cancelButton],
    });
  };

  const clearStocklists = async (id) => {
    dispatch(showSpinner());
    try {
      const { status } = await stockListApi.clearStockList(id);
      if (status === 200) {
        Alert.success(translate(
          'react.stocklists.clear.success.label',
          'Stock List has been cleared Stock List has been cloned successfully',
        ));
        fireFetchData();
      }
    } finally {
      dispatch(hideSpinner());
    }
  };

  const onClickClearStocklists = (id) => {
    const confirmButton = {
      label: translate('react.default.yes.label', 'Yes'),
      onClick: () => clearStocklists(id),
    };
    const cancelButton = {
      label: translate('react.default.no.label', 'No'),
    };
    confirmAlert({
      title: translate(
        'react.stocklists.clear.confirm.title.label',
        'Confirm clear of stock list',
      ),
      message: translate(
        'react.stocklists.clear.confirm.message.label',
        'Are you sure you want to clear this stock list ?',
      ),
      buttons: [confirmButton, cancelButton],
    });
  };

  const cloneStocklists = async (id) => {
    dispatch(showSpinner());
    try {
      const { status } = await stockListApi.cloneStockList(id);
      if (status === 200) {
        Alert.success(translate(
          'react.stocklists.clone.success.label',
          'Stock List has been cloned successfully',
        ));
        fireFetchData();
      }
    } finally {
      dispatch(hideSpinner());
    }
  };

  const publishStocklists = async (id) => {
    dispatch(showSpinner());
    try {
      const { status } = await stockListApi.publishStockList(id);
      if (status === 200) {
        Alert.success(translate(
          'react.stocklists.publish.success.label',
          'Stock List has been published successfully',
        ));
        fireFetchData();
      }
    } finally {
      dispatch(hideSpinner());
    }
  };

  const unpublishStocklists = async (id) => {
    dispatch(showSpinner());
    try {
      const { status } = await stockListApi.unpublishStockList(id);
      if (status === 200) {
        Alert.success(translate(
          'react.stocklists.unpublish.success.label',
          'Stock List has been unpublished successfully',
        ));
        fireFetchData();
      }
    } finally {
      dispatch(hideSpinner());
    }
  };

  return {
    tableData,
    tableRef,
    loading,
    onFetchHandler,
    exportStockList,
    onClickClearStocklists,
    onClickDeleteStocklists,
    unpublishStocklists,
    publishStocklists,
    cloneStocklists,
    exportStockListItems,
  };
};

export default useStockListTableData;
