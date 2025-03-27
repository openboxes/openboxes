import React from 'react';

import PropTypes from 'prop-types';
import { useFieldArray, useWatch } from 'react-hook-form';
import { BiExit, BiSave, IoCloseCircleOutline } from 'react-icons/all';
import { RiAddLine } from 'react-icons/ri';

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
                onClick={save}
                StartIcon={<BiSave className="icon" />}
                defaultLabel="Save"
                label="react.default.button.save.label"
                variant="primary-outline"
                disabled={hasErrors}
              />
              <Button
                onClick={saveAndExit}
                StartIcon={<BiExit className="icon" />}
                defaultLabel="Save And Exit"
                label="react.default.button.saveAndExit.label"
                variant="primary-outline"
                disabled={hasErrors}
              />
              <Button
                onClick={removeAll}
                StartIcon={<IoCloseCircleOutline className="icon" />}
                defaultLabel="Delete All"
                label="react.default.button.deleteAll.label"
                variant="primary-outline"
              />
            </div>
          </div>
          <DataTable
            columns={columns}
            data={fields}
            defaultPageSize={4}
            loading={loading}
            disablePagination
            emptyTableMessage={{
              id: 'react.stockMovement.emptyTable.label',
              defaultMessage: 'No items to display',
            }}
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
          type="submit"
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
};
