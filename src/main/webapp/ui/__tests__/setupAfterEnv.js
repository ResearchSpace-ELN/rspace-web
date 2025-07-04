/* eslint-env jest */
Object.defineProperty(document, "documentElement", {
  writable: true,
  configurable: true,
  value: {
    clientWidth: 1024,
    clientHeight: 768,
    scrollHeight: 768,
    scrollWidth: 1024,
    style: {},
  },
});

// Also mock getComputedStyle which might be used by MUI
window.getComputedStyle = () => ({
  getPropertyValue: () => "",
});
