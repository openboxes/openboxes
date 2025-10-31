import React from 'react';

import PropTypes from 'prop-types';
import ReactHtmlParser from 'react-html-parser';

import LOAD_DATA_STEPS from 'consts/loadDataStep';
import Translate from 'utils/Translate';

const LoadDemoDataInfo = ({ summaryItemsTitle, summaryItemsList, goToStep }) => (
  <>
    <h3 className="font-weight-bold my-3">
      <Translate id="react.loadData.loadDataHeader.label" defaultMessage="Load Demo Data" />
    </h3>
    <p className="my-3">
      <Translate id="react.loadData.loadDataDescription.label" />
    </p>
    <div>
      <h5 className="font-weight-bold my-3">
        {summaryItemsTitle}
        :
      </h5>
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
  </>
);

LoadDemoDataInfo.propTypes = {
  goToStep: PropTypes.func.isRequired,
  summaryItemsTitle: PropTypes.string.isRequired,
  summaryItemsList: PropTypes.string.isRequired,
};

export default LoadDemoDataInfo;
