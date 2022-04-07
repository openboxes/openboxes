import React from 'react';

import PropTypes from 'prop-types';
import ReactHtmlParser from 'react-html-parser';

import Translate from 'utils/Translate';

import { LOAD_DATA_STEPS } from './LoadDemoDataPage';

const LoadDemoDataInfo = ({ summaryItemsTitle, summaryItemsList, goToStep }) => (
  <React.Fragment>
    <h3 className="font-weight-bold my-3">
      <Translate id="react.loadData.loadDataHeader.label" defaultMessage="Load Demo Data" />
    </h3>
    <p className="my-3">
      <Translate id="react.loadData.loadDataDescription.label" />
    </p>
    <div>
      <h5 className="font-weight-bold my-3">{summaryItemsTitle}:</h5>
      {ReactHtmlParser(summaryItemsList)}
    </div>
    <div className="d-flex justify-content-between m-3">
      <button type="button" onClick={() => goToStep(null)} className="btn btn-outline-primary">
        <Translate id="default.button.back.label" defaultMessage="Back" />
      </button>
      <button type="button" onClick={() => goToStep(LOAD_DATA_STEPS.proceedLoadingDemoData)} className="btn btn-outline-primary">
        <Translate id="default.button.proceed.label" defaultMessage="Proceed" />
      </button>
    </div>
  </React.Fragment>
);

LoadDemoDataInfo.propTypes = {
  goToStep: PropTypes.func.isRequired,
  summaryItemsTitle: PropTypes.string.isRequired,
  summaryItemsList: PropTypes.string.isRequired,
};

export default LoadDemoDataInfo;
