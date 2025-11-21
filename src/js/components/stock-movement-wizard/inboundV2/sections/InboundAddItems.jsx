import React from 'react';

import PropTypes from 'prop-types';
import { useWatch } from 'react-hook-form';
import {
  RiAddLine,
  RiCloseCircleLine,
  RiDownload2Line,
  RiPictureInPictureExitLine,
  RiRefreshLine,
  RiSave2Line,
  RiUpload2Line,
} from 'react-icons/ri';

import DataTable from 'components/DataTable/v2/DataTable';
import Button from 'components/form-elements/Button';
import Section from 'components/Layout/v2/Section';
import ConfirmDuplicatedItemsModal from 'components/modals/ConfirmDuplicatedItemsModal';
import ConfirmExpirationDateModal from 'components/modals/ConfirmExpirationDateModal';
import modalWithTableType from 'consts/modalWithTableType';
import useInboundAddItemsColumns from 'hooks/inboundV2/addItems/useInboundAddItemsColumns';
import useInboundAddItemsForm from 'hooks/inboundV2/addItems/useInboundAddItemsForm';

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
    importTemplate,
    exportTemplate,
    addNewLine,
    removeSavedRow,
    removeRow,
    lineItemsArrayFields,
    isModalOpen,
    modalData,
    modalType,
    handleModalResponse,
  } = useInboundAddItemsForm({ next, previous });
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
      <Section>
        <div className="inbound-add-items">
          <div className="d-flex justify-content-between align-items-center mb-3">
            <Button
              onClick={addNewLine}
              StartIcon={<RiAddLine className="icon" />}
              defaultLabel="Add line"
              label="react.default.button.addLine.label"
            />

            <div className="buttons-container">
              <Button
                onClick={() => {
                  const fileInput = document.getElementById('csvInput');
                  // Resetting fileInput.value to null ensures the onChange
                  // event triggers even if the same file is selected again,
                  // as browsers don't fire onChange for unchanged file inputs
                  fileInput.value = null;
                  fileInput?.click();
                }}
                StartIcon={<RiDownload2Line className="icon" />}
                defaultLabel="Import template"
                label="react.default.button.importTemplate.label"
                variant="primary-outline"
              />
              <input
                id="csvInput"
                type="file"
                className="d-none"
                onChange={importTemplate}
                accept=".csv"
              />
              <Button
                onClick={exportTemplate}
                StartIcon={<RiUpload2Line className="icon" />}
                defaultLabel="Export template"
                label="react.default.button.exportTemplate.label"
                variant="primary-outline"
              />
              <Button
                onClick={refresh}
                StartIcon={<RiRefreshLine className="icon" />}
                defaultLabel="Reload"
                label="react.default.button.refresh.label"
                variant="primary-outline"
              />
              <Button
                onClick={save}
                StartIcon={<RiSave2Line className="icon" />}
                defaultLabel="Save"
                label="react.default.button.save.label"
                variant="primary-outline"
                disabled={hasErrors}
              />
              <Button
                onClick={saveAndExit}
                StartIcon={<RiPictureInPictureExitLine className="icon" />}
                defaultLabel="Save And Exit"
                label="react.default.button.saveAndExit.label"
                variant="primary-outline"
                disabled={hasErrors}
              />
              <Button
                onClick={removeAllRows}
                StartIcon={<RiCloseCircleLine className="icon" />}
                defaultLabel="Delete All"
                label="react.default.button.deleteAll.label"
                variant="primary-outline"
                disabled={hasErrors}
              />
            </div>
          </div>
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
            item.product && parseInt(item.quantityRequested, 10))}
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
