import React from 'react';

import { render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { BrowserRouter as Router } from 'react-router-dom';

import CreateInvoicePage from 'components/invoice/create/CreateInvoicePage';
import Wizard from 'components/wizard/Wizard';
import WizardPage from 'components/wizard/WizardPage';
import WizardSteps from 'components/wizard/WizardSteps';
import WizardTitle from 'components/wizard/WizardTitle';

import store from '../../store';

let renderedWizard;
let renderedWizardPage;
let renderedWizardSteps;
let renderedWizardTitle;

describe('wizard component', () => {
  beforeEach(() => {
    const props = {
      title: [{ title: '' }],
      currentPage: 1,
      prevPage: 1,
      pageList: [CreateInvoicePage],
      stepList: ['Create', 'Add items', 'Confirm'],
    };
    renderedWizard = render(
      <Router>
        <Provider
          store={store}
        >
          <Wizard {...props} />
        </Provider>
      </Router>,
    );
  });

  it('should match snapshot', () => {
    expect(renderedWizard.asFragment())
      .toMatchSnapshot();
  });

  it('should render component correctly', () => {
    expect(renderedWizard.getByTestId('content-wrap'))
      .toBeTruthy();
  });
});

describe('wizardPage component', () => {
  beforeEach(() => {
    renderedWizardPage = render(
      <Router>
        <Provider
          store={store}
        >
          <WizardPage
            pageList={[CreateInvoicePage]}
            nextPage={CreateInvoicePage}
            prevPage={CreateInvoicePage}
            goToPage={CreateInvoicePage}
            currentPage={1}
          />
        </Provider>
      </Router>,
    );
  });

  it('should match snapshot', () => {
    expect(renderedWizardPage.asFragment())
      .toMatchSnapshot();
  });

  it('should render component correctly', () => {
    expect(renderedWizardPage.getByTestId('wizardPage'))
      .toBeTruthy();
  });
});

describe('wizardSteps component', () => {
  beforeEach(() => {
    renderedWizardSteps = render(
      <Router>
        <Provider
          store={store}
        >
          <WizardSteps steps={['firstTestStep', 'secondTestStep']} currentStep={1} />
        </Provider>
      </Router>,
    );
  });

  it('should match snapshot', () => {
    expect(renderedWizardSteps.asFragment())
      .toMatchSnapshot();
  });

  it('should have an active element', () => {
    expect(renderedWizardSteps.container.querySelector('[data-stepstate="active"]'))
      .toBeTruthy();
  });

  it('should have an inactive element', () => {
    expect(renderedWizardSteps.container.querySelector('[data-stepstate="inactive"]'))
      .toBeTruthy();
  });
});

describe('wizardTitle component', () => {
  beforeEach(() => {
    renderedWizardTitle = render(
      <Router>
        <Provider store={store}>
          <WizardTitle
            title={[{ title: '' }]}
          />
        </Provider>
      </Router>,
    );
  });

  it('should match snapshot', () => {
    expect(renderedWizardTitle.asFragment())
      .toMatchSnapshot();
  });

  it('should render component correctly', () => {
    expect(renderedWizardTitle.getByTestId('wizardTitle'))
      .toBeTruthy();
  });

  it('should display text correctly', () => {
    render(
      <Router>
        <Provider store={store}>
          <WizardTitle title={[{
            title: '',
            text: 'test',
          }]}
          />
        </Provider>
      </Router>,
    );
    expect(screen.getByText('test'))
      .toBeTruthy();
  });
});
