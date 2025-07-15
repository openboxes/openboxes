import React, { useMemo } from 'react';

import PropTypes from 'prop-types';
import { RiDownload2Line } from 'react-icons/ri';

import DataTable, { TableCell } from 'components/DataTable';
import DateCell from 'components/DataTable/DateCell';
import Button from 'components/form-elements/Button';
import PreferenceTypeColumn from 'components/productSupplier/PreferenceTypeColumn';
import { INVENTORY_ITEM_URL, PRODUCT_SUPPLIER_URL } from 'consts/applicationUrls';
import RoleType from 'consts/roleType';
import useProductSupplierActions from 'hooks/list-pages/productSupplier/useProductSupplierActions';
import useProductSupplierListTableData from 'hooks/list-pages/productSupplier/useProductSupplierListTableData';
import useUserHasPermissions from 'hooks/useUserHasPermissions';
import ContextMenu from 'utils/ContextMenu';
import StatusIndicator from 'utils/StatusIndicator';
import Translate from 'utils/Translate';
import ListTableTitleWrapper from 'wrappers/ListTableTitleWrapper';
import ListTableWrapper from 'wrappers/ListTableWrapper';

const ProductSupplierListTable = ({ filterParams }) => {
  const {
    tableRef,
    tableData,
    onFetchHandler,
    loading,
    fireFetchData,
  } = useProductSupplierListTableData(filterParams);
  const {
    getActions,
    exportProductSuppliers,
  } = useProductSupplierActions({ fireFetchData, filterParams });

  const canManageProducts = useUserHasPermissions({
    minRequiredRole: RoleType.ROLE_ADMIN,
    supplementalRoles: [RoleType.ROLE_PRODUCT_MANAGER],
  });

  const columns = useMemo(() => [
    {
      Header: ' ',
      width: 50,
      sortable: false,
      style: {
        overflow: 'visible',
        zIndex: 1,
      },
      fixed: 'left',
      Cell: (row) => (
        <ContextMenu
          positions={['right']}
          dropdownClasses="action-dropdown-offset"
          id={row.original.id}
          actions={getActions(row.original.id)}
        />
      ),
    },
    {
      Header: <Translate id="react.productSupplier.column.productCode.label" defaultMessage="Product Code" />,
      accessor: 'product.productCode',
      width: 80,
      headerClassName: 'text-wrap text-left pl-1',
      className: 'pl-1',
      fixed: 'left',
    },
    {
      Header: <Translate id="react.productSupplier.column.productName.label" defaultMessage="Product Name" />,
      accessor: 'product.name',
      minWidth: 400,
      fixed: 'left',
      Cell: (row) =>
        (
          <TableCell
            {...row}
            tooltip
            openLinkInNewTab
            link={INVENTORY_ITEM_URL.showStockCard(row.original.product.id, { activeTab: 4 })}
          />
        ),
    },
    {
      Header: <Translate id="react.productSupplier.column.sourceCode.label" defaultMessage="Source Code" />,
      accessor: 'code',
      minWidth: 200,
      Cell: (row) =>
        (
          <TableCell
            {...row}
            link={
              canManageProducts
                ? PRODUCT_SUPPLIER_URL.edit(row.original.id)
                : undefined
            }
          />
        ),
    },
    {
      Header: <Translate id="react.productSupplier.column.preferenceType.label" defaultMessage="Preference Type" />,
      Cell: (row) => (
        <PreferenceTypeColumn
          productSupplierId={row.original.id}
          productSupplierPreferences={row.original.productSupplierPreferences}
        />
      ),
      minWidth: 180,
      sortable: false,
    },
    {
      Header: <Translate id="react.productSupplier.column.defaultPackSize.label" defaultMessage="Default Pack Size" />,
      accessor: 'packageSize',
      width: 90,
      headerClassName: 'text-wrap text-left',
      sortable: false,
    },
    {
      Header: <Translate id="react.productSupplier.column.packagePrice.label" defaultMessage="Package Price" />,
      accessor: 'packagePrice',
      width: 80,
      className: 'text-right',
      headerClassName: 'text-wrap text-left',
      sortable: false,
    },
    {
      Header: <Translate id="react.productSupplier.column.eachPrice.label" defaultMessage="Each Price" />,
      accessor: 'unitPrice',
      width: 80,
      className: 'text-right',
      headerClassName: 'text-wrap text-left',
      sortable: false,
    },
    {
      Header: <Translate id="react.productSupplier.column.supplier.label" defaultMessage="Supplier" />,
      accessor: 'supplier.displayName',
      minWidth: 300,
      Cell: (row) => (
        <TableCell
          {...row}
          tooltip
        />
      ),
    },
    {
      Header: <Translate id="react.productSupplier.column.supplierCode.label" defaultMessage="Supplier Code" />,
      accessor: 'supplierCode',
      minWidth: 150,
      sortable: false,
      Cell: (row) => (
        <TableCell
          {...row}
          tooltip
        />
      ),
    },
    {
      Header: <Translate id="react.productSupplier.column.name.label" defaultMessage="(Source) Name" />,
      accessor: 'name',
      minWidth: 350,
      Cell: (row) => <TableCell {...row} tooltip />,
    },
    {
      Header: <Translate id="react.productSupplier.column.dateCreated.label" defaultMessage="Date Created" />,
      accessor: 'dateCreated',
      minWidth: 150,
      Cell: (row) => (<DateCell {...row} />),
    },
    {
      Header: <Translate id="react.productSupplier.column.status.label" defaultMessage="Status" />,
      accessor: 'active',
      minWidth: 100,
      className: 'active-circle d-flex justify-content-center',
      headerClassName: 'header justify-content-center',
      maxWidth: 150,
      Cell: (row) => (
        <StatusIndicator
          variant={row.original.active ? 'success' : 'danger'}
          status={row.original.active ? 'Active' : 'Inactive'}
        />
      ),
    },
  ], []);

  return (
    <ListTableWrapper>
      <ListTableTitleWrapper>
        <span>
          <Translate id="react.productSupplier.listProductSources.label" defaultMessage="List Product Sources" />
          &nbsp;
          (
          {tableData?.totalCount}
          )
        </span>
        <div className="btn-group">
          <Button
            isDropdown
            defaultLabel="Export"
            label="react.default.button.export.label"
            variant="secondary"
            EndIcon={<RiDownload2Line />}
          />
          <div
            className="dropdown-menu dropdown-menu-right nav-item padding-8"
            aria-labelledby="dropdownMenuButton"
          >
            <a
              href="#"
              className="dropdown-item"
              onClick={() => exportProductSuppliers()}
              role="button"
              tabIndex={0}
            >
              <Translate
                id="react.productSupplier.exportAll.label"
                defaultMessage="Export All"
              />
            </a>
            <a
              href="#"
              className="dropdown-item"
              onClick={() => exportProductSuppliers(true)}
              role="button"
              tabIndex={0}
            >
              <Translate
                id="react.productSupplier.exportResults.label"
                defaultMessage="Export Results"
              />
            </a>
          </div>
        </div>
      </ListTableTitleWrapper>
      <DataTable
        manual
        sortable
        ref={tableRef}
        columns={columns}
        data={tableData.data}
        loading={loading}
        defaultPageSize={10}
        pages={tableData.pages}
        totalData={tableData.totalCount}
        onFetchData={onFetchHandler}
        noDataText="No product sources match the given criteria"
        footerComponent={() => (
          <span className="title-text p-1 d-flex flex-1 justify-content-end" />
        )}
      />
    </ListTableWrapper>
  );
};

export default ProductSupplierListTable;

ProductSupplierListTable.propTypes = {
  filterParams: PropTypes.shape({}).isRequired,
};
