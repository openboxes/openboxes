import React, { useEffect } from 'react';

import _ from 'lodash';
import PropTypes from 'prop-types';
import { RiCloseFill } from 'react-icons/all';
import Modal from 'react-modal';

import Button from 'components/form-elements/Button';
import { PRODUCT_SUPPLIER_URL } from 'consts/applicationUrls';
import RoleType from 'consts/roleType';
import useUserHasPermissions from 'hooks/useUserHasPermissions';
import Translate from 'utils/Translate';

const PreferenceTypeModal = ({
  isOpen,
  closeModal,
  modalData,
  productSupplierId,
}) => {
  const canManageProducts = useUserHasPermissions({
    minRequiredRole: RoleType.ROLE_ADMIN,
    supplementalRoles: [RoleType.ROLE_PRODUCT_MANAGER],
  });

  useEffect(() => {
    if (isOpen) {
      document.body.style.overflowY = 'hidden';
    }

    return () => {
      document.body.style.overflowY = 'auto';
    };
  }, [isOpen]);

  const mappedPreferenceTypes = modalData?.reduce((acc, preferenceType) => {
    const {
      preferenceType: { name },
      destinationParty,
    } = preferenceType;

    if (!destinationParty) {
      return {
        ...acc,
        default: {
          name,
        },
      };
    }

    return {
      ...acc,
      preferenceTypes: [
        ...acc.preferenceTypes,
        {
          destination: destinationParty?.name,
          name,
        },
      ],
    };
  }, { preferenceTypes: [], default: null });

  const redirectToEditProductSource = (id) => {
    window.location = PRODUCT_SUPPLIER_URL.edit(id);
  };

  return (
    <Modal isOpen={isOpen} className="modal-content">
      <div>
        <div className="d-flex justify-content-between">
          <p className="preference-type-modal-header">
            <Translate
              id="react.productSupplier.column.preferenceType.label"
              defaultMessage="Preference Type"
            />
          </p>
          <RiCloseFill
            size="32px"
            className="cursor-pointer"
            role="button"
            onClick={closeModal}
          />
        </div>
        <div className="preference-type-modal-list-container">
          <div>
            <p className="default-preference-type">
              <span className="preference-type-location">Default: </span>
              <span>{mappedPreferenceTypes?.default?.name}</span>
            </p>
          </div>
          <div className="preference-type-modal-list">
            {_.sortBy(mappedPreferenceTypes?.preferenceTypes,
              (preferenceType) => preferenceType.destination)?.map((preferenceType) => (
                <p className="preference-type-modal-list-element" key={preferenceType?.destination}>
                  <span className="preference-type-location">
                    {preferenceType?.destination}
                    :
                    {' '}
                  </span>
                  <span>{preferenceType?.name}</span>
                </p>
            ))}
          </div>
        </div>
        <div className="d-flex justify-content-end mt-3">
          {canManageProducts && (
            <Button
              defaultLabel="Edit"
              label="react.productSupplier.edit.label"
              onClick={() => redirectToEditProductSource(productSupplierId)}
            />
          )}
        </div>
      </div>
    </Modal>
  );
};

export default PreferenceTypeModal;

PreferenceTypeModal.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  closeModal: PropTypes.func.isRequired,
  modalData: PropTypes.arrayOf(PropTypes.shape({
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
