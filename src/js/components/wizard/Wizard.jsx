import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';

import WizardSteps from '../wizard/WizardSteps';
import WizardPage from '../wizard/WizardPage';
import WizardTitle from '../wizard/WizardTitle';

/** Wizard component. */
class Wizard extends Component {
  constructor(props) {
    super(props);

    this.state = {
      currentPage: this.props.currentPage || 1,
      prevPage: this.props.prevPage || 1,
      values: this.props.initialValues,
    };

    this.nextPage = this.nextPage.bind(this);
    this.prevPage = this.prevPage.bind(this);
    this.goToPage = this.goToPage.bind(this);
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.currentPage !== this.props.currentPage) {
      this.goToPage(nextProps.currentPage, nextProps.initialValues);
    }
  }

  /**
   * Sets current page state as a previous page and takes user to the next page.
   * @param {object} values
   * @public
   */
  nextPage(values) {
    if (this.props.pageList.length > this.state.currentPage) {
      this.setState({
        prevPage: this.state.currentPage, currentPage: this.state.currentPage + 1, values,
      });
    } else {
      this.setState({ values });
    }
  }

  /**
   * Returns user to the previous page.
   * @param {object} values
   * @public
   */
  prevPage(values) {
    if (this.state.prevPage > 0) {
      this.setState({
        prevPage: this.state.prevPage - 1, currentPage: this.state.prevPage, values,
      });
    } else {
      this.setState({ values });
    }
  }

  /**
   * Sets current page state as a previous page and takes user to the given number page.
   * @param {object} values
   * @param {number} currentPage
   * @public
   */
  goToPage(currentPage, values) {
    this.setState({ prevPage: currentPage - 1, currentPage, values });
  }

  render() {
    const { currentPage, values } = this.state;
    const {
      title, pageList, stepList, additionalTitle,
    } = this.props;

    return (
      <div className="content-wrap">
        <WizardSteps steps={stepList} currentStep={currentPage} />
        <div className="panel panel-primary">
          <WizardTitle title={title} additionalTitle={additionalTitle} />
          <WizardPage
            pageList={pageList}
            currentPage={currentPage}
            initialValues={values}
            nextPage={this.nextPage}
            prevPage={this.prevPage}
            goToPage={this.goToPage}
          />
        </div>
      </div>
    );
  }
}

const mapStateToProps = state => ({
  locale: state.session.activeLanguage,
  stockMovementTranslationsFetched: state.session.fetchedTranslations.stockMovement,
  hasPackingSupport: state.session.currentLocation.hasPackingSupport,
});

export default connect(mapStateToProps, {
  // showSpinner, hideSpinner, fetchTranslations,
})(Wizard);

Wizard.propTypes = {
  /** Initial components' data */
  initialValues: PropTypes.shape({
    shipmentStatus: PropTypes.string,
  }),
  title: PropTypes.string.isRequired,
  additionalTitle: PropTypes.oneOf([PropTypes.string, PropTypes.func]),
  currentPage: PropTypes.number.isRequired,
  prevPage: PropTypes.number.isRequired,
  pageList: PropTypes.arrayOf(PropTypes.func).isRequired,
  stepList: PropTypes.arrayOf(PropTypes.string).isRequired,
};

Wizard.defaultProps = {
  initialValues: {},
  additionalTitle: null,
};
