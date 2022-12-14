import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';

import { CancelToken } from 'axios';
import _ from 'lodash';
import PropTypes from 'prop-types';
import queryString from 'query-string';
import { confirmAlert } from 'react-confirm-alert';
import {
  RiDeleteBinLine,
  RiInformationLine,
} from 'react-icons/all';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import Alert from 'react-s-alert';

import { hideSpinner, showSpinner } from 'actions';
import DataTable, { TableCell } from 'components/DataTable';
import StockTransferStatus from 'components/stock-transfer/list/StockTransferStatus';
import ActionDots from 'utils/ActionDots';
import apiClient from 'utils/apiClient';
import { findActions } from 'utils/list-utils';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

import 'react-confirm-alert/src/react-confirm-alert.css';

const INITIAL_STATE = {
  stockTransfersData: [],
  loading: false,
  pages: -1,
  totalCount: 0,
  currentParams: {},
};


const StockTransferListTable = ({
  filterParams,
  currentLocation,
  translate,
  showTheSpinner,
  hideTheSpinner,
  highestRole,
}) => {
  const [loading, setLoading] = useState(false);
  const [tableData, setTableData] = useState(INITIAL_STATE);
  // Util ref for react-table to force the fetch of data
  const tableRef = useRef(null);

  // Cancel token/signal for fetching data
  const sourceRef = useRef(CancelToken.source());

  useEffect(() => () => {
    if (currentLocation?.id) {
      sourceRef.current.cancel('Fetching canceled');
    }
  }, [currentLocation?.id]);

  const fireFetchData = () => {
    sourceRef.current = CancelToken.source();
    tableRef.current.fireFetchData();
  };
  // If filterParams change, refetch the data with applied filters
  useEffect(() => {
    fireFetchData();
  }, [filterParams]);

  const deleteStockTransfer = (id) => {
    showTheSpinner();
    apiClient.delete(`/openboxes/api/stockTransfers/${id}`)
      .then((res) => {
        if (res.status === 204) {
          hideTheSpinner();
          const successMessage = translate('react.stockTransfer.delete.success.label', 'Stock transfer has been deleted successfully');
          Alert.success(successMessage);
          fireFetchData();
        }
      })
      .catch(() => {
        hideTheSpinner();
        fireFetchData();
      });
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

  const actions = useMemo(() => [
    {
      label: 'react.stockTransfer.view.label',
      defaultLabel: 'View stock transfer',
      leftIcon: <RiInformationLine />,
      href: '/openboxes/stockTransfer/show',
    },
    {
      label: 'react.stockTransfer.delete.label',
      defaultLabel: 'Delete',
      leftIcon: <RiDeleteBinLine />,
      minimumRequiredRole: 'Manager',
      statuses: ['PENDING', 'APPROVED'],
      variant: 'danger',
      onClick: id => deleteHandler(id),
    },
  ], []);


  // Columns for react-table
  const columns = useMemo(() => [
    {
      Header: ' ',
      width: 50,
      sortable: false,
      className: 'active-circle d-flex align-items-center',
      style: { overflow: 'visible', zIndex: 1 },
      Cell: row => (
        <ActionDots
          dropdownPlacement="right"
          dropdownClasses="action-dropdown-offset"
          actions={findActions(actions, row, { highestRole })}
          id={row.original.id}
        />),
    },
    {
      Header: <Translate id="react.stockTransfer.column.lineItems.label" defaultMessage="Line items" />,
      accessor: 'orderItemsCount',
      className: 'd-flex align-items-center justify-content-center',
      sortable: false,
      maxWidth: 100,
      Cell: row => (<span className="items-count-circle d-flex align-items-center justify-content-center align-self-center">{row.original.orderItemsCount}</span>),
    },
    {
      Header: <Translate id="react.stockTransfer.column.status.label" defaultMessage="Status" />,
      accessor: 'status',
      className: 'active-circle d-flex',
      headerClassName: 'header',
      Cell: row => <StockTransferStatus status={row.original.status} />,
      maxWidth: 250,
    },
    {
      Header: <Translate id="react.stockTransfer.column.transferNumber.label" defaultMessage="Transfer number" />,
      accessor: 'orderNumber',
      className: 'active-circle d-flex align-items-center',
      headerClassName: 'header',
      Cell: row => <TableCell {...row} link={`/openboxes/stockTransfer/show/${row.original.id}`} />,
    },
    {
      Header: <Translate id="react.stockTransfer.column.createdBy.label" defaultMessage="Created by" />,
      accessor: 'createdBy',
      className: 'active-circle d-flex align-items-center',
      headerClassName: 'header',
    },
    {
      Header: <Translate id="react.stockTransfer.column.dateCreated.label" defaultMessage="Date created" />,
      accessor: 'dateCreated',
      className: 'd-flex align-items-center',
      Cell: row => <TableCell {...row} tooltip />,
    },
  ], []);


  const onFetchHandler = useCallback((tableState) => {
    if (!_.isEmpty(filterParams)) {
      const offset = tableState.page > 0 ? (tableState.page) * tableState.pageSize : 0;
      const sortingParams = tableState.sorted.length > 0 ?
        {
          sort: tableState.sorted[0].id,
          order: tableState.sorted[0].desc ? 'desc' : 'asc',
        } :
        {
          sort: 'dateCreated',
          order: 'desc',
        };

      const params = _.omitBy({
        location: currentLocation?.id,
        offset: `${offset}`,
        max: `${tableState.pageSize}`,
        ...sortingParams,
        ...filterParams,
        createdBy: filterParams.createdBy?.id,
        status: filterParams.status && filterParams.status.map(({ value }) => value),
      }, _.isEmpty);

      // Fetch data
      setLoading(true);
      apiClient.get('/openboxes/api/stockTransfers', {
        params,
        paramsSerializer: parameters => queryString.stringify(parameters),
        cancelToken: sourceRef.current?.token,
      })
        .then((res) => {
          setTableData({
            stockTransfersData: res.data.data,
            totalCount: res.data.totalCount,
            pages: Math.ceil(res.data.totalCount / tableState.pageSize),
            currentParams: params,
          });
          setLoading(false);
        })
        .catch(() => {
          setLoading(false);
          return Promise.reject(new Error(translate('react.stockTransfer.fetch.fail.label', 'Unable to fetch stock transfers')));
        });
    }
  }, [filterParams]);


  return (
    <div className="list-page-list-section">
      <div className="title-text p-3 d-flex justify-content-between align-items-center">
        <span>
          <Translate id="react.stockTransfer.list.label" defaultMessage="List Stock Transfers" />
        </span>
      </div>
      <DataTable
        manual
        sortable
        ref={tableRef}
        columns={columns}
        data={tableData.stockTransfersData}
        loading={loading}
        defaultPageSize={10}
        pages={tableData.pages}
        totalData={tableData.totalCount}
        onFetchData={onFetchHandler}
        noDataText="No stock transfers match the given criteria"
      />
    </div>
  );
};

const mapStateToProps = state => ({
  highestRole: state.session.highestRole,
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  currentLocation: state.session.currentLocation,
});

const mapDispatchToProps = {
  showTheSpinner: showSpinner,
  hideTheSpinner: hideSpinner,
};

export default connect(mapStateToProps, mapDispatchToProps)(StockTransferListTable);


StockTransferListTable.propTypes = {
  filterParams: PropTypes.shape({}).isRequired,
  translate: PropTypes.func.isRequired,
  currentLocation: PropTypes.shape({}).isRequired,
  showTheSpinner: PropTypes.func.isRequired,
  hideTheSpinner: PropTypes.func.isRequired,
  highestRole: PropTypes.string.isRequired,
};
