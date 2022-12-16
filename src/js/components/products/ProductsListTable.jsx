import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';

import fileDownload from 'js-file-download';
import _ from 'lodash';
import PropTypes from 'prop-types';
import queryString from 'query-string';
import { RiDownload2Line } from 'react-icons/all';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';

import DataTable, { TableCell } from 'components/DataTable';
import Button from 'components/form-elements/Button';
import apiClient from 'utils/apiClient';
import StatusIndicator from 'utils/StatusIndicator';
import Translate, { translateWithDefaultMessage } from 'utils/Translate';

const INITIAL_STATE = {
  productsData: [],
  pages: -1,
  totalCount: 0,
  currentParams: {},
};


const ProductsListTable = ({
  filterParams,
  translate,
}) => {
  // Util ref for react-table to force the fetch of data
  const tableRef = useRef(null);
  const fireFetchData = () => {
    tableRef.current.fireFetchData();
  };
  // If filterParams change, refetch the data with applied filters
  useEffect(() => {
    fireFetchData();
  }, [filterParams]);

  const [loading, setLoading] = useState(false);
  const [tableData, setTableData] = useState(INITIAL_STATE);


  // Columns for react-table
  const columns = useMemo(() => [
    {
      Header: <Translate id="react.productsList.column.active.label" defaultMessage="Active" />,
      accessor: 'active',
      className: 'active-circle d-flex justify-content-center',
      headerClassName: 'header justify-content-center',
      maxWidth: 150,
      Cell: row =>
        (<StatusIndicator
          variant={row.original.active ? 'success' : 'danger'}
          status={row.original.active ? 'Active' : 'Inactive'}
        />),
    },
    {
      Header: <Translate id="react.productsList.column.code.label" defaultMessage="Code" />,
      accessor: 'productCode',
      className: 'active-circle d-flex justify-content-center',
      headerClassName: 'header justify-content-center',
      Cell: row => <TableCell {...row} link={`/openboxes/inventoryItem/showStockCard/${row.original.id}`} />,
      maxWidth: 150,
    },
    {
      Header: <Translate id="react.productsList.column.name.label" defaultMessage="Name" />,
      accessor: 'name',
      className: 'active-circle',
      headerClassName: 'header',
      sortable: false,
      Cell: row => <TableCell {...row} tooltip link={`/openboxes/inventoryItem/showStockCard/${row.original.id}`} />,
      minWidth: 200,
    },
    {
      Header: <Translate id="react.productsList.filters.category.label" defaultMessage="Category" />,
      accessor: 'category',
      Cell: row => <TableCell {...row} tooltip />,
    },
    {
      Header: <Translate id="react.productsList.column.updatedBy.label" defaultMessage="Updated by" />,
      accessor: 'updatedBy',
    },
    {
      Header: <Translate id="react.productsList.column.lastUpdated.label" defaultMessage="Last updated" />,
      accessor: 'lastUpdated',
      maxWidth: 200,
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
          sort: 'lastUpdated',
          order: 'desc',
        };

      const params = _.omitBy({
        offset: `${offset}`,
        max: `${tableState.pageSize}`,
        ...sortingParams,
        ...filterParams,
        catalogId: filterParams.catalogId && filterParams.catalogId.map(({ id }) => id),
        categoryId: filterParams.categoryId && filterParams.categoryId.map(({ id }) => id),
        tagId: filterParams.tagId && filterParams.tagId.map(({ id }) => id),
      }, (val) => {
        if (typeof val === 'boolean') {
          return !val;
        }
        return _.isEmpty(val);
      });

      // Fetch data
      setLoading(true);
      apiClient.get('/openboxes/api/products', {
        params,
        paramsSerializer: parameters => queryString.stringify(parameters),
      })
        .then((res) => {
          setTableData({
            productsData: res.data.data,
            totalCount: res.data.totalCount,
            pages: Math.ceil(res.data.totalCount / tableState.pageSize),
            currentParams: params,
          });
          setLoading(false);
        })
        .catch(() => {
          setLoading(false);
          return Promise.reject(new Error(translate('react.productsList.fetch.fail.label', 'Unable to fetch products')));
        });
    }
  }, [filterParams]);

  const exportProducts = (allProducts = false, withAttributes = false) => {
    const params = () => {
      if (allProducts) {
        return { format: 'csv' };
      }
      if (withAttributes) {
        return { format: 'csv', includeAttributes: true };
      }
      return {
        ..._.omit(tableData.currentParams, ['offset', 'max']),
        format: 'csv',
      };
    };

    apiClient.get('/openboxes/api/products', {
      params: params(),
      paramsSerializer: parameters => queryString.stringify(parameters),
    })
      .then((res) => {
        const date = new Date();
        const [month, day, year] = [date.getMonth(), date.getDate(), date.getFullYear()];
        const [hour, minutes, seconds] = [date.getHours(), date.getMinutes(), date.getSeconds()];
        fileDownload(res.data, `Products-${year}${month}${day}-${hour}${minutes}${seconds}`, 'text/csv');
      });
  };


  return (
    <div className="list-page-list-section">
      <div className="title-text p-3 d-flex justify-content-between align-items-center">
        <span>
          <Translate id="react.productsList.header.label" defaultMessage="Product list" />
          &nbsp;
          ({tableData.totalCount})
        </span>
        <div className="btn-group">
          <Button
            isDropdown
            defaultLabel="Export"
            label="react.default.button.export.label"
            variant="secondary"
            EndIcon={<RiDownload2Line />}
          />
          <div className="dropdown-menu dropdown-menu-right nav-item padding-8" aria-labelledby="dropdownMenuButton">
            <a href="#" className="dropdown-item" onClick={() => exportProducts(false)} role="button" tabIndex={0}>
              <Translate
                id="react.productsList.exportResults.label"
                defaultMessage="Export results"
              />
            </a>
            <a className="dropdown-item" onClick={() => exportProducts(true)} href="#">
              <Translate
                id="react.productsList.exportProducts.label"
                defaultMessage="Export Products"
              />
            </a>
            <a className="dropdown-item" onClick={() => exportProducts(false, true)} href="#">
              <Translate
                id="react.productsList.exportProductAttrs"
                defaultMessage="Export Product Attributes"
              />
            </a>
          </div>
        </div>
      </div>
      <DataTable
        manual
        sortable
        ref={tableRef}
        columns={columns}
        data={tableData.productsData}
        loading={loading}
        defaultPageSize={10}
        pages={tableData.pages}
        totalData={tableData.totalCount}
        onFetchData={onFetchHandler}
        className="mb-1"
        noDataText="No products match the given criteria"
      />
    </div>
  );
};

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
});


export default connect(mapStateToProps)(ProductsListTable);


ProductsListTable.propTypes = {
  filterParams: PropTypes.shape({}).isRequired,
  translate: PropTypes.func.isRequired,
};
