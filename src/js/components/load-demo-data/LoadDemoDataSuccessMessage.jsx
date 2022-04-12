import React from 'react';

import PropTypes from 'prop-types';
import { withRouter } from 'react-router-dom';

import Translate from 'utils/Translate';

import 'components/locations-configuration/SuccessMessage.scss';

const LoadDemoDataSuccessMessage = ({ history, supportLinks }) => (
  <div className="success-wrapper">
    <div className="success-content">
      <i className="fa fa-check-circle-o success-icon" aria-hidden="true" />
      <h2 className="font-weight-bold">
        <Translate id="react.loadData.success.title.label" defaultMessage="Data Successfully Loaded" />
      </h2>
      <p className="w-75">
        <Translate
          id="react.loadData.success.content.label"
          defaultMessage="You are not ready to start your exploration of OpenBoxes!
                                    Go to your dashboard to get started.
                                    For guidance on how to use OpenBoxes,be sure to refer to our"
        />
        {' '}
        <a href={supportLinks.knowledgeBase} className="knowledge-base-link">
          <Translate id="react.loadData.success.helpscout.button.label" defaultMessage="helpscout knowledge base." />
        </a>
      </p>
      <div className="success-buttons-section d-flex flex-column w-50">
        <button type="button" className="btn btn-outline-primary" onClick={() => history.push('/openboxes')}>
          <Translate id="react.loadData.success.exploreDashboard.button.label" defaultMessage="Explore dashboard" />
        </button>
      </div>
    </div>
  </div>
);

export default withRouter(LoadDemoDataSuccessMessage);

LoadDemoDataSuccessMessage.propTypes = {
  history: PropTypes.shape({ push: PropTypes.func }).isRequired,
  supportLinks: PropTypes.shape({
    knowledgeBase: PropTypes.string.isRequired,
  }).isRequired,
};
