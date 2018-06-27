import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { BrowserRouter, Switch } from 'react-router-dom';
import { ClimbingBoxLoader } from 'react-spinners';

import StockMovement from './stock-movement-wizard/StockMovement';
import ReceivingPage from './receiving/ReceivingPage';
import MainLayoutRoute from './Layout/MainLayoutRoute';


const Router = props => (
  <div>
    <BrowserRouter>
      <Switch>
        <MainLayoutRoute path="/**/stockMovement" component={StockMovement} />
        <MainLayoutRoute path="/**/partialReceiving" component={ReceivingPage} />
      </Switch>
    </BrowserRouter>
    <div className="spinner-container">
      <ClimbingBoxLoader
        color="#0c769e"
        loading={props.spinner}
        style={{ top: '40%', lefft: '50%' }}
      />
    </div>
  </div>
);

const mapStateToProps = state => ({
  spinner: state.spinner.show,
});

export default connect(mapStateToProps, {})(Router);

Router.propTypes = {
  spinner: PropTypes.bool.isRequired,
};
