import React, { useMemo, useState } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import { useDispatch, useSelector } from 'react-redux';
import {
  getCurrentLocationId,
  getCurrentLocationSupportedActivities,
  makeGetCycleCountItems,
} from 'selectors';

import { removeRow } from 'actions';
import ActionsCell from 'components/cycleCount/tableCell/ActionsCell';
import BinLocationCell from 'components/cycleCount/tableCell/BinLocationCell';
import CommentCell from 'components/cycleCount/tableCell/CommentCell';
import ExpirationDateCell from 'components/cycleCount/tableCell/ExpirationDateCell';
import LotNumberCell from 'components/cycleCount/tableCell/LotNumberCell';
import QuantityCell from 'components/cycleCount/tableCell/QuantityCell';
import HeaderCell from 'components/cycleCount/tableHeader/HeaderCell';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import cycleCountColumn from 'consts/cycleCountColumn';
import { getBinLocationToDisplay } from 'utils/groupBinLocationsByZone';
import { checkBinLocationSupport } from 'utils/supportedActivitiesUtils';

// Managing state for single table, mainly table configuration (from count step)
const useCountStepTable = ({
  cycleCountId,
  isStepEditable,
  isFormDisabled,
}) => {
  const columnHelper = createColumnHelper();
  const [disabledExpirationDateFields, setDisabledExpirationDateFields] = useState({});

  const currentLocationId = useSelector(getCurrentLocationId);
  const currentLocationSupportedActivities = useSelector(getCurrentLocationSupportedActivities);

  const getCycleCountItems = useMemo(makeGetCycleCountItems, []);

  const cycleCountItems = useSelector(
    (state) => getCycleCountItems(state, cycleCountId),
  );

  const dispatch = useDispatch();

  const showBinLocation = useMemo(() =>
    checkBinLocationSupport(currentLocationSupportedActivities), [currentLocationId]);

  const handleRemoveRow = (rowId) => {
    dispatch(removeRow(cycleCountId, rowId));
  };

  const columns = [
    columnHelper.accessor(
      (row) => getBinLocationToDisplay(row?.binLocation), {
        id: cycleCountColumn.BIN_LOCATION,
        header: () => <HeaderCell id="react.cycleCount.table.binLocation.label" defaultMessage="Bin Location" />,
        cell: ({ getValue, row: { original: { id } } }) => (
          <BinLocationCell
            initialValue={getValue()}
            id={id}
            cycleCountId={cycleCountId}
            showBinLocation={showBinLocation}
            isStepEditable={isStepEditable}
          />
        ),
        meta: {
          flexWidth: 100,
          hide: !showBinLocation,
        },
      },
    ),
    columnHelper.accessor(cycleCountColumn.LOT_NUMBER, {
      header: () => <HeaderCell id="react.cycleCount.table.lotNumber.label" defaultMessage="Serial / Lot Number" />,
      cell: ({ getValue, row: { original: { id } } }) => (
        <LotNumberCell
          initialValue={getValue()}
          id={id}
          cycleCountId={cycleCountId}
          setDisabledExpirationDateFields={setDisabledExpirationDateFields}
          isStepEditable={isStepEditable}
        />
      ),
      meta: {
        flexWidth: 100,
      },
    }),
    columnHelper.accessor(cycleCountColumn.EXPIRATION_DATE, {
      header: () => <HeaderCell id="react.cycleCount.table.expirationDate.label" defaultMessage="Expiration Date" />,
      cell: ({ getValue, row: { original: { id } } }) => (
        <ExpirationDateCell
          initialValue={getValue()}
          disabledExpirationDateFields={disabledExpirationDateFields}
          cycleCountId={cycleCountId}
          isStepEditable={isStepEditable}
          id={id}
        />
      ),
      meta: {
        flexWidth: 100,
        getCellContext: () => ({
          className: 'split-table-right',
        }),
      },
    }),
    columnHelper.accessor(cycleCountColumn.QUANTITY_COUNTED, {
      header: () => <HeaderCell id="react.cycleCount.table.quantityCounted.label" defaultMessage="Quantity Counted" />,
      cell: ({ getValue, row: { original: { id } } }) => (
        <QuantityCell
          initialValue={getValue()}
          id={id}
          cycleCountId={cycleCountId}
          isStepEditable={isStepEditable}
        />
      ),
      meta: {
        flexWidth: 50,
      },
    }),
    columnHelper.accessor(cycleCountColumn.COMMENT, {
      header: () => <HeaderCell id="react.cycleCount.table.comment.label" defaultMessage="Comment" />,
      cell: ({ getValue, row: { original: { id } } }) => (
        <CommentCell
          initialValue={getValue()}
          id={id}
          cycleCountId={cycleCountId}
          isStepEditable={isStepEditable}
        />
      ),
      meta: {
        flexWidth: 100,
      },
    }),
    columnHelper.accessor(null, {
      id: cycleCountColumn.ACTIONS,
      header: () => <TableHeaderCell />,
      cell: ({ row: { original: { id }, custom } }) => (
        <ActionsCell
          custom={custom}
          id={id}
          isStepEditable={isStepEditable}
          isFormDisabled={isFormDisabled}
          removeRow={() => handleRemoveRow(id)}
        />
      ),
      meta: {
        getCellContext: () => ({
          className: 'count-step-actions',
        }),
        flexWidth: 25,
      },
    }),
  ];

  return {
    columns,
    cycleCountItems,
  };
};

export default useCountStepTable;
