import React, { useRef } from 'react';

import { useWindowVirtualizer } from '@tanstack/react-virtual';
import _ from 'lodash';
import { useSelector } from 'react-redux';
import { getCurrentLocation } from 'selectors';

import ConfirmStepHeader from 'components/cycleCount/ConfirmStepHeader';
import ResolveStepHeader from 'components/cycleCount/toResolveTab/ResolveStepHeader';
import ResolveStepTable from 'components/cycleCount/toResolveTab/ResolveStepTable';
import { TO_RESOLVE_TAB } from 'consts/cycleCount';
import useResolveStep from 'hooks/cycleCount/useResolveStep';
import useTranslation from 'hooks/useTranslation';
import PageWrapper from 'wrappers/PageWrapper';

import 'components/cycleCount/cycleCount.scss';

const ResolveStep = () => {
  const {
    currentLocation,
  } = useSelector((state) => ({
    currentLocation: getCurrentLocation(state),
  }));
  const initialCurrentLocation = useRef(currentLocation?.id);
  const isFormDisabled = !_.isEqual(currentLocation?.id, initialCurrentLocation.current);
  const {
    tableData,
    validationErrors,
    printRecountForm,
    refreshCountItems,
    next,
    tableMeta,
    addEmptyRow,
    removeRow,
    assignRecountedBy,
    getRecountedDate,
    setRecountedDate,
    shouldHaveRootCause,
    back,
    save,
    isStepEditable,
    getRecountedBy,
    getCountedBy,
    submitRecount,
    getProduct,
    getDateCounted,
    refreshFocusCounter,
    triggerValidation,
    isSaveDisabled,
    setIsSaveDisabled,
    cycleCountsWithItemsWithoutRecount,
    sortByProductName,
    setSortByProductName,
  } = useResolveStep();
  useTranslation('cycleCount');

  const tableVirtualizer = useWindowVirtualizer({
    count: tableData.length,
    // table with ~ 5 rows, average size of the recount table
    estimateSize: () => 518,
    overscan: 5,
  });

  return (
    <PageWrapper>
      {isStepEditable ? (
        <ResolveStepHeader
          printRecountForm={printRecountForm}
          refreshCountItems={refreshCountItems}
          next={next}
          save={save}
          isFormDisabled={isFormDisabled}
          sortByProductName={sortByProductName}
          setSortByProductName={setSortByProductName}
        />
      ) : (
        <ConfirmStepHeader
          back={back}
          save={submitRecount}
          isSaveDisabled={isSaveDisabled}
          setIsSaveDisabled={setIsSaveDisabled}
          isFormDisabled={isFormDisabled}
          redirectTab={TO_RESOLVE_TAB}
          redirectLabel="react.cycleCount.redirectToResolveTab.label"
          redirectDefaultMessage="Back to Resolve tab"
        />
      )}
      <div
        style={{
          height: `${tableVirtualizer.getTotalSize()}px`,
          position: 'relative',
        }}
      >
        {tableVirtualizer.getVirtualItems()
          .map((virtualRow) => {
            const {
              cycleCountItems,
              id,
            } = tableData[virtualRow.index];

            return (
              <div
                key={id}
                data-index={virtualRow.index}
                ref={tableVirtualizer.measureElement}
                style={{
                  position: 'absolute',
                  top: 0,
                  transform: `translateY(${virtualRow.start}px)`,
                  width: '100%',
                }}
              >
                <ResolveStepTable
                  key={id}
                  id={id}
                  product={getProduct(id)}
                  dateCounted={getDateCounted(id)}
                  dateRecounted={getRecountedDate(id)}
                  tableData={cycleCountItems}
                  tableMeta={tableMeta}
                  addEmptyRow={addEmptyRow}
                  removeRow={removeRow}
                  setRecountedDate={setRecountedDate(id)}
                  assignRecountedBy={assignRecountedBy}
                  validationErrors={validationErrors}
                  shouldHaveRootCause={shouldHaveRootCause}
                  isStepEditable={isStepEditable}
                  recountedBy={getRecountedBy(id)}
                  countedBy={getCountedBy(id)}
                  triggerValidation={triggerValidation}
                  refreshFocusCounter={refreshFocusCounter}
                  cycleCountWithItemsWithoutRecount={
                    cycleCountsWithItemsWithoutRecount.find((cycleCount) => cycleCount.id === id)
                  }
                  isFormDisabled={isFormDisabled}
                />
              </div>
            );
          })}
      </div>
    </PageWrapper>
  );
};

export default ResolveStep;
