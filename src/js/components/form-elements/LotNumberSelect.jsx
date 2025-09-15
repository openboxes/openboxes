import React from 'react';

import PropTypes from 'prop-types';
import { useSelector } from 'react-redux';
import { getLotNumbersByProductId } from 'selectors';

import SelectField from 'components/form-elements/v2/SelectField';

const LotNumberSelect = ({ productId, ...props }) => {
  const { lotNumbers } = useSelector((state) => ({
    lotNumbers: getLotNumbersByProductId(state, productId),
  }));

  return (
    <SelectField
      options={lotNumbers.map((item) => ({
        label: item.lotNumber,
        value: item.lotNumber,
      }))}
      creatable
      {...props}
    />
  );
};

LotNumberSelect.propTypes = {
  productId: PropTypes.string.isRequired,
};

export default LotNumberSelect;
