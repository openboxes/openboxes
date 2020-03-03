import React from 'react';
import renderer from 'react-test-renderer';
import WizardSteps from '../../components/wizard/WizardSteps';

it('WizardSteps renders correctly', () => {
  const steps = ['Create', 'Add items', 'Edit', 'Pick', 'Send'];
  const currentStep = 2;
  const component = renderer
    .create(<WizardSteps steps={steps} currentStep={currentStep} />);
  const tree = component.toJSON();
  expect(tree).toMatchSnapshot();
});
