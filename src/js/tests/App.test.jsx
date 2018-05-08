/* eslint-disable no-undef */

import React from 'react';
import Enzyme, { shallow } from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';

import App from '../components/app';

Enzyme.configure({ adapter: new Adapter() });

test('App component is rendering properly', () => {
  const wrapper = shallow(<App />);

  expect(wrapper.find('#mainApp')).toHaveLength(1);

  expect(wrapper.text()).toEqual('React Component');
});
