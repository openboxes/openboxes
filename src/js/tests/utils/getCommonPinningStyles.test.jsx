import getCommonPinningStyles from 'utils/getCommonPinningStyles';

const mockColumn = {
  getIsPinned: jest.fn(),
  getIsLastColumn: jest.fn(),
  getStart: jest.fn(),
  getSize: jest.fn(),
};

describe('getCommonPinningStyles', () => {
  const mockParams = {
    column: mockColumn,
    flexWidth: undefined,
    isScreenWiderThanTable: false,
    dataLength: 10,
    loading: false,
  };

  beforeEach(() => {
    jest.clearAllMocks();
    mockColumn.getIsPinned.mockReturnValue(false);
    mockColumn.getIsLastColumn.mockReturnValue(false);
    mockColumn.getStart.mockReturnValue(0);
    mockColumn.getSize.mockReturnValue(100);
  });

  it('should match snapshot for sample params', () => {
    const result = getCommonPinningStyles(
      mockParams.column,
      mockParams.flexWidth,
      mockParams.isScreenWiderThanTable,
      mockParams.dataLength,
      mockParams.loading,
    );
    expect(result)
      .toMatchSnapshot();
  });

  it('returns styles for non-pinned column', () => {
    const result = getCommonPinningStyles(
      mockParams.column,
      mockParams.flexWidth,
      mockParams.isScreenWiderThanTable,
      mockParams.dataLength,
      mockParams.loading,
    );
    expect(result)
      .toEqual({
        boxShadow: undefined,
        clipPath: undefined,
        marginRight: undefined,
        left: undefined,
        position: false,
        flex: 100,
        width: 100,
        zIndex: 0,
        background: false,
      });
  });

  it('returns styles for last left-pinned column', () => {
    mockColumn.getIsPinned.mockReturnValue('left');
    mockColumn.getIsLastColumn.mockReturnValue(true);
    mockColumn.getStart.mockReturnValue(50);
    const result = getCommonPinningStyles(
      mockParams.column,
      mockParams.flexWidth,
      mockParams.isScreenWiderThanTable,
      mockParams.dataLength,
      mockParams.loading,
    );
    expect(result)
      .toEqual({
        boxShadow: '0 0 15px #00000040',
        clipPath: 'inset(0 -15px 0 0)',
        marginRight: '5px',
        left: '50px',
        position: 'sticky',
        flex: 100,
        width: 100,
        zIndex: 1,
        background: 'white',
      });
  });

  it('disables sticky position when conditions are not met', () => {
    mockColumn.getIsPinned.mockReturnValue('left');
    const result1 = getCommonPinningStyles(
      mockParams.column,
      mockParams.flexWidth,
      true,
      mockParams.dataLength,
      mockParams.loading,
    );
    const result2 = getCommonPinningStyles(
      mockParams.column,
      mockParams.flexWidth,
      mockParams.isScreenWiderThanTable,
      0,
      mockParams.loading,
    );
    const result3 = getCommonPinningStyles(
      mockParams.column,
      mockParams.flexWidth,
      mockParams.isScreenWiderThanTable,
      mockParams.dataLength,
      true,
    );
    expect(result1.position)
      .toBeFalsy();
    expect(result2.position)
      .toBeFalsy();
    expect(result3.position)
      .toBeFalsy();
  });

  it('handles undefined column methods safely', () => {
    const undefinedColumn = {
      getIsPinned: jest.fn()
        .mockReturnValue(undefined),
      getIsLastColumn: jest.fn()
        .mockReturnValue(undefined),
      getStart: jest.fn()
        .mockReturnValue(undefined),
      getSize: jest.fn()
        .mockReturnValue(undefined),
    };
    const result = getCommonPinningStyles(
      undefinedColumn,
      mockParams.isScreenWiderThanTable,
      mockParams.dataLength,
      mockParams.loading,
    );
    expect(result)
      .toEqual({
        boxShadow: undefined,
        clipPath: undefined,
        marginRight: undefined,
        left: undefined,
        position: undefined,
        flex: undefined,
        width: undefined,
        zIndex: 0,
        background: undefined,
      });
  });
});
