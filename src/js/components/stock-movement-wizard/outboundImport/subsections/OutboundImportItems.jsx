import React from 'react';

import PropTypes from 'prop-types';

import DataTable from 'components/DataTable';
import Subsection from 'components/Layout/v2/Subsection';
import InvalidItemsIndicator from 'components/productSupplier/create/InvalidItemsIndicator';
import ItemInStockModal from 'components/stock-movement-wizard/modals/ItemInStockModal';
import useOutboundImportItems from 'hooks/outboundImport/useOutboundImportItems';

const OutboundImportItems = ({
  data,
  errors,
}) => {
  const { itemsWithErrors, itemsInOrder } = data;

  const {
    columns,
    isFiltered,
    setIsFiltered,
    getFilteredTableData,
    getTablePageSize,
    toggleFiltering,
    handleCloseItemInStockModal,
    isItemInStockModalOpen,
    selectedItemInStock,
  } = useOutboundImportItems({ itemsInOrder });

  return (
    <>
      <ItemInStockModal
        item={selectedItemInStock}
        isOpen={isItemInStockModalOpen}
        onCancel={handleCloseItemInStockModal}
      />
      <Subsection
        title={{
          label: 'react.outboundImport.steps.items.label',
          defaultMessage: 'Items',
        }}
        collapsable={false}
      >
        <span>
          {itemsWithErrors?.length > 0
          && (
            <InvalidItemsIndicator
              className="mr-3"
              errorsCounter={itemsWithErrors?.length}
              isFiltered={isFiltered}
              setIsFiltered={setIsFiltered}
              triggerValidation={null}
              handleOnFilterButtonClick={toggleFiltering}
            />
          )}
        </span>
        <DataTable
          style={{ maxHeight: '20rem' }}
          showPagination={false}
          pageSize={getTablePageSize(itemsWithErrors, itemsInOrder)}
          columns={columns}
          errors={errors}
          data={getFilteredTableData(itemsWithErrors, itemsInOrder)}
          loading={false}
        />
      </Subsection>
    </>
  );
};

export default OutboundImportItems;

OutboundImportItems.defaultProps = {
  data: [],
  errors: {},
};

OutboundImportItems.propTypes = {
  errors: PropTypes.shape({}),
  data: PropTypes.shape({
    itemsWithErrors: PropTypes.arrayOf(PropTypes.shape({
      product: PropTypes.shape({
        id: PropTypes.string,
        productCode: PropTypes.string,
      }),
      lotNumber: PropTypes.string,
      quantityPicked: PropTypes.number,
      binLocation: PropTypes.shape({
        id: PropTypes.string,
        name: PropTypes.string,
        zone: PropTypes.shape({
          id: PropTypes.string,
          name: PropTypes.string,
        }),
      }),
      recipient: PropTypes.shape({
        id: PropTypes.string,
        name: PropTypes.string,
        firstName: PropTypes.string,
        lastName: PropTypes.string,
        username: PropTypes.string,
      }),
    })),
    itemsInOrder: PropTypes.arrayOf(PropTypes.shape({
      product: PropTypes.shape({
        id: PropTypes.string,
        productCode: PropTypes.string,
      }),
      lotNumber: PropTypes.string,
      quantityPicked: PropTypes.number,
      binLocation: PropTypes.shape({
        id: PropTypes.string,
        name: PropTypes.string,
        zone: PropTypes.shape({
          id: PropTypes.string,
          name: PropTypes.string,
        }),
      }),
      recipient: PropTypes.shape({
        id: PropTypes.string,
        name: PropTypes.string,
        firstName: PropTypes.string,
        lastName: PropTypes.string,
        username: PropTypes.string,
      }),
    })),
  }),
};
