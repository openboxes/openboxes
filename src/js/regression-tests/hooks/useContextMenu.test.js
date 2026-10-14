import { act, renderHook } from '@testing-library/react-hooks';

import actionItemType from 'consts/actionItemType';
import useContextMenu from 'hooks/useContextMenu';

describe('useContextMenu', () => {
  const render = () => renderHook(() => useContextMenu()).result;

  it('renders an action with a link as a link, a react link or a button', () => {
    const result = render();

    expect(result.current.getActionItemType({ href: '/x' })).toBe(actionItemType.LINK);
    expect(result.current.getActionItemType({ href: '/x', reactLink: true }))
      .toBe(actionItemType.REACT_LINK);
    expect(result.current.getActionItemType({ onClick: jest.fn() })).toBe(actionItemType.BUTTON);
    expect(result.current.getActionItemType({ reactLink: true })).toBe(actionItemType.BUTTON);
  });

  it('appends the id to a string href', () => {
    expect(render().current.buildLink({ href: '/openboxes/order/show' }, 'o1'))
      .toBe('/openboxes/order/show/o1');
  });

  it('builds the link with an href function', () => {
    const href = jest.fn((id) => `/openboxes/product/${id}/edit`);

    expect(render().current.buildLink({ href }, 'p1')).toBe('/openboxes/product/p1/edit');
    expect(href).toHaveBeenCalledWith('p1');
  });

  it('builds no link for an action without href', () => {
    expect(render().current.buildLink({ onClick: jest.fn() }, 'p1')).toBeNull();
  });

  it.each([
    ['top', 'dropup'],
    ['left', 'dropleft'],
    ['right', 'dropright'],
    ['bottom', ''],
    [undefined, ''],
  ])('maps the %p placement to the %p class', (placement, className) => {
    expect(render().current.getPositionClass(placement)).toBe(className);
  });

  it('opens and closes the popover', () => {
    const result = render();
    expect(result.current.isPopoverOpen).toBe(false);

    act(() => result.current.setIsPopoverOpen(true));

    expect(result.current.isPopoverOpen).toBe(true);
    expect(result.current.popoverRef).toEqual({ current: undefined });
  });
});
