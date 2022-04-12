import React from 'react';

import PropTypes from 'prop-types';
import { withRouter } from 'react-router-dom';

import Translate from 'utils/Translate';

import 'components/locations-configuration/SuccessMessage.scss';

const LoadDemoDataErrorMessage = ({ history, supportLinks }) => (
  <div className="success-wrapper">
    <div className="success-content">
      <i className="fa fa-exclamation-circle error-icon" aria-hidden="true" />
      <h2 className="font-weight-bold">
        <Translate id="react.loadData.error.title.label" defaultMessage="Error occurred while loading demo data" />
      </h2>
      <p className="w-75 text-break">
        <Translate
          id="react.loadData.error.content.label"
          defaultMessage="There was an issue loading the demo data.
        Sometimes this can be resolved by simply re-trying the load process.
        You can find a link to restart the load process in the configuration menu.
        You can also ask your administrator to reset your instance.
        If you aren't sure how to proceed, you can request support on the OpenBoxes"
        />
        {' '}
        <a href={supportLinks.discussionForum} className="knowledge-base-link">
          <Translate id="react.loadData.error.discussionForum.link.label" defaultMessage="discussion forum" />
        </a>
      </p>
      <div className="success-buttons-section d-flex flex-column w-50">
        <button type="button" className="btn btn-outline-primary" onClick={() => history.push('/openboxes')}>Back to dashboard</button>
      </div>
    </div>
  </div>
);

export default withRouter(LoadDemoDataErrorMessage);

LoadDemoDataErrorMessage.propTypes = {
  history: PropTypes.shape({ push: PropTypes.func }).isRequired,
  supportLinks: PropTypes.shape({
    discussionForum: PropTypes.string.isRequired,
  }).isRequired,
};
