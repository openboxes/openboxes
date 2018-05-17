import React from 'react';
import Enzyme, { shallow } from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';

import StockMovement from '../components/StockMovement';

Enzyme.configure({ adapter: new Adapter() });

test('StockMovement component is rendering properly', () => {
  const wrapper = shallow(<StockMovement />);

  expect(wrapper.find('#mainApp')).toHaveLength(1);

  expect(wrapper.text()).toEqual('React Component');
});
