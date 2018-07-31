const LOCATION_MOCKS = [
  { label: 'AlGhanem Group [Supplier]', value: { name: 'AlGhanem Group [Supplier]', type: 'Supplier' } },
  { label: 'Amazon [Supplier]', value: { name: 'Amazon [Supplier]', type: 'Supplier' } },
  { label: 'Bangalore 011 [Depot]', value: { name: 'Bangalore 011 [Depot]', type: 'Depot' } },
  { label: 'Bangalore- 20 Tech [Distributor]', value: { name: 'Bangalore- 20 Tech [Distributor]', type: 'Distributor' } },
  { label: 'Bangalore-Titan [Supplier]', value: { name: 'Bangalore-Titan [Supplier]', type: 'Supplier' } },
  { label: 'Bangalore-Vijayanagar [Warehouse]', value: { name: 'Bangalore-Vijayanagar [Warehouse]', type: 'Warehouse' } },
  { label: 'Belonia foods [Supplier]', value: { name: 'Belonia foods [Supplier]', type: 'Supplier' } },
  { label: 'Canada [Warehouse]', value: { name: 'Canada [Warehouse]', type: 'Warehouse' } },
  { label: 'CCS [Depot]', value: { name: 'CCS [Depot]', type: 'Depot' } },
  { label: 'CDMX [Cliente]', value: { name: 'CDMX [Cliente]', type: 'Cliente' } },
  { label: 'China [Supplier]', value: { name: 'China [Supplier]', type: 'Supplier' } },
  { label: 'CHT [Installation]', value: { name: 'CHT [Installation]', type: 'Installation' } },
  { label: 'Client Test [Cliente]', value: { name: 'Client Test [Cliente]', type: 'Cliente' } },
  { label: 'Corona CA [Depot]', value: { name: 'Corona CA [Depot]', type: 'Depot' } },
  { label: 'DELHI - BADARPUR [Book Wahrehouse]', value: { name: 'DELHI - BADARPUR [Book Wahrehouse]', type: 'Book Wahrehouse' } },
  { label: 'DELHI - EAST [Book Wahrehouse]', value: { name: 'DELHI - EAST [Book Wahrehouse]', type: 'Book Wahrehouse' } },
  { label: 'DELHI - IG AIRPORT [Book Wahrehouse]', value: { name: 'DELHI - IG AIRPORT [Book Wahrehouse]', type: 'Book Wahrehouse' } },
  { label: 'DELHI - NORTH [Book Wahrehouse]', value: { name: 'DELHI - NORTH [Book Wahrehouse]', type: 'Book Wahrehouse' } },
  { label: 'DELHI - SOUTH [Book Wahrehouse]', value: { name: 'DELHI - SOUTH [Book Wahrehouse]', type: 'Book Wahrehouse' } },
  { label: 'East End SH Clinic [SH Clinic]', value: { name: 'East End SH Clinic [SH Clinic]', type: 'SH Clinic' } },
  { label: 'East Medical Facility [Depot]', value: { name: 'East Medical Facility [Depot]', type: 'Depot' } },
  { label: 'East Pharmacy Depot [Depot]', value: { name: 'East Pharmacy Depot [Depot]', type: 'Depot' } },
  { label: 'East Pharmacy Supplier [Supplier]', value: { name: 'East Pharmacy Supplier [Supplier]', type: 'Supplier' } },
  { label: 'EXCELINDO [Supplier]', value: { name: 'EXCELINDO [Supplier]', type: 'Supplier' } },
  { label: 'FARIDABAD [Depot]', value: { name: 'FARIDABAD [Depot]', type: 'Depot' } },
  { label: 'Florida [Depot]', value: { name: 'Florida [Depot]', type: 'Depot' } },
  { label: 'GBAC [Installation]', value: { name: 'GBAC [Installation]', type: 'Installation' } },
  { label: 'Gujarat [Warehouse]', value: { name: 'Gujarat [Warehouse]', type: 'Warehouse' } },
  { label: 'Harbour [Depot]', value: { name: 'Harbour [Depot]', type: 'Depot' } },
  { label: 'HO-JKT [HO]', value: { name: 'HO-JKT [HO]', type: 'HO' } },
  { label: 'Importaciones VEN [Cliente]', value: { name: 'Importaciones VEN [Cliente]', type: 'Cliente' } },
  { label: 'INDIA - NOIDA [Book Wahrehouse]', value: { name: 'INDIA - NOIDA [Book Wahrehouse]', type: 'Book Wahrehouse' } },
  { label: 'ISAAC [Cliente]', value: { name: 'ISAAC [Cliente]', type: 'Cliente' } },
  { label: 'LC 002 [SH Clinic]', value: { name: 'LC 002 [SH Clinic]', type: 'SH Clinic' } },
  { label: 'MOM [Depot]', value: { name: 'MOM [Depot]', type: 'Depot' } },
  { label: 'Monitors [Book Wahrehouse]', value: { name: 'Monitors [Book Wahrehouse]', type: 'Book Wahrehouse' } },
  { label: 'New location [Depot]', value: { name: 'New location [Depot]', type: 'Depot' } },
  { label: 'OpenBoxes HQ [Depot]', value: { name: 'OpenBoxes HQ [Depot]', type: 'Depot' } },
  { label: 'Palletized [Depot]', value: { name: 'Palletized [Depot]', type: 'Depot' } },
  { label: 'PHH [Installation]', value: { name: 'PHH [Installation]', type: 'Installation' } },
  { label: 'PNC [Supplier]', value: { name: 'PNC [Supplier]', type: 'Supplier' } },
  { label: 'Porto [Book Wahrehouse]', value: { name: 'Porto [Book Wahrehouse]', type: 'Book Wahrehouse' } },
  { label: 'Principal [Principal]', value: { name: 'Principal [Principal]', type: 'Principal' } },
  { label: 'RIM India Pvt Ltd [Warehouse]', value: { name: 'RIM India Pvt Ltd [Warehouse]', type: 'Warehouse' } },
  { label: 'Rivercess [Depot]', value: { name: 'Rivercess [Depot]', type: 'Depot' } },
  { label: 'Sambas - Kalbar [Supplier]', value: { name: 'Sambas - Kalbar [Supplier]', type: 'Supplier' } },
  { label: 'Sepang Air Port [Book Wahrehouse]', value: { name: 'Sepang Air Port [Book Wahrehouse]', type: 'Book Wahrehouse' } },
  { label: 'SNET [Supplier]', value: { name: 'SNET [Supplier]', type: 'Supplier' } },
  { label: 'SO MEDAN [SO]', value: { name: 'SO MEDAN [SO]', type: 'SO' } },
  { label: 'SO SURABAYA [SO]', value: { name: 'SO SURABAYA [SO]', type: 'SO' } },
  { label: 'Toulouse [Supplier]', value: { name: 'Toulouse [Supplier]', type: 'Supplier' } },
  { label: 'Zwedru [Installation]', value: { name: 'Zwedru [Installation]', type: 'Installation' } },
  { label: 'Головной офис [Depot]', value: { name: 'Головной офис [Depot]', type: 'Depot' } },
];

const USERNAMES_MOCKS = ['Julian Benson',
  'Alyssa Chandler',
  'Diana Sharp',
  'Caleb Ramirez',
  'Sonia Rios',
  'Wilbur Moran',
  'Nadine Bowen',
  'Bradford Ingram',
  'Johnnie Rodriquez',
  'Kim Dennis',
];

const STOCK_LIST_ITEMS_MOCKS = {
  1: [
    {
      product: { productCode: 1, name: 'Advil 200mg' }, maxQuantity: 10, monthlyConsumption: 350,
    },
    {
      product: { productCode: 4, name: 'Similac Advance low iron 400g' }, maxQuantity: 10, monthlyConsumption: 250,
    },
    {
      product: { productCode: 3, name: 'Aspirin 20mg' }, maxQuantity: 10, monthlyConsumption: 140,
    },
  ],
  2: [
    {
      product: { productCode: 2, name: 'Tylenol 325mg' }, maxQuantity: 10, monthlyConsumption: 120,
    },
    {
      product: { productCode: 1, name: 'Advil 200mg' }, maxQuantity: 10, monthlyConsumption: 400,
    },
    {
      product: { productCode: 4, name: 'Similac Advance low iron 400g' }, maxQuantity: 10, monthlyConsumption: 120,
    },
  ],
  3: [
    {
      product: { productCode: 3, name: 'Aspirin 20mg' }, maxQuantity: 10, monthlyConsumption: 55,
    },
    {
      product: { productCode: 1, name: 'Advil 200mg' }, maxQuantity: 10, monthlyConsumption: 44,
    },
    {
      product: { productCode: 2, name: 'Tylenol 325mg' }, maxQuantity: 10, monthlyConsumption: 55,
    },
  ],
  4: [
    { product: { productCode: 4, name: 'Similac Advance low iron 400g' }, maxQuantity: 10 },
    {
      product: { productCode: 3, name: 'Aspirin 20mg' }, maxQuantity: 10, monthlyConsumption: 100,
    },
    {
      product: { productCode: 2, name: 'Advil 200mg' }, maxQuantity: 10, monthlyConsumption: 140,
    },
  ],
};

const SUBSTITUTIONS_MOCKS = {
  '00001': [
    {
      product: { productCode: 5, name: 'Ibuprofenum #1 200mg' },
      maxQuantity: 8,
      monthlyConsumption: 350,
      substitutionExpiryDate: '2018/11/31',
    },
    {
      product: { productCode: 6, name: 'Ibuprofenum #2 200mg' },
      maxQuantity: 5,
      monthlyConsumption: 250,
      substitutionExpiryDate: '2018/09/31',
    },
  ],
  '00002': [
    {
      product: { productCode: 7, name: 'Paracetamol #1 325mg' },
      maxQuantity: 2,
      monthlyConsumption: 120,
      substitutionExpiryDate: '2018/06/31',
    },
    {
      product: { productCode: 8, name: 'Paracetamol #2 200mg' },
      maxQuantity: 12,
      monthlyConsumption: 400,
      substitutionExpiryDate: '2018/07/31',
    },
    {
      product: { productCode: 9, name: 'Paracetamol #3 400g' },
      maxQuantity: 15,
      monthlyConsumption: 120,
      substitutionExpiryDate: '2018/09/31',
    },
  ],
  '00003': [
    {
      product: { productCode: 10, name: 'Aspirin #1 20mg' },
      maxQuantity: 14,
      monthlyConsumption: 55,
      substitutionExpiryDate: '2018/12/31',
    },
  ],
  4: [],
};

const BIN_LOCATION_MOCKS = [
  { value: 'Receiving', label: 'Receiving' },
  { value: 'Bin 1', label: 'Bin 1' },
  { value: 'Bin 2', label: 'Bin 2' },
];

export {
  LOCATION_MOCKS,
  USERNAMES_MOCKS,
  STOCK_LIST_ITEMS_MOCKS,
  SUBSTITUTIONS_MOCKS,
  BIN_LOCATION_MOCKS,
};
