import React from 'react';

import filterFields from 'components/putaway-task/list/FilterFields';
import PutawayTaskListFilters from 'components/putaway-task/list/PutawayTaskListFilters';
import PutawayTaskListHeader from 'components/putaway-task/list/PutawayTaskListHeader';
import PutawayTaskListTable from 'components/putaway-task/list/PutawayTaskListTable';
import usePutawayTaskFilters from 'hooks/list-pages/putaway-task/usePutawayTaskFilters';
import useTranslation from 'hooks/useTranslation';

const PutawayTaskList = () => {
  const {
    setFilterValues, filterByOrder, defaultFilterValues, filterParams,
  } = usePutawayTaskFilters();

  useTranslation('putawayTask', 'reactTable');

  return (
    <div className="d-flex flex-column list-page-main">
      <PutawayTaskListHeader />
      <PutawayTaskListFilters
        filterFields={filterFields}
        setFilterParams={setFilterValues}
        defaultValues={defaultFilterValues}
      />
      <PutawayTaskListTable
        filterParams={filterParams}
        onFilterByOrder={filterByOrder}
      />
    </div>
  );
};

export default PutawayTaskList;
