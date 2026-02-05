import React from 'react';

import PropTypes from 'prop-types';
import { useWatch } from 'react-hook-form';

import DataTable from 'components/DataTable/v2/DataTable';
import Section from 'components/Layout/v2/Section';
import ConfirmDuplicatedItemsModal from 'components/modals/ConfirmDuplicatedItemsModal';
import ConfirmExpirationDateModal from 'components/modals/ConfirmExpirationDateModal';
import InboundAddItemsHeader from 'components/stock-movement-wizard/inboundV2/sections/addItems/InboundAddItemsHeader';
import InboundAddItemsNavigationButtons
  from 'components/stock-movement-wizard/inboundV2/sections/addItems/InboundAddItemsNavigationButtons';
import modalWithTableType from 'consts/modalWithTableType';
import useInboundAddItemsForm from 'hooks/inboundV2/addItems/useInboundAddItemsForm';

const InboundAddItems = ({
  next,
  previous,
}) => {
  const {
    form: {
      control, errors,
    },
    table: {
      lineItemsArrayFields, columns,
    },
    actions: {
      loading, addNewLine, removeAllRows, nextPage, previousPage, save, saveAndExit, refresh,
    },
    modal: {
      isModalOpen, modalData, modalType, handleModalResponse,
    },
    importExport: { importTemplate, exportTemplate },
  } = useInboundAddItemsForm({ next, previous });
  const hasErrors = Boolean(Object.keys(errors).length);

  const lineItems = useWatch({
    name: 'values.lineItems',
    control,
  });

  const isNextButtonEnabled = lineItems?.some((item) =>
    item.product && item.quantityRequested && parseInt(item.quantityRequested, 10) > 0);

  return (
    <form>
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
              totalCount={lineItemsArrayFields.length}
              emptyTableMessage={{
                id: 'react.stockMovement.emptyTable.label',
                defaultMessage: 'No items to display',
              }}
              overflowVisible
            />
          </div>
        </div>
      </Section>
      <InboundAddItemsNavigationButtons
        onPrevious={previousPage}
        onNext={nextPage}
        isPreviousDisabled={hasErrors}
        isNextDisabled={!isNextButtonEnabled}
      />
      <ConfirmExpirationDateModal
        isOpen={isModalOpen && modalType === modalWithTableType.EXPIRATION}
        data={modalData}
        onConfirm={() => handleModalResponse(true)}
        onCancel={() => handleModalResponse(false)}
      />
      <ConfirmDuplicatedItemsModal
        isOpen={isModalOpen && modalType === modalWithTableType.DUPLICATES}
        data={modalData}
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
