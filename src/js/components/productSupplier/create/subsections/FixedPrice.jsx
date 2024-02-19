import React from 'react';

import Subsection from 'components/Layout/v2/Subsection';

const FixedPrice = () => {
  return (
    <Subsection
      title={{
        label: 'react.productSupplier.form.subsection.fixedPrice',
        defaultMessage: 'Fixed Price',
      }}
      collapsable={false}
    >
      <div>FixedPrice</div>
    </Subsection>
  );
};

export default FixedPrice;
