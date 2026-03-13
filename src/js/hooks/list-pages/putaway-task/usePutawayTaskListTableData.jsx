import _ from 'lodash';
import { confirmAlert } from 'react-confirm-alert';
import { getTranslate } from 'react-localize-redux';
import { useDispatch, useSelector } from 'react-redux';
import Alert from 'react-s-alert';

import { hideSpinner, showSpinner } from 'actions';
import { PUTAWAY_TASK_API } from 'api/urls';
import useTableData from 'hooks/list-pages/useTableData';
import apiClient from 'utils/apiClient';
import { translateWithDefaultMessage } from 'utils/Translate';

const usePutawayTaskListTableData = (filterParams) => {
  const errorMessageId = 'react.putawayTask.fetch.fail.label';
  const defaultErrorMessage = 'Unable to fetch putaway tasks';
  const defaultSorting = {
    sort: 'dateCreated',
    order: 'desc',
  };

  const currentLocation = useSelector((state) => state.session.currentLocation);

  const getParams = ({
    offset,
    state,
    sortingParams,
  }) => {
    const {
      statusCategory, status, searchTerm, container, destination,
    } = filterParams;
    return _.omitBy({
      offset: `${offset}`,
      max: `${state.pageSize}`,
      ...sortingParams,
      statusCategory: statusCategory?.id ?? statusCategory,
      status: status && status.map(({ value }) => value),
      searchTerm,
      container: container?.id,
      destination: destination?.id,
    }, (value) => value === '' || value === undefined || value === null || (Array.isArray(value) && value.length === 0));
  };

  const {
    tableRef,
    fireFetchData,
    loading,
    tableData,
    onFetchHandler,
  } = useTableData({
    filterParams,
    url: PUTAWAY_TASK_API(currentLocation?.id),
    errorMessageId,
    defaultErrorMessage,
    defaultSorting,
    getParams,
  });

  const dispatch = useDispatch();
  const { translate } = useSelector((state) => ({
    translate: translateWithDefaultMessage(getTranslate(state.localize)),
  }));

  const rerunPutawayStrategy = async (id) => {
    dispatch(showSpinner());
    try {
      await apiClient.patch(`${PUTAWAY_TASK_API(currentLocation?.id)}/${id}`, { action: 'rerunStrategy' });
      const successMessage = translate(
        'react.putawayTask.rerunStrategy.success.label',
        'Putaway strategy has been rerun successfully',
      );
      Alert.success(successMessage);
    } finally {
      dispatch(hideSpinner());
      fireFetchData();
    }
  };

  const rerunHandler = (id) => {
    confirmAlert({
      title: translate('react.default.areYouSure.label', 'Are you sure?'),
      message: translate(
        'react.putawayTask.rerunStrategy.confirm.label',
        'Are you sure you want to rerun the putaway strategy? The current task will be replaced with new strategy results.',
      ),
      buttons: [
        {
          label: translate('react.default.yes.label', 'Yes'),
          onClick: () => rerunPutawayStrategy(id),
        },
        {
          label: translate('react.default.no.label', 'No'),
        },
      ],
    });
  };

  return {
    onFetchHandler,
    rerunHandler,
    loading,
    tableData,
    tableRef,
  };
};

export default usePutawayTaskListTableData;
