import { render, screen } from '@testing-library/react';

import renderHandlingIcons from 'utils/product-handling-icons';

const icon = [{
  icon: 'fa-snowflake',
  color: '#3bafda',
  label: 'Cold chain',
}];

describe('product-handling-icons', () => {
  it('should return null if handlingIcons is not specified', () => {
    expect(renderHandlingIcons()).toBeNull();
    expect(renderHandlingIcons([])).toBeNull();
  });

  it('should return fontAwesomeIcon', () => {
    expect(renderHandlingIcons(icon)).not.toBeNull();
  });

  it('should render properly', () => {
    render(renderHandlingIcons(icon));
    expect(screen.findByTestId('product-handling-icons'));
  });
});
