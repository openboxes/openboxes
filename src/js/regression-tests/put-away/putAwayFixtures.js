// Test-only helper module, outside the *.test.* pattern the lint rule exempts.
// eslint-disable-next-line import/no-extraneous-dependencies
import { fireEvent, screen } from '@testing-library/react';

// OBAF renamed the put-away URLs to /putaway/* (consts/applicationUrls.js, api/urls.js,
// Router.jsx).
export const PATH = '**/putaway/create/:putAwayId?';
export const CREATE_ENTRY = '/openboxes/putaway/create';

export const product = (id, name, extra = {}) => ({
  id,
  productCode: id.toUpperCase(),
  name,
  displayNameOrDefaultName: name,
  ...extra,
});

/** A line waiting in the receiving bin, as GET /api/putaways returns it. */
export const candidate = ({
  id, name, quantity = 10, status = 'READY', sm = 'sm1', ...rest
}) => ({
  product: product(id, name),
  inventoryItem: { id: `ii-${id}`, lotNumber: `LOT-${id}`, expirationDate: new Date('2030-01-15T12:00:00').toISOString() },
  recipient: { name: 'Ward A' },
  quantity,
  quantityAvailable: quantity,
  stockMovement: { id: sm, name: `SM-${sm}` },
  putawayStatus: status,
  currentFacility: { id: 'loc1' },
  ...rest,
});

/** A line of a started put-away, as GET /api/putaways/:id returns it. */
export const putawayItem = ({
  id, name, quantity = 10, quantityAvailable = quantity, sm = 'sm1',
  putawayLocation = {}, preferredBin = {}, splitItems = [], ...rest
}) => ({
  id: `pai-${id}`,
  product: product(id, name),
  inventoryItem: { id: `ii-${id}`, lotNumber: `LOT-${id}`, expirationDate: new Date('2030-01-15T12:00:00').toISOString() },
  recipient: { name: 'Ward A' },
  quantity,
  quantityAvailable,
  stockMovement: { id: sm, name: `SM-${sm}` },
  putawayStatus: 'PENDING',
  putawayFacility: { id: 'loc1' },
  currentLocation: { id: 'recv' },
  currentBins: 'RECEIVING',
  putawayLocation,
  preferredBin,
  splitItems,
  ...rest,
});

/** Opens the react-select inside `container` and picks the option labelled `label`. */
export const pickOption = (container, label) => {
  fireEvent.keyDown(container.querySelector('input'), { key: 'ArrowDown', keyCode: 40 });
  fireEvent.click(screen.getAllByText(label).find((el) => el.closest('[role="listitem"]')));
};
