import React, { Component } from 'react';

import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import WizardPage from 'components/wizard/WizardPage';
import WizardSteps from 'components/wizard/WizardSteps';
import WizardTitle from 'components/wizard/WizardTitle';

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
    const { currentPage } = this.state;
    if (this.props.pageList.length > currentPage) {
      this.setState({
        prevPage: currentPage, currentPage: currentPage + 1, values,
      });
      this.props.updateWizardValues(currentPage + 1, values);
    } else {
      this.setState({ values });
      this.props.updateWizardValues(currentPage, values);
    }
  }

  /**
   * Returns user to the previous page.
   * @param {object} values
   * @public
   */
  prevPage(values) {
    const { prevPage } = this.state;
    if (prevPage > 0) {
      this.setState({
        prevPage: prevPage - 1, currentPage: prevPage, values,
      });
      this.props.updateWizardValues(prevPage, values);
    } else {
      this.setState({ values });
      this.props.updateWizardValues(1, values);
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
    this.props.updateWizardValues(currentPage, values);
  }

  render() {
    const { currentPage, values } = this.state;
    const {
      title, pageList, stepList, additionalTitle, additionalProps, showStepNumber,
    } = this.props;

    return (
      <div className="content-wrap" data-testid="content-wrap">
        <WizardTitle title={title} additionalTitle={additionalTitle} values={values} />
        <WizardSteps steps={stepList} currentStep={currentPage} showStepNumber={showStepNumber} />
        <div className="panel panel-primary">
          <WizardPage
            pageList={pageList}
            currentPage={currentPage}
            initialValues={values}
            nextPage={this.nextPage}
            prevPage={this.prevPage}
            goToPage={this.goToPage}
            additionalProps={additionalProps}
          />
        </div>
      </div>
    );
  }
}

const mapStateToProps = (state) => ({
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
  title: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  additionalTitle: PropTypes.oneOf([PropTypes.string, PropTypes.func]),
  currentPage: PropTypes.number.isRequired,
  prevPage: PropTypes.number.isRequired,
  pageList: PropTypes.arrayOf(PropTypes.func).isRequired,
  stepList: PropTypes.arrayOf(PropTypes.string).isRequired,
  updateWizardValues: PropTypes.func,
  additionalProps: PropTypes.shape({}),
  showStepNumber: PropTypes.bool,
};

Wizard.defaultProps = {
  initialValues: {},
  additionalProps: {},
  additionalTitle: null,
  updateWizardValues: () => {},
  showStepNumber: false,
};
