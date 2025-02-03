import React, { useEffect, useState } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import { RiDeleteBinLine } from 'react-icons/ri';
import { useSelector } from 'react-redux';
import { Tooltip } from 'react-tippy';

import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import DateField from 'components/form-elements/v2/DateField';
import SelectField from 'components/form-elements/v2/SelectField';
import TextInput from 'components/form-elements/v2/TextInput';
import useTranslate from 'hooks/useTranslate';
import { fetchBins } from 'utils/option-utils';

// Managing state for single table, mainly table configuration (from count step)
const useCountStepTable = ({ removeRow }) => {
  const columnHelper = createColumnHelper();
  const [binLocations, setBinLocations] = useState([]);

  const translate = useTranslate();

  const { recipients, currentLocation } = useSelector((state) => ({
    recipients: state.users.data,
    currentLocation: state.session.currentLocation,
  }));

  useEffect(async () => {
    const fetchedBins = await fetchBins(currentLocation?.id);
    setBinLocations(fetchedBins);
  }, [currentLocation?.id]);

  const getFieldComponent = (fieldName) => {
    if (fieldName === 'expirationDate') {
      return DateField;
    }

    if (fieldName === 'internalLocation') {
      return SelectField;
    }

    return TextInput;
  };

  const getFieldType = (fieldName) => {
    if (fieldName === 'quantityCounted') {
      return 'number';
    }

    return 'text';
  };

  const getFieldProps = (fieldName) => {
    if (fieldName === 'internalLocation') {
      return {
        options: binLocations.map((binLocation) => ({
          id: binLocation.id,
          label: binLocation.name,
        })),
      };
    }

    return {};
  };

  const defaultColumn = {
    cell: ({
      getValue, row: { original }, column: { id }, table,
    }) => {
      const isFieldEditable = !original.id.includes('newRow') && id !== 'quantityCounted';
      // We shouldn't allow users edit fetched data (only quantity counted is editable)
      if (isFieldEditable) {
        return (
          <TableCell className="rt-td rt-td-count-step">
            {getValue()}
          </TableCell>
        );
      }
      // Keep and update the state of the cell
      const initialValue = getValue();
      const [value, setValue] = useState(initialValue);

      // When the input is blurred, we'll call our table meta's updateData function
      const onBlur = () => {
        if (initialValue !== value) {
          table.options.meta?.updateData(original.id, id, value);
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
            className="w-100"
            noWrapper
            {...fieldProps}
          />
        </TableCell>
      );
    },
  };

  const columns = [
    columnHelper.accessor('internalLocation', {
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.binLocation.label', 'Bin Location')}
        </TableHeaderCell>
      ),
    }),
    columnHelper.accessor('lotNumber', {
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.lotNumber.label', 'Serial / Lot Number')}
        </TableHeaderCell>
      ),
    }),
    columnHelper.accessor('expirationDate', {
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.expirationDate.label', 'Expiration Date')}
        </TableHeaderCell>
      ),
    }),
    columnHelper.accessor('quantityCounted', {
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.quantityCounted.label', 'Quantity Counted')}
        </TableHeaderCell>
      ),
    }),
    columnHelper.accessor('comment', {
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.comment.label', 'Comment')}
        </TableHeaderCell>
      ),
    }),
    columnHelper.accessor(null, {
      id: 'actions',
      header: () => <TableHeaderCell className="count-step-actions" />,
      cell: ({ row: { original, index } }) => (
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
            {!original.id && (
            <RiDeleteBinLine
              onClick={() => removeRow(index)}
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
