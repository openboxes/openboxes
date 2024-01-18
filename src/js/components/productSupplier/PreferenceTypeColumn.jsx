import React, { useState } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { RiInformationLine } from 'react-icons/ri';

import PreferenceTypeModal from 'components/productSupplier/modals/PreferenceTypeModal';
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
      icon: <RiInformationLine />,
      className: 'cell-content',
    };
  }
  return productSupplierPreferences[0].preferenceType?.name;
};

const PreferenceTypeColumn = ({ productSupplierPreferences, productSupplierId }) => {
  const [preferenceTypeModalData, setPreferenceTypeModalData] = useState([]);
  const label = getLabel(productSupplierPreferences);

  const onCellClick = () => {
    if (productSupplierPreferences.length > 1) {
      setPreferenceTypeModalData(productSupplierPreferences);
    }
  };

  const closeModal = () => setPreferenceTypeModalData([]);

  return (
    <>
      <span
        className={label?.className}
        onClick={onCellClick}
        role="presentation"
      >
        {_.isObject(label)
          ? (
            <>
              <Translate id={label.id} defaultMessage={label.defaultMessage} />
              {' '}
              {label?.icon}
            </>
          )
          : label}
      </span>
      <PreferenceTypeModal
        productSupplierId={productSupplierId}
        isOpen={Boolean(preferenceTypeModalData.length)}
        modalData={preferenceTypeModalData}
        closeModal={closeModal}
      />
    </>
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
  productSupplierId: PropTypes.string.isRequired,
};
