/*
 * @jest-environment jsdom
 */
//@flow
/* eslint-env jest */
import { stripDiacritics } from "../../StringUtils";

describe("stripDiacritics", () => {
  test("Example", () => {
    expect(stripDiacritics("Zoë")).toEqual("Zoe");
  });
});