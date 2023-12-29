import React from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';

import Translate from 'utils/Translate';

const getLabel = (productSupplierPreferences) => {
  if (!productSupplierPreferences.length) {
    return {
      id: 'react.productSupplier.preferenceType.none.label',
      defaultMessage: 'None',
    };
  }
  if (productSupplierPreferences.length > 1) {
    return {
      id: 'react.productSupplier.preferenceType.multiple.label',
      defaultMessage: 'Multiple',
    };
  }
  return productSupplierPreferences[0].preferenceType?.id;
};

const PreferenceTypeColumn = ({ productSupplierPreferences }) => {
  const label = getLabel(productSupplierPreferences);

  return (
    <span>
      {_.isObject(label)
        ? <Translate id={label.id} defaultMessage={label.defaultMessage} />
        : label}
    </span>
  );
};

export default PreferenceTypeColumn;

PreferenceTypeColumn.propTypes = {
  productSupplierPreferences: PropTypes.arrayOf(PropTypes.shape({
    destinationParty: PropTypes.shape({
      id: PropTypes.string.isRequired,
      name: PropTypes.string,
      description: PropTypes.string,
      code: PropTypes.string,
      dateCreated: PropTypes.string,
      lastUpdated: PropTypes.string,
      partyType: PropTypes.shape({
        id: PropTypes.string.isRequired,
        name: PropTypes.string,
        code: PropTypes.string,
        partyTypeCode: PropTypes.string,
      }),
      roles: PropTypes.arrayOf(PropTypes.shape({
        id: PropTypes.string,
        roleType: PropTypes.string,
        startDate: PropTypes.string,
        endDate: PropTypes.string,
      })),
      sequences: PropTypes.arrayOf(PropTypes.shape({})),
    }),
    preferenceType: PropTypes.shape({
      id: PropTypes.string.isRequired,
      dateCreated: PropTypes.string,
      lastUpdated: PropTypes.string,
      name: PropTypes.string,
    }),
  })).isRequired,
};
