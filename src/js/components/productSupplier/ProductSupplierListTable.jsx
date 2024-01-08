import React, { useMemo, useState } from 'react';

import PropTypes from 'prop-types';

import { RiDeleteBinLine, RiPencilLine } from 'react-icons/ri';
import { useSelector } from 'react-redux';

import productSupplierApi from 'api/services/ProductSupplierApi';
import DataTable, { TableCell } from 'components/DataTable';
import DateCell from 'components/DataTable/DateCell';
import PreferenceTypeColumn from 'components/productSupplier/PreferenceTypeColumn';
import { PRODUCT_SUPPLIER_URL } from 'consts/applicationUrls';
import useProductSupplierListTableData from 'hooks/list-pages/productSupplier/useProductSupplierListTableData';
import ActionDots from 'utils/ActionDots';
import CustomModal from 'utils/CustomModal';
import { hasPermissionsToProductSourceActions } from 'utils/permissionUtils';
import StatusIndicator from 'utils/StatusIndicator';
import Translate from 'utils/Translate';
import ListTableTitleWrapper from 'wrappers/ListTableTitleWrapper';
import ListTableWrapper from 'wrappers/ListTableWrapper';

const ProductSupplierListTable = ({ filterParams }) => {
  const [isDeleteConfirmationOpened, setIsDeleteConfirmationOpened] = useState(false);
  const [selectedProductSupplierId, setSelectedProductSupplierId] = useState(null);

  const { currentUser } = useSelector((state) => ({
    currentUser: state.session.user,
  }));

  const closeDeleteConfirmationModal = () => {
    setIsDeleteConfirmationOpened(false);
  };

  const deleteProductSupplier = async () => {
    try {
      await productSupplierApi.deleteProductSupplier(selectedProductSupplierId);
    } finally {
      setIsDeleteConfirmationOpened(false);
    }
  };

  const deleteConfirmationModalButtons = [
    {
      variant: 'transparent',
      defaultLabel: 'Cancel',
      label: 'Cancel',
      onClick: closeDeleteConfirmationModal,
    },
    {
      variant: 'danger',
      defaultLabel: 'Delete',
      label: '',
      onClick: deleteProductSupplier,
    },
  ];

  const getActions = (id) => [
    {
      defaultLabel: 'Edit',
      label: 'react.productSupplier.edit.label',
      leftIcon: <RiPencilLine />,
      onClick: () => {
        window.location = PRODUCT_SUPPLIER_URL.edit(id);
      },
    },
    {
      defaultLabel: 'Delete Product Source',
      label: 'react.productSupplier.delete.label',
      leftIcon: <RiDeleteBinLine />,
      variant: 'danger',
      onClick: () => {
        setIsDeleteConfirmationOpened(true);
        setSelectedProductSupplierId(id);
      },
    },
  ];

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
      Cell: (row) => {
        const clickableActions = hasPermissionsToProductSourceActions(currentUser)
          ? { actions: getActions(row.original.id) }
          : {};

        return (
          <ActionDots
            dropdownPlacement="right"
            dropdownClasses="action-dropdown-offset"
            id={row.original.id}
            {...clickableActions}
          />
        );
      },
    },
    {
      Header: <Translate id="react.productSupplier.column.productCode.label" defaultMessage="Product Code" />,
      accessor: 'productCode',
      width: 160,
      fixed: 'left',
    },
    {
      Header: <Translate id="react.productSupplier.column.productName.label" defaultMessage="Product Name" />,
      accessor: 'productName',
      minWidth: 350,
      fixed: 'left',
      Cell: (row) => <TableCell {...row} tooltip />,
    },
    {
      Header: <Translate id="react.productSupplier.column.sourceCode.label" defaultMessage="Source Code" />,
      accessor: 'code',
      minWidth: 200,
    },
    {
      Header: <Translate id="react.productSupplier.column.preferenceType.label" defaultMessage="Preference Type" />,
      Cell: (row) => (
        <PreferenceTypeColumn
          productSupplierId={row.original.id}
          productSupplierPreferences={row.original.productSupplierPreferences}
        />
      ),
      minWidth: 250,
      sortable: false,
    },
    {
      Header: <Translate id="react.productSupplier.column.defaultPackSize.label" defaultMessage="Default Pack Size" />,
      accessor: 'packageSize',
      minWidth: 250,
      sortable: false,
    },
    {
      Header: <Translate id="react.productSupplier.column.packagePrice.label" defaultMessage="Package Price" />,
      accessor: 'packagePrice',
      minWidth: 200,
      sortable: false,
    },
    {
      Header: <Translate id="react.productSupplier.column.eachPrice.label" defaultMessage="Each Price" />,
      accessor: 'eachPrice',
      minWidth: 150,
      sortable: false,
    },
    {
      Header: <Translate id="react.productSupplier.column.supplier.label" defaultMessage="Supplier" />,
      accessor: 'supplierName',
      minWidth: 300,
    },
    {
      Header: <Translate id="react.productSupplier.column.supplierCode.label" defaultMessage="Supplier Code" />,
      accessor: 'supplierCode',
      minWidth: 150,
    },
    {
      Header: <Translate id="react.productSupplier.column.supplierProductName.label" defaultMessage="Supplier Product Name" />,
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

  const {
    tableRef,
    tableData,
    onFetchHandler,
    loading,
  } = useProductSupplierListTableData(filterParams);

  return (
    <>
      <CustomModal
        titleLabel="react.productSupplier.deleteConfirmation.title.label"
        defaultTitle="Are you sure?"
        contentLabel="react.productSupplier.deleteConfirmation.content.label"
        defaultContent="Are you sure you want to delete this Product Source?"
        isOpen={isDeleteConfirmationOpened}
        onClose={closeDeleteConfirmationModal}
        buttons={deleteConfirmationModalButtons}
      />
      <ListTableWrapper>
        <ListTableTitleWrapper>
          <span>
            <Translate id="react.productSupplier.listProductSources.label" defaultMessage="List Product Sources" />
            &nbsp;
            (
            {tableData?.totalCount}
            )
          </span>
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
    </>
  );
};

export default ProductSupplierListTable;

ProductSupplierListTable.propTypes = {
  filterParams: PropTypes.shape({}).isRequired,
};
