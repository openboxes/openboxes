import React from 'react';

import PropTypes from 'prop-types';
import Modal from 'react-modal';
import { withRouter } from 'react-router-dom';

import Translate from 'utils/Translate';

import 'components/locations-configuration/SuccessMessage.scss';


const SuccessMessage = ({ setShowSuccessMessage, history, successMessageOpen }) => (
  <Modal
    isOpen={successMessageOpen}
    className="location-modal"
    onRequestClose={setShowSuccessMessage}
  >
    <div className="success-main">
      <div className="success-wrapper">
        <div className="cancel-icon">
          <i className="fa fa-times" aria-hidden="true" onClick={setShowSuccessMessage} />
        </div>
        <div className="success-content">
          <i className="fa fa-check-circle-o success-icon" aria-hidden="true" />
          <h2>
            <Translate
              id="react.locationsConfiguration.success.congratulations"
              defaultMessage="Congratulations"
            />
          </h2>
          <p>
            <Translate
              id="react.locationsConfiguration.success.subtitle"
              defaultMessage="You have created your first location!"
            />
          </p>
          <div className="success-buttons-section d-flex flex-column">
            <button type="button" className="btn btn-outline-primary" onClick={() => window.location.reload()}>
              <Translate
                id="react.locationsConfiguration.success.createAnother"
                defaultMessage="Create another location"
              />
            </button>
            <button type="button" className="btn btn-outline-primary" onClick={() => { window.location = '/openboxes/location/list'; }}>
              <Translate
                id="react.locationsConfiguration.success.viewLocationList"
                defaultMessage="View Location List"
              />
            </button>
            <button type="button" className="btn btn-outline-primary" onClick={() => history.push('/openboxes/productsConfiguration/index')}>
              <Translate
                id="react.locationsConfiguration.success.createCategoriesAndProducts="
                defaultMessage="Create Categories and Products"
              />
            </button>
          </div>
        </div>
      </div>
    </div>
  </Modal>
);

SuccessMessage.propTypes = {
  setShowSuccessMessage: PropTypes.func.isRequired,
  history: PropTypes.shape({
    push: PropTypes.func,
  }).isRequired,
  successMessageOpen: PropTypes.bool.isRequired,
};

export default withRouter(SuccessMessage);
