import React from 'react';

import { fireEvent, render, screen } from '@testing-library/react';
import { Provider } from 'react-redux';
import { BrowserRouter as Router } from 'react-router-dom';
import renderer from 'react-test-renderer';

import CreateInvoicePage from 'components/invoice/CreateInvoicePage';
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
    renderedWizard = renderer.create(<Router><Provider
      store={store}
    ><Wizard {...props} />
    </Provider>
    </Router>);
  });

  it('should match snapshot', () => {
    expect(renderedWizard.toJSON())
      .toMatchSnapshot();
  });

  it('should render component correctly', () => {
    expect(renderedWizard.root.findByProps({ 'data-testid': 'content-wrap' }))
      .toBeTruthy();
  });
});

describe('wizardPage component', () => {
  beforeEach(() => {
    renderedWizardPage = renderer.create(<Router><Provider
      store={store}
    ><WizardPage
      pageList={[CreateInvoicePage]}
      nextPage={CreateInvoicePage}
      prevPage={CreateInvoicePage}
      goToPage={CreateInvoicePage}
      currentPage={1}
    />
    </Provider>
    </Router>);
  });

  it('should match snapshot', () => {
    expect(renderedWizardPage.toJSON())
      .toMatchSnapshot();
  });

  it('should render component correctly', () => {
    expect(renderedWizardPage.root.findByProps({ 'data-testid': 'wizardPage' }))
      .toBeTruthy();
  });
});

describe('wizardSteps component', () => {
  beforeEach(() => {
    renderedWizardSteps = renderer.create(<Router><Provider
      store={store}
    ><WizardSteps steps={['firstTestStep', 'secondTestStep']} currentStep={1} />
    </Provider>
    </Router>);
  });

  it('should match snapshot', () => {
    expect(renderedWizardSteps.toJSON())
      .toMatchSnapshot();
  });

  it('should have an active element', () => {
    expect(renderedWizardSteps.root.findByProps({ 'data-testid': 'active' }))
      .toBeTruthy();
  });

  it('should have an inactive element', () => {
    expect(renderedWizardSteps.root.findByProps({ 'data-testid': 'inactive' }))
      .toBeTruthy();
  });
});

describe('wizardTitle component', () => {
  beforeEach(() => {
    renderedWizardTitle = renderer.create(<Router><Provider store={store}><WizardTitle
      title={[{ title: '' }]}
    />
    </Provider>
    </Router>);
  });

  it('should match snapshot', () => {
    expect(renderedWizardTitle.toJSON())
      .toMatchSnapshot();
  });

  it('should render component correctly', () => {
    expect(renderedWizardTitle.root.findByProps({ 'data-testid': 'wizardTitle' }))
      .toBeTruthy();
  });

  it('should display text correctly', () => {
    render(<Router><Provider store={store}><WizardTitle title={[{
      title: '',
      text: 'test',
    }]}
    />
    </Provider>
    </Router>);
    expect(screen.getByText('test'))
      .toBeTruthy();
  });
});
