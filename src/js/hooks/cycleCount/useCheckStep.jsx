import React, { useEffect, useRef, useState } from 'react';

import { createColumnHelper } from '@tanstack/react-table';
import { useSelector } from 'react-redux';
import { useHistory } from 'react-router-dom';

import cycleCountApi from 'api/services/CycleCountApi';
import { TableCell } from 'components/DataTable';
import TableHeaderCell from 'components/DataTable/TableHeaderCell';
import { CYCLE_COUNT } from 'consts/applicationUrls';
import useTranslate from 'hooks/useTranslate';

const useCheckStep = () => {
  const tableData = useRef([]);
  const [dateCounted, setDateCounted] = useState({});
  const history = useHistory();
  const columnHelper = createColumnHelper();
  const translate = useTranslate();

  const {
    cycleCountIds,
    currentLocation,
  } = useSelector((state) => ({
    cycleCountIds: state.cycleCount.toCount,
    currentLocation: state.session.currentLocation,
  }));

  useEffect(() => {
    (async () => {
      const { data } = await cycleCountApi.getCycleCounts(
        currentLocation?.id,
        cycleCountIds,
      );
      tableData.current = data?.data;
      const countedDates = data?.data?.reduce((acc, cycleCount) => ({
        ...acc,
        [cycleCount?.id]: cycleCount?.cycleCountItems[0].dateCounted,
      }), {});
      setDateCounted(countedDates);
    })();
  }, [cycleCountIds]);

  const back = () => {
    history.push(CYCLE_COUNT.countStep());
  };

  const save = () => {
    console.log('save');
  };

  const getCountedDate = (cycleCountId) => dateCounted[cycleCountId];

  const columns = [
    columnHelper.accessor(
      (row) => (row?.binLocation?.label ? row?.binLocation : row.binLocation?.name), {
        id: 'binLocation',
        header: () => (
          <TableHeaderCell>
            {translate('react.cycleCount.table.binLocation.label', 'Bin Location')}
          </TableHeaderCell>
        ),
        cell: ({ getValue }) => (
          <TableCell className="rt-td-check-step">
            {getValue()}
          </TableCell>
        ),
      },
    ),
    columnHelper.accessor('inventoryItem.lotNumber', {
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.lotNumber.label', 'Serial / Lot Number')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td-check-step">
          {getValue()}
        </TableCell>
      ),
    }),
    columnHelper.accessor('inventoryItem.expirationDate', {
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.expirationDate.label', 'Expiration Date')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td-check-step">
          {getValue()}
        </TableCell>
      ),
      meta: {
        getCellContext: () => ({
          className: 'split-table-right',
        }),
      },
    }),
    columnHelper.accessor('quantityCounted', {
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.quantityCounted.label', 'Quantity Counted')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td-check-step">
          {getValue()}
        </TableCell>
      ),
    }),
    columnHelper.accessor('comment', {
      header: () => (
        <TableHeaderCell>
          {translate('react.cycleCount.table.comment.label', 'Comment')}
        </TableHeaderCell>
      ),
      cell: ({ getValue }) => (
        <TableCell className="rt-td-check-step">
          {getValue()}
        </TableCell>
      ),
    }),
  ];

  return {
    tableData: tableData.current,
    back,
    save,
    getCountedDate,
    columns,
  };
};

export default useCheckStep;
