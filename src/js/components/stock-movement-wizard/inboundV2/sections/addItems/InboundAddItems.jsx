import React from 'react';

import PropTypes from 'prop-types';
import { useWatch } from 'react-hook-form';

import DataTable from 'components/DataTable/v2/DataTable';
import Button from 'components/form-elements/Button';
import Section from 'components/Layout/v2/Section';
import ConfirmDuplicatedItemsModal from 'components/modals/ConfirmDuplicatedItemsModal';
import ConfirmExpirationDateModal from 'components/modals/ConfirmExpirationDateModal';
import InboundAddItemsHeader from 'components/stock-movement-wizard/inboundV2/sections/addItems/InboundAddItemsHeader';
import modalWithTableType from 'consts/modalWithTableType';
import useInboundAddItemsColumns from 'hooks/inboundV2/addItems/useInboundAddItemsColumns';
import useInboundAddItemsForm from 'hooks/inboundV2/addItems/useInboundAddItemsForm';
import useInboundAddItemsImportExport from 'hooks/inboundV2/addItems/useInboundAddItemsImportExport';

const InboundAddItems = ({
  next,
  previous,
}) => {
  const {
    control,
    handleSubmit,
    errors,
    trigger,
    getValues,
    setValue,
    loading,
    nextPage,
    save,
    removeAllRows,
    saveAndExit,
    previousPage,
    refresh,
    addNewLine,
    removeSavedRow,
    removeRow,
    lineItemsArrayFields,
    isModalOpen,
    modalData,
    modalType,
    handleModalResponse,
    fetchLineItems,
    saveRequisitionItemsInCurrentStep,
    defaultTableRow,
  } = useInboundAddItemsForm({ next, previous });

  const {
    importTemplate,
    exportTemplate,
  } = useInboundAddItemsImportExport({
    getValues,
    setValue,
    fetchLineItems,
    saveRequisitionItemsInCurrentStep,
    defaultTableRow,
  });
  const hasErrors = !!Object.keys(errors).length;

  const lineItems = useWatch({
    name: 'values.lineItems',
    control,
  });

  const { columns } = useInboundAddItemsColumns({
    errors,
    control,
    removeSavedRow,
    trigger,
    getValues,
    setValue,
    removeRow,
    addNewLine,
  });

  return (
    <form onSubmit={handleSubmit(nextPage)}>
      <Section showTitle={false}>
        <div className="inbound-add-items">
          <InboundAddItemsHeader
            addNewLine={addNewLine}
            importTemplate={importTemplate}
            exportTemplate={exportTemplate}
            refresh={refresh}
            save={save}
            saveAndExit={saveAndExit}
            removeAllRows={removeAllRows}
            hasErrors={hasErrors}
          />
          <div data-testid="items-table">
            <DataTable
              columns={columns}
              data={lineItemsArrayFields}
              loading={loading}
              disablePagination
              emptyTableMessage={{
                id: 'react.stockMovement.emptyTable.label',
                defaultMessage: 'No items to display',
              }}
              overflowVisible
            />
          </div>
        </div>
      </Section>
      <div className="submit-buttons">
        <Button
          label="react.default.button.previous.label"
          defaultLabel="Previous"
          variant="primary"
          onClick={previousPage}
          disabled={hasErrors}
        />
        <Button
          label="react.default.button.next.label"
          defaultLabel="Next"
          variant="primary"
          disabled={!lineItems.some(item =>
            item.product && item.quantityRequested && parseInt(item.quantityRequested, 10) > 0)}
          type="submit"
        />
      </div>
      <ConfirmExpirationDateModal
        isOpen={isModalOpen && modalType === modalWithTableType.EXPIRATION}
        data={modalData || []}
        onConfirm={() => handleModalResponse(true)}
        onCancel={() => handleModalResponse(false)}
      />
      <ConfirmDuplicatedItemsModal
        isOpen={isModalOpen && modalType === modalWithTableType.DUPLICATES}
        data={modalData || []}
        onConfirm={() => handleModalResponse(true)}
        onCancel={() => handleModalResponse(false)}
      />
    </form>
  );
};

export default InboundAddItems;

InboundAddItems.propTypes = {
  next: PropTypes.func.isRequired,
  previous: PropTypes.func.isRequired,
};
