const useSessionStorage = (key, defaultValue) => {
  const val = JSON.parse(sessionStorage.getItem(key)) ?? defaultValue;
  const setValue = (newValue) => sessionStorage.setItem(key, JSON.stringify(newValue));
  const clearValue = () => sessionStorage.removeItem(key);

  return [val, setValue, clearValue];
};

export default useSessionStorage;
