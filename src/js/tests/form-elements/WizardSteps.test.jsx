import React from 'react';

import { render } from '@testing-library/react';

import WizardSteps from 'components/wizard/WizardSteps';

it('WizardSteps renders correctly', () => {
  const steps = ['Create', 'Add items', 'Edit', 'Pick', 'Send'];
  const currentStep = 2;
  const component = render(<WizardSteps steps={steps} currentStep={currentStep} />);
  const tree = component.asFragment();
  expect(tree).toMatchSnapshot();
});
