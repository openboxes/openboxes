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


describe('wizard component test', () => {
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

  it('test if wizard component matches snapshot', () => {
    expect(renderedWizard.toJSON())
      .toMatchSnapshot();
  });

  it('test if wizard component correctly rendering', () => {
    expect(renderedWizard.root.findByProps({ className: 'content-wrap' }))
      .toBeTruthy();
  });
});

describe('wizardPage component tests', () => {
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

  it('test if wizardPage matches snapshot', () => {
    expect(renderedWizardPage.toJSON())
      .toMatchSnapshot();
  });

  it('test if wizardPage correctly rendering', () => {
    expect(renderedWizardPage.root.findByProps({ className: 'panel-body m-4' }))
      .toBeTruthy();
  });
});

describe('wizardSteps component tests', () => {
  beforeEach(() => {
    renderedWizardSteps = renderer.create(<Router><Provider
      store={store}
    ><WizardSteps steps={['firstTestStep', 'secondTestStep']} currentStep={1} />
    </Provider>
    </Router>);
  });

  it('test if wizardSteps matches snapshot', () => {
    expect(renderedWizardSteps.toJSON())
      .toMatchSnapshot();
  });

  it('test if wizardSteps has active element', () => {
    expect(renderedWizardSteps.root.findByProps({ className: 'step-container active' }))
      .toBeTruthy();
  });

  it('test if wizardSteps has inactive elements', () => {
    expect(renderedWizardSteps.root.findByProps({ className: 'step-container ' }))
      .toBeTruthy();
  });
});

describe('wizardTitle components tests', () => {
  beforeEach(() => {
    renderedWizardTitle = renderer.create(<Router><Provider store={store}><WizardTitle
      title={[{ title: '' }]}
    />
    </Provider>
    </Router>);
  });

  it('test if wizardTitle matches snapshot', () => {
    expect(renderedWizardTitle.toJSON())
      .toMatchSnapshot();
  });

  it('test if wizardTitle correctly rendering', () => {
    expect(renderedWizardTitle.root.findByProps({ className: 'panel-heading movement-number' }))
      .toBeTruthy();
  });

  it('test if wizardTitle correctly displaying text', () => {
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
