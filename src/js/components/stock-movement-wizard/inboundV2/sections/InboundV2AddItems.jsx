import React from 'react';

import PropTypes from 'prop-types';
import { useFieldArray, useWatch } from 'react-hook-form';
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
import useInboundAddItemsColumns from 'hooks/inboundV2/addItems/useInboundAddItemsColumns';
import useResetScrollbar from 'hooks/useResetScrollbar';
import { FormErrorPropType } from 'utils/propTypes';

const InboundV2AddItems = ({
  control,
  errors,
  trigger,
  getValues,
  setValue,
  loading,
  save,
  removeItem,
  updateTotalCount,
  removeAll,
  saveAndExit,
  previousPage,
  refreshFocusCounter,
  resetFocus,
  refresh,
  importTemplate,
  exportTemplate,
  nextPage,
}) => {
  const hasErrors = !!Object.keys(errors).length;
  const {
    fields,
    remove,
    append,
  } = useFieldArray({
    control,
    name: 'values.lineItems',
  });
  const currentLineItems = useWatch({
    control,
    name: 'currentLineItems',
  });

  const updatedRows = useWatch({
    name: 'values.lineItems',
    control,
  });
  const { resetScrollbar } = useResetScrollbar({
    selector: '.rt-table',
  });

  const { columns } = useInboundAddItemsColumns({
    errors,
    control,
    remove,
    trigger,
    getValues,
    setValue,
    removeItem,
    updateTotalCount,
    removeAll,
    currentLineItems,
    append,
    refreshFocusCounter,
  });

  const defaultTableRow = {
    palletName: '',
    boxName: '',
    product: undefined,
    lotNumber: '',
    expirationDate: '',
    quantityRequested: undefined,
    recipient: undefined,
  };

  const getNextSortOrder = () => {
    const maxSortOrder = Math.max(0, ...fields.map(item => item.sortOrder || 0));
    return maxSortOrder + 100;
  };
  const addNewLine = () => {
    const newRow = {
      ...defaultTableRow,
      sortOrder: getNextSortOrder(),
    };
    append(newRow);
    resetScrollbar();
    resetFocus();
  };

  return (
    <>
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
                onClick={removeAll}
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
            data={fields}
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
          onClick={nextPage}
          disabled={!updatedRows.some(item =>
            item.product && parseInt(item.quantityRequested, 10))}
        />
      </div>
    </>
  );
};

export default InboundV2AddItems;

InboundV2AddItems.propTypes = {
  control: PropTypes.shape({}).isRequired,
  errors: PropTypes.shape({
    lineItems: FormErrorPropType,
  }).isRequired,
  trigger: PropTypes.func.isRequired,
  getValues: PropTypes.func.isRequired,
  setValue: PropTypes.func.isRequired,
  loading: PropTypes.bool.isRequired,
  save: PropTypes.func.isRequired,
  removeItem: PropTypes.func.isRequired,
  updateTotalCount: PropTypes.func.isRequired,
  removeAll: PropTypes.func.isRequired,
  saveAndExit: PropTypes.func.isRequired,
  previousPage: PropTypes.func.isRequired,
  refresh: PropTypes.func.isRequired,
  importTemplate: PropTypes.func.isRequired,
  exportTemplate: PropTypes.func.isRequired,
  refreshFocusCounter: PropTypes.number.isRequired,
  resetFocus: PropTypes.func.isRequired,
  nextPage: PropTypes.func.isRequired,
};
