import React, { Component } from 'react';
import { defaults, Line } from 'react-chartjs-2';
import { getTranslate } from 'react-localize-redux';
import { connect } from 'react-redux';
import 'react-table/react-table.css';
import { fetchTranslations, hideSpinner, showSpinner } from '../../actions';
import { translateWithDefaultMessage } from '../../utils/Translate';

// Disable animating charts by default.
defaults.global.legend = false;

class Tablero extends Component {
  constructor(props) {
    super(props);

    this.state = {
      expirationSummary: {
        labels: ['January', 'February', 'March', 'April', 'May', 'June'],
        datasets: [{
          label: 'Expiration summary',
          fill: true,
          data: [12, 30, 26, 7, 19, 17],
        }],
      }
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

    this.setState({ isDataLoading: false })
  }

  render() {
    return (
      <div className="cardsContainer">
        <div className="cardComponent">
          <div className="numberCard">
            <span className="titleCard"> Bin Location Summary</span>
            <span className="resultCard"> 2,696</span>
            <span className="subtitleCard"> In stock </span>
          </div>
          <div className="numberCard">
            <span className="titleCard"> Bin Location Summary</span>
            <span className="resultCard"> 1,082</span>
            <span className="subtitleCard"> Out of Stock </span>
          </div>
          <div className="numberCard">
            <span className="titleCard"> S.M not received </span>
            <span className="resultCard"> 468</span>
            <span className="subtitleCard"> N° of S.M not shipped </span>
          </div>
          <div className="numberCard">
            <span className="titleCard"> User incomplete tasks </span>
            <span className="resultCard"> 188</span>
            <span className="subtitleCard"> N° of put aways not co... </span>
          </div>
          <div className="numberCard">
            <span className="titleCard"> Discrepancy </span>
            <span className="resultCard"> 290</span>
            <span className="subtitleCard"> N° of items received </span>
          </div>
        </div>
        <div className="cardComponent">
          <div className="graphCard">
            <div className="headerCard">
              <span className="titleCard"> Expiration summary </span>
            </div>
            <div className="contentCard">
              <Line data={this.state.expirationSummary} width={632} height={300} />
            </div>
          </div>
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
