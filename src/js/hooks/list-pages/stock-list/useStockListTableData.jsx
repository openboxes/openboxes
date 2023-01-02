import _ from 'lodash';
import { confirmAlert } from 'react-confirm-alert';
import { getTranslate } from 'react-localize-redux';
import { useDispatch, useSelector } from 'react-redux';
import Alert from 'react-s-alert';

import { hideSpinner, showSpinner } from 'actions';
import useTableData from 'hooks/list-pages/useTableData';
import apiClient from 'utils/apiClient';
import exportFileFromAPI from 'utils/file-download-util';
import { translateWithDefaultMessage } from 'utils/Translate';

const useStockListTableData = (filterParams) => {
  const url = '/openboxes/api/stocklists';
  const messageId = 'react.stocklists.fetch.fail.label';
  const defaultMessage = 'Unable to fetch stock transfers';

  const getParams = (offset, currentLocation, state, sortingParams) => {
    const { isPublished, ...otherFilterParams } = filterParams;
    return _.omitBy({
      ...otherFilterParams,
      offset: `${offset}`,
      max: `${state.pageSize}`,
      includeUnpublished: isPublished,
      origin: filterParams.origin && filterParams.origin.map(({ id }) => id),
      destination: filterParams.destination && filterParams.destination.map(({ id }) => id),
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
    url,
    messageId,
    defaultMessage,
    getParams,
  });

  const dispatch = useDispatch();
  const { translate } = useSelector(state => ({
    translate: translateWithDefaultMessage(getTranslate(state.localize)),
  }));

  const exportStockList = () => {
    exportFileFromAPI({
      url: '/openboxes/api/stocklists',
      params: {
        ...tableData.currentParams,
      },
    });
  };

  const exportStockListItems = (id) => {
    exportFileFromAPI({
      url: `/openboxes/api/stocklists/${id}/export`,
    });
  };


  const deleteStocklists = (id) => {
    dispatch(showSpinner());
    apiClient.delete(`/openboxes/api/stocklists/${id}`)
      .then((res) => {
        if (res.status === 204) {
          Alert.success(translate(
            'react.stocklists.delete.success.label',
            'Stock List has been deleted successfully',
          ));
          fireFetchData();
        }
      })
      .finally(() => dispatch(hideSpinner()));
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

  const clearStocklists = (id) => {
    dispatch(showSpinner());
    apiClient.post(`/openboxes/api/stocklists/${id}/clear`)
      .then((res) => {
        if (res.status === 200) {
          Alert.success(translate(
            'react.stocklists.clear.success.label',
            'Stock List has been cleared Stock List has been cloned successfully',
          ));
          fireFetchData();
        }
      })
      .finally(() => dispatch(hideSpinner()));
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

  const cloneStocklists = (id) => {
    dispatch(showSpinner());
    apiClient.post(`/openboxes/api/stocklists/${id}/clone`)
      .then((res) => {
        if (res.status === 200) {
          Alert.success(translate(
            'react.stocklists.clone.success.label',
            'Stock List has been cloned successfully',
          ));
          fireFetchData();
        }
      })
      .finally(() => dispatch(hideSpinner()));
  };

  const publishStocklists = (id) => {
    dispatch(showSpinner());
    apiClient.post(`/openboxes/api/stocklists/${id}/publish`)
      .then((res) => {
        if (res.status === 200) {
          Alert.success(translate(
            'react.stocklists.publish.success.label',
            'Stock List has been published successfully',
          ));
          fireFetchData();
        }
      })
      .finally(() => dispatch(hideSpinner()));
  };

  const unpublishStocklists = (id) => {
    dispatch(showSpinner());
    apiClient.post(`/openboxes/api/stocklists/${id}/unpublish`)
      .then((res) => {
        if (res.status === 200) {
          Alert.success(translate(
            'react.stocklists.unpublish.success.label',
            'Stock List has been unpublished successfully',
          ));
          fireFetchData();
        }
      })
      .finally(() => dispatch(hideSpinner()));
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
