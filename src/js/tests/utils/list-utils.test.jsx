import {
  findActions,
  getParamList,
  hasMinimumRequiredRole,
  transformFilterParams,
} from 'utils/list-utils';

import '@testing-library/jest-dom';

describe('hasMinimumRequiredRole()', () => {
  it('should return true if have minimum required role', () => {
    const minimumRequiredRole = hasMinimumRequiredRole('Manager', 'Admin');
    expect(minimumRequiredRole)
      .toBeTruthy();
  });

  it('should return true if roles are equal', () => {
    const minimumRequiredRole = hasMinimumRequiredRole('Manager', 'Manager');
    expect(minimumRequiredRole)
      .toBeTruthy();
  });

  it('should return false if do not have minimum required role', () => {
    const minimumRequiredRole = hasMinimumRequiredRole('Manager', 'Browser');
    expect(minimumRequiredRole)
      .toBeFalsy();
  });

  it('should return false if role does not exist', () => {
    const minimumRequiredRole = hasMinimumRequiredRole('Test', 'Superuser');
    expect(minimumRequiredRole)
      .toBeFalsy();
  });

  it('should return false if both roles does not exist', () => {
    const minimumRequiredRole = hasMinimumRequiredRole('Test', 'Test2');
    expect(minimumRequiredRole)
      .toBeFalsy();
  });
});

describe('findActions()', () => {
  it('should filter by status', () => {
    const actionList = [
      {
        id: 1,
        statuses: ['test'],
      },
      {
        id: 2,
        statuses: ['test', 'test2'],
      },
      {
        id: 3,
        statuses: ['test'],
      },
    ];
    const filteredByStatus = findActions(actionList, { original: { status: 'test2' } }, {
      supportedActivities: [],
      highestRole: 'Admin',
    });
    expect(filteredByStatus.length)
      .toBe(1);
    expect(filteredByStatus[0].id)
      .toBe(2);
  });

  it('should filter by activity code', () => {
    const actionList = [
      {
        id: 1,
        activityCode: ['testActivity2'],
      },
      {
        id: 2,
        activityCode: ['testActivity'],
      },
      {
        id: 3,
        activityCode: ['testActivity2'],
      },
    ];
    const filteredByStatus = findActions(actionList, { original: { status: 'test' } }, {
      supportedActivities: ['testActivity'],
      highestRole: 'Admin',
    });
    expect(filteredByStatus.length)
      .toBe(1);
    expect(filteredByStatus[0].id)
      .toBe(2);
  });

  it('should filter by minimum required role', () => {
    const actionList = [
      {
        id: 1,
        minimumRequiredRole: 'Admin',
      },
      {
        id: 2,
        minimumRequiredRole: 'Assistant',
      },
      {
        id: 3,
        minimumRequiredRole: 'Browser',
      },
    ];
    const filteredByMinimumRequiredRole = findActions(actionList, { original: { status: 'test' } }, {
      supportedActivities: ['testActivity'],
      highestRole: 'Manager',
    });
    expect(filteredByMinimumRequiredRole.length)
      .toBe(2);
    expect(filteredByMinimumRequiredRole[0].id)
      .toBe(2);
    expect(filteredByMinimumRequiredRole[1].id)
      .toBe(3);
  });

  it('should return all actions', () => {
    const actionList = [
      {
        id: 1,
      },
      {
        id: 2,
      },
      {
        id: 3,
      },
    ];
    const allActions = findActions(actionList, { original: { status: 'test' } }, {
      supportedActivities: ['testActivity'],
      highestRole: 'Manager',
    });
    expect(allActions.length)
      .toBe(3);
  });

  it('should return empty list', () => {
    const actionList = [
      {
        id: 1,
        minimumRequiredRole: 'Superuser',
      },
      {
        id: 2,
        minimumRequiredRole: 'Superuser',
      },
      {
        id: 3,
        minimumRequiredRole: 'Superuser',
      },
    ];
    const allActions = findActions(actionList, { original: { status: 'test' } }, {
      supportedActivities: ['testActivity'],
      highestRole: 'Manager',
    });
    expect(allActions.length)
      .toBe(0);
  });

  it('should filter using custom filters', () => {
    const customFilter = action => action.id % 2;
    const actionList = [
      {
        id: 1,
      },
      {
        id: 2,
      },
      {
        id: 3,
      },
    ];
    const customFilteredActions = findActions(actionList, { original: { status: 'test' } }, {
      supportedActivities: ['testActivity'],
      highestRole: 'Manager',
      customFilter,
    });
    expect(customFilteredActions.length)
      .toBe(2);
    expect(customFilteredActions[0].id)
      .toBe(1);
    expect(customFilteredActions[1].id)
      .toBe(3);
  });
});

describe('transformFilterParams()', () => {
  it('should transform normal key', () => {
    const filterValues = {
      origin: {
        id: 'test',
        name: 'test',
      },
    };
    const filterAccessors = {
      origin: {
        name: 'origin',
        accessor: 'id',
      },
    };
    const transformedFilterParams = transformFilterParams(filterValues, filterAccessors);
    expect(Object.keys(transformedFilterParams).length)
      .toBe(1);
    expect(transformedFilterParams.origin)
      .toBe('test');
  });
  it('should transform key which is an array', () => {
    const filterValues = {
      origin: [{
        id: 'test',
        name: 'test',
      }, {
        id: 'test2',
        name: 'test2',
      }],
    };
    const filterAccessors = {
      origin: {
        name: 'origin',
        accessor: 'id',
      },
    };
    const transformedFilterParams = transformFilterParams(filterValues, filterAccessors);
    expect(Object.keys(transformedFilterParams).length)
      .toBe(1);
    expect(transformedFilterParams.origin.length)
      .toBe(2);
    expect(transformedFilterParams.origin[0])
      .toBe('test');
    expect(transformedFilterParams.origin[1])
      .toBe('test2');
  });

  it('should return empty object', () => {
    const filterValues = { test: 'test' };
    const filterAccessors = { accessorTest: 'accessorTest' };
    const transformedFilterParams = transformFilterParams(filterValues, filterAccessors);
    expect(Object.keys(transformedFilterParams).length)
      .toBe(0);
  });
});

describe('getParamList()', () => {
  it('transform object into an array', () => {
    const paramList = getParamList({
      0: 1,
      1: 2,
      2: 3,
    });
    expect(paramList.length)
      .toBe(1);
  });

  it('transform string into an array', () => {
    const paramList = getParamList('test string');
    expect(paramList.length)
      .toBe(1);
    expect(paramList[0])
      .toBe('test string');
  });
});
