import React, { useMemo } from 'react';

import { RiAlertLine } from 'react-icons/ri';
import { getTranslate } from 'react-localize-redux';
import { useSelector } from 'react-redux';
import { Tooltip } from 'react-tippy';

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
      Cell: (row) => (
        <span className="d-flex align-items-center justify-content-end">
          {row.value}
          {row.original.hasStockIssue ? (
            <Tooltip
              arrow="true"
              delay="150"
              duration="250"
              hideDelay="50"
              html={translate(
                'react.dashboard.readyToStage.issue.tooltip.label',
                'The picked quantity may no longer be available at this location - it may have been transferred or removed from stock since it was picked',
              )}
              className="cursor-help"
            >
              <RiAlertLine className="text-warning ml-1" data-testid="pick-task-stock-issue-icon" />
            </Tooltip>
          ) : (
            // Invisible placeholder so the quantity lines up whether or not the icon shows.
            <RiAlertLine className="invisible ml-1" aria-hidden="true" />
          )}
        </span>
      ),
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
  ], [translate]);

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
