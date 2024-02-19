import React from 'react';

import Subsection from 'components/Layout/v2/Subsection';

const PackageSpecification = () => {
  return (
    <Subsection
      title={{
        label: 'react.productSupplier.form.subsection.packageSpecification',
        defaultMessage: 'Package Specification',
      }}
      collapsable={false}
    >
      <div>FixedPrice</div>
    </Subsection>
  );
};

export default PackageSpecification;
