import React from 'react';
import Enzyme, { shallow } from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';

import StockMovement from '../components/stock-movement-wizard/StockMovement';

Enzyme.configure({ adapter: new Adapter() });

test('StockMovement component is rendering properly', () => {
  const wrapper = shallow(<StockMovement />);

  expect(wrapper.text()).toEqual('<WizardSteps /><ReduxForm />');
  // TODO add more tests once we add more steps to Stock Movement
});
