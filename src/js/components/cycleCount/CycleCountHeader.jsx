import React from 'react';
import HeaderWrapper from "wrappers/HeaderWrapper";
import ListTitle from "components/listPagesUtils/ListTitle";

const CycleCountHeader = () => {
  return (
    <HeaderWrapper>
      <ListTitle label={{
        id: 'react.cycleCount.headerTitle.label',
        defaultMessage: 'Cycle Count',
      }}
      />
    </HeaderWrapper>
  );
};

export default CycleCountHeader;
