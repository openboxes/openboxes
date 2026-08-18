import React, { useMemo } from 'react';

import { getTranslate } from 'react-localize-redux';
import { useSelector } from 'react-redux';

import DataTable, { TableCell } from 'components/DataTable';
import { STOCK_MOVEMENT_URL } from 'consts/applicationUrls';
import usePickTaskListTableData from 'hooks/list-pages/pick-task/usePickTaskListTableData';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

const PickTaskListTable = () => {
  const {
    onFetchHandler,
    loading,
    tableData,
    tableRef,
  } = usePickTaskListTableData();

  const translate = useSelector(
    (state) => translateWithDefaultMessage(getTranslate(state.localize)),
  );

  const columns = useMemo(() => [
    {
      Header: <Translate id="react.dashboard.readyToStage.order.label" defaultMessage="Order" />,
      accessor: 'requisitionNumber',
      className: 'd-flex align-items-center',
      headerClassName: 'header',
      Cell: (row) => {
        const { requisitionId } = row.original;
        return (
          <TableCell
            {...row}
            link={requisitionId ? STOCK_MOVEMENT_URL.show(requisitionId) : null}
          />
        );
      },
    },
    {
      Header: <Translate id="react.dashboard.readyToStage.product.label" defaultMessage="Product" />,
      accessor: 'product.name',
      className: 'd-flex align-items-center',
      headerClassName: 'header',
      minWidth: 200,
    },
    {
      Header: <Translate id="react.dashboard.readyToStage.quantityPicked.label" defaultMessage="Qty Picked" />,
      accessor: 'quantityPicked',
      className: 'd-flex align-items-center justify-content-end',
      headerClassName: 'header',
      maxWidth: 120,
    },
    {
      Header: <Translate id="react.dashboard.readyToStage.location.label" defaultMessage="Location" />,
      id: 'location',
      accessor: (row) => row.outboundContainer?.name ?? row.location?.name,
      className: 'd-flex align-items-center',
      headerClassName: 'header',
    },
    {
      Header: <Translate id="react.dashboard.readyToStage.picker.label" defaultMessage="Picker" />,
      accessor: 'pickedBy.name',
      className: 'd-flex align-items-center',
      headerClassName: 'header',
    },
  ], []);

  return (
    <div className="list-page-list-section">
      <div className="title-text p-3 d-flex justify-content-between align-items-center">
        <span>
          <Translate id="react.dashboard.readyToStage.title.label" defaultMessage="Ready to be Staged" />
        </span>
      </div>
      <DataTable
        manual
        sortable={false}
        resizable
        ref={tableRef}
        columns={columns}
        data={tableData.data}
        loading={loading}
        defaultPageSize={10}
        pages={tableData.pages}
        totalData={tableData.totalCount}
        onFetchData={onFetchHandler}
        noDataText={translate(
          'react.dashboard.readyToStage.empty.label',
          'There is no outstanding staging work',
        )}
      />
    </div>
  );
};

export default PickTaskListTable;
