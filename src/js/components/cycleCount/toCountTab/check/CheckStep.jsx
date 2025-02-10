import React from 'react';

import CheckStepHeader from 'components/cycleCount/toCountTab/check/CheckStepHeader';
import CheckStepTable from 'components/cycleCount/toCountTab/check/CheckStepTable';
import useCheckStep from 'hooks/cycleCount/useCheckStep';
import PageWrapper from 'wrappers/PageWrapper';

const CheckStep = () => {
  const {
    tableData,
    columns,
    back,
    save,
    getCountedDate,
  } = useCheckStep();

  return (
    <PageWrapper>
      <CheckStepHeader back={back} save={save} />
      {tableData.map(({ cycleCountItems, id }) => (
        <CheckStepTable
          key={id}
          id={id}
          product={cycleCountItems[0]?.product}
          tableData={cycleCountItems}
          columns={columns}
          dateCounted={getCountedDate(id)}
        />
      ))}
    </PageWrapper>
  );
};

export default CheckStep;
