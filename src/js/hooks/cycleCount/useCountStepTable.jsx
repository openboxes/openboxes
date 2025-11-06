import React, { useMemo, useState } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import { useDispatch, useSelector } from 'react-redux';
import {
  getCurrentLocationId,
  getCurrentLocationSupportedActivities, makeGetCycleCountItem,
  makeGetCycleCountItemIds,
  makeGetCycleCountItemsTotalCount,
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

  const getTotalCount = useMemo(makeGetCycleCountItemsTotalCount, []);
  const cycleCountItemsTotalCount = useSelector((state) => getTotalCount(state, cycleCountId));

  // We are returning only IDs to render rows properly. The array doesn't contain full objects to
  // reduce re-renders of the whole table on every small cell update.
  const getCycleCountItemIds = useMemo(makeGetCycleCountItemIds, []);
  const cycleCountItemIds = useSelector((state) => getCycleCountItemIds(state, cycleCountId));

  const dispatch = useDispatch();

  const showBinLocation = useMemo(() =>
    checkBinLocationSupport(currentLocationSupportedActivities), [currentLocationId]);

  const handleRemoveRow = (rowId) => {
    dispatch(removeRow(cycleCountId, rowId));
  };

  const columns = useMemo(() => [
    columnHelper.accessor(
      (row) => getBinLocationToDisplay(row?.binLocation), {
        id: cycleCountColumn.BIN_LOCATION,
        header: () => <HeaderCell id="react.cycleCount.table.binLocation.label" defaultMessage="Bin Location" />,
        cell: ({ row: { original } }) => (
          <BinLocationCell
            id={original}
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
      cell: ({ row: { original } }) => (
        <LotNumberCell
          id={original}
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
      cell: ({ row: { original } }) => (
        <ExpirationDateCell
          disabledExpirationDateFields={disabledExpirationDateFields}
          cycleCountId={cycleCountId}
          isStepEditable={isStepEditable}
          id={original}
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
      cell: ({ row: { original } }) => (
        <QuantityCell
          id={original}
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
      cell: ({ row: { original } }) => (
        <CommentCell
          id={original}
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
  ], []);

  return {
    columns,
    cycleCountItemsTotalCount,
    cycleCountItemIds,
  };
};

export default useCountStepTable;
