import React from 'react';

import PropTypes from 'prop-types';
import { useSelector } from 'react-redux';
import { getLotNumbersByProductId } from 'selectors';

import SelectField from 'components/form-elements/v2/SelectField';

const LotSelectorField = ({ productId, ...props }) => {
  const { lotNumbers } = useSelector((state) => ({
    lotNumbers: getLotNumbersByProductId(state, productId),
  }));

  return (
    <SelectField
      options={lotNumbers.map((item) => ({
        id: item.lotNumber,
        name: item.lotNumber,
        label: item.lotNumber,
        value: item.lotNumber,
      }))}
      creatable
      {...props}
    />
  );
};

LotSelectorField.propTypes = {
  productId: PropTypes.string.isRequired,
};

export default LotSelectorField;
