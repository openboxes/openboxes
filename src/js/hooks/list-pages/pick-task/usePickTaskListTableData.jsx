import { useSelector } from 'react-redux';

import { PICK_TASK_API } from 'api/urls';
import useTableData from 'hooks/list-pages/useTableData';

// Fixed to status=PICKED: this page only shows outstanding staging work, so there's no filter UI.
// Declared once at module scope so its reference is stable across renders - useTableData's
// fetch effect depends on filterParams by reference. And a fresh object literal on every render
// would re-trigger the fetch effect after every fetch's state update, looping forever.
const FILTER_PARAMS = { status: 'PICKED' };

const usePickTaskListTableData = () => {
  const currentLocation = useSelector((state) => state.session.currentLocation);

  const getParams = ({ offset, state }) => ({
    offset: `${offset}`,
    max: `${state.pageSize}`,
    status: 'PICKED',
  });

  return useTableData({
    filterParams: FILTER_PARAMS,
    url: PICK_TASK_API(currentLocation?.id),
    errorMessageId: 'react.pickTask.fetch.fail.label',
    defaultErrorMessage: 'Unable to fetch pick tasks',
    defaultSorting: {},
    getParams,
  });
};

export default usePickTaskListTableData;
