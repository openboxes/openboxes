import React, { useEffect, useState } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import _ from 'lodash';
import { RiDeleteBinLine } from 'react-icons/ri';
import { useSelector } from 'react-redux';
import { Tooltip } from 'react-tippy';

import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import DateField from 'components/form-elements/v2/DateField';
import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import cycleCountColumn from 'consts/cycleCountColumn';
import useTranslate from 'hooks/useTranslate';
import { fetchBins } from 'utils/option-utils';

// Managing state for single table, mainly table configuration (from count step)
const useCountStepTable = ({
  cycleCountId,
  removeRow,
  validationErrors,
  tableData,
  isEditable,
}) => {
  const columnHelper = createColumnHelper();
  // State for saving data for binLocation dropdown
  const [binLocations, setBinLocations] = useState([]);

  const translate = useTranslate();

  const { recipients, currentLocation } = useSelector((state) => ({
    recipients: state.users.data,
    currentLocation: state.session.currentLocation,
  }));

  useEffect(() => {
    (async () => {
      const fetchedBins = await fetchBins(currentLocation?.id);
      setBinLocations(fetchedBins);
    })();
  }, [currentLocation?.id]);

  // Get appropriate input component based on table column
  const getFieldComponent = (fieldName) => {
    if (fieldName === cycleCountColumn.EXPIRATION_DATE) {
      return DateField;
    }

    if (fieldName === cycleCountColumn.BIN_LOCATION) {
      return SelectField;
    }

    return TextInput;
  };

  // Get text input type: quantityCounted expects a number,
  // the rest of the inputs should be text
  const getFieldType = (fieldName) => {
    if (fieldName === cycleCountColumn.QUANTITY_COUNTED) {
      return 'number';
    }

    return 'text';
  };

  // Get field props, for the binLocation dropdown we have to pass options
  const getFieldProps = (fieldName) => {
    if (fieldName === cycleCountColumn.BIN_LOCATION) {
      return {
        labelKey: 'name',
        options: binLocations.map((binLocation) => ({
          id: binLocation.id,
          name: binLocation.name,
        })),
      };
    }

    return {};
  };

  const defaultColumn = {
    cell: ({
      getValue, row: { original, index }, column: { id }, table,
    }) => {
      const isFieldEditable = !original.id.includes('newRow') && id !== cycleCountColumn.QUANTITY_COUNTED;
      // We shouldn't allow users edit fetched data (only quantity counted is editable)
      if (isFieldEditable || !isEditable) {
        return (
          <TableCell className="static-cell-count-step">
            {getValue()}
          </TableCell>
        );
      }
      // Keep and update the state of the cell during rerenders
      const columnPath = id.replaceAll('_', '.');
      const initialValue = _.get(tableData, `[${index}].${columnPath}`);
      const errorMessage = validationErrors?.[cycleCountId]?.errors?.[index]?.[columnPath]?._errors;

      const [value, setValue] = useState(initialValue);
      const [error, setError] = useState(errorMessage);
      // If the value at the end of entering data is the same as it was initially,
      // we don't want to trigger rerender
      const isEdited = initialValue !== value;
      // When the input is blurred, we'll call the table meta's updateData function
      const onBlur = () => {
        if (isEdited) {
          table.options.meta?.updateData(cycleCountId, original.id, id, value);
          setError(null);
        }
      };

      // on change function expects e.target.value for text fields,
      // in other cases it expects just the value
      const onChange = (e) => {
        setValue(e?.target?.value ?? e);
      };

      // Table consists of text fields, one numerical field for quantity counted,
      // select field for bin locations and one date picker for the expiration date.
      const type = getFieldType(id);
      const Component = getFieldComponent(id);
      const fieldProps = getFieldProps(id);

      return (
        <TableCell className="rt-td rt-td-count-step pb-0">
          <Component
            type={type}
            value={value}
            onChange={onChange}
            onBlur={onBlur}
            className="w-75 m-1"
            errorMessage={error}
            {...fieldProps}
          />
        </TableCell>
      );
    },
  };

  const columns = [
    columnHelper.accessor(
      (row) => (row?.binLocation?.label ? row?.binLocation : row.binLocation?.name), {
        id: cycleCountColumn.BIN_LOCATION,
        header: () => (
          <TableHeaderCell>
            {translate('react.cycleCount.table.binLocation.label', 'Bin Location')}
          </TableHeaderCell>
        ),
      },
    ),
    columnHelper.accessor(cycleCountColumn.LOT_NUMBER, {
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.lotNumber.label', 'Serial / Lot Number')}
        </TableHeaderCell>
      ),
    }),
    columnHelper.accessor(cycleCountColumn.EXPIRATION_DATE, {
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.expirationDate.label', 'Expiration Date')}
        </TableHeaderCell>
      ),
      meta: {
        getCellContext: () => ({
          className: 'split-table-right',
        }),
      },
    }),
    columnHelper.accessor(cycleCountColumn.QUANTITY_COUNTED, {
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.quantityCounted.label', 'Quantity Counted')}
        </TableHeaderCell>
      ),
    }),
    columnHelper.accessor(cycleCountColumn.COMMENT, {
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.comment.label', 'Comment')}
        </TableHeaderCell>
      ),
    }),
    columnHelper.accessor(null, {
      id: cycleCountColumn.ACTIONS,
      header: () => <TableHeaderCell className="count-step-actions" />,
      cell: ({ row: { original } }) => (
        <TableCell className="rt-td d-flex justify-content-center count-step-actions">
          <Tooltip
            arrow="true"
            delay="150"
            duration="250"
            hideDelay="50"
            className="text-overflow-ellipsis"
            html={(
              <span className="p-2">
                {translate('react.default.button.delete.label', 'Delete')}
              </span>
            )}
            disabled={original.id}
          >
            {original.id.includes('newRow') && (
            <RiDeleteBinLine
              onClick={() => removeRow(cycleCountId, original.id)}
              size={22}
            />
            )}
          </Tooltip>
        </TableCell>
      ),
      meta: {
        getCellContext: () => ({
          className: 'count-step-actions',
        }),
      },
    }),
  ];

  return {
    columns,
    defaultColumn,
    recipients,
  };
};

export default useCountStepTable;
