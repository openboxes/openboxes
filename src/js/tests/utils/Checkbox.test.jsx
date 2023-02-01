import React from 'react';

import { fireEvent, render, screen } from '@testing-library/react';
import renderer from 'react-test-renderer';

import Checkbox from 'utils/Checkbox';

import '@testing-library/jest-dom';

let checkbox;
let customCheckbox;
let withLabelCheckbox;

describe('custom checkbox', () => {
  beforeEach(() => {
    customCheckbox = (<Checkbox custom value={false} />);
  });

  it('should match snapshot', () => {
    const renderedCheckbox = renderer.create(customCheckbox);
    expect(renderedCheckbox.toJSON()).toMatchSnapshot();
  });

  it('should not be checked', () => {
    render(customCheckbox);
    const foundCheckbox = screen.getByRole('checkbox');
    expect(foundCheckbox).toBeInTheDocument();
    expect(foundCheckbox).not.toBeChecked();
  });

  it('should be checked after selecting', () => {
    const { rerender } = render(customCheckbox);
    const foundCheckbox = screen.getByRole('checkbox');
    expect(foundCheckbox).toBeInTheDocument();
    expect(foundCheckbox).not.toBeChecked();
    fireEvent.click(foundCheckbox);
    rerender(<Checkbox custom value />);
    expect(screen.getByRole('checkbox')).toBeInTheDocument();
    expect(screen.getByRole('checkbox')).toBeChecked();
  });
});

describe('checkbox with label', () => {
  beforeEach(() => {
    withLabelCheckbox = (<Checkbox withLabel value={false} label="test label" />);
  });

  it('should match snapshot', () => {
    const renderedCheckbox = renderer.create(withLabelCheckbox);
    expect(renderedCheckbox.toJSON()).toMatchSnapshot();
  });

  it('should not be checked', () => {
    render(withLabelCheckbox);
    const foundCheckbox = screen.getByRole('checkbox');
    expect(foundCheckbox).toBeInTheDocument();
    expect(foundCheckbox).not.toBeChecked();
  });

  it('should be checked after selecting', () => {
    const { rerender } = render(withLabelCheckbox);
    const foundCheckbox = screen.getByRole('checkbox');
    expect(foundCheckbox).toBeInTheDocument();
    expect(foundCheckbox).not.toBeChecked();
    fireEvent.click(foundCheckbox);
    rerender(<Checkbox withLabel value label="test label" />);
    expect(screen.getByRole('checkbox')).toBeInTheDocument();
    expect(screen.getByRole('checkbox')).toBeChecked();
  });

  it('should have label', () => {
    expect(screen.findByText('test label')).toBeTruthy();
  });
});

describe('default checkbox', () => {
  beforeEach(() => {
    checkbox = (<Checkbox value={false} />);
  });

  it('should match snapshot', () => {
    const renderedCheckbox = renderer.create(checkbox);
    expect(renderedCheckbox.toJSON()).toMatchSnapshot();
  });

  it('should not be checked', () => {
    render(checkbox);
    const foundCheckbox = screen.getByRole('checkbox');
    expect(foundCheckbox).toBeInTheDocument();
    expect(foundCheckbox).not.toBeChecked();
  });

  it('should be checked after selecting', () => {
    const { rerender } = render(checkbox);
    const foundCheckbox = screen.getByRole('checkbox');
    expect(foundCheckbox).toBeInTheDocument();
    expect(foundCheckbox).not.toBeChecked();
    fireEvent.click(foundCheckbox);
    rerender(<Checkbox custom value />);
    expect(screen.getByRole('checkbox')).toBeInTheDocument();
    expect(screen.getByRole('checkbox')).toBeChecked();
  });
});

