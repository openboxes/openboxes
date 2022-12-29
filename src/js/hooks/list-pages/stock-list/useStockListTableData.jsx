import { useCallback, useState } from 'react';

import _ from 'lodash';
import queryString from 'query-string';
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
  const { tableRef, fireFetchData } = useTableData(filterParams);
  const [tableData, setTableData] = useState({
    stockListData: [],
    pages: -1,
    totalCount: 0,
    currentParams: {},
  });
  const [loading, setLoading] = useState(true);
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

  const onFetchHandler = useCallback((state) => {
    if (!_.isEmpty(filterParams)) {
      const offset = state.page > 0 ? (state.page) * state.pageSize : 0;
      const sortingParams = state.sorted.length > 0 ?
        {
          sort: state.sorted[0].id,
          order: state.sorted[0].desc ? 'desc' : 'asc',
        } : undefined;

      const { isPublished, ...otherFilterParams } = filterParams;
      const params = _.omitBy({
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

      // Fetch data
      setLoading(true);
      apiClient.get('/openboxes/api/stocklists', {
        params,
        paramsSerializer: parameters => queryString.stringify(parameters),
      })
        .then((res) => {
          setTableData({
            stockListData: res.data.data,
            pages: Math.ceil(res.data.totalCount / state.pageSize),
            totalCount: res.data.totalCount,
            currentParams: params,
          });
        })
        .finally(() => setLoading(false));
    }
  }, [filterParams]);

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
