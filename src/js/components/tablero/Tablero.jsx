import _ from 'lodash';
import React, { Component } from 'react';
import { connect } from 'react-redux';
import { getTranslate } from 'react-localize-redux';
import { ChartComponent } from './BarChart'

import 'react-table/react-table.css';

import { hideSpinner, showSpinner, fetchTranslations } from '../../actions';

import  { translateWithDefaultMessage } from '../../utils/Translate';

class Tablero extends Component {
  constructor(props) {
    super(props);

    this.state = {
      data: [],
      selectedStocklist: null,
      availableStocklists: [],
      productInfo: null,
      users: [],
      isDataLoading: true,
      usersFetched: false,
      stocklistsFetched: false,

    };
  }

  componentDidMount() {

    if (this.props.stockListManagementTranslationsFetched) {
      this.dataFetched = true;

      this.fetchData();
    }
  }

  dataFetched = false;


  fetchData() {
    this.props.showSpinner();
  
    this.setState({isDataLoading: false})
  }


  render() {
return (
  <div class="cardComponent">
    <div class="Card"> 
      <span class="titleCard"> Bin Location Summary</span>
      <span class="resultCard"> 2,696</span>
      <span class="subtitleCard"> In stock </span>
    </div>
    <div class="Card"> 
      <span class="titleCard"> Bin Location Summary</span>
      <span class="resultCard"> 1,082</span>
      <span class="subtitleCard"> Out of Stock </span>
    </div>
    <div class="Card"> 
      <span class="titleCard"> S.M not received </span>
      <span class="resultCard"> 468</span>
      <span class="subtitleCard"> N° of S.M not shipped </span>
    </div>
    <div class="Card"> 
      <span class="titleCard"> User incomplete tasks </span>
      <span class="resultCard"> 188</span>
      <span class="subtitleCard"> N° of put aways not co... </span>
    </div>
    <div class="Card"> 
      <span class="titleCard"> Discrepancy </span>
      <span class="resultCard"> 290</span>
      <span class="subtitleCard"> N° of items receive </span>
    </div>
   
  </div>

  
);

  }
}

const mapStateToProps = state => ({
  translate: translateWithDefaultMessage(getTranslate(state.localize)),
  locale: state.session.activeLanguage,
  stockListManagementTranslationsFetched: state.session.fetchedTranslations.stockListManagement,
  isUserAdmin: state.session.isUserAdmin,
});

export default connect(mapStateToProps, {
  showSpinner, hideSpinner, fetchTranslations,
})(Tablero);


