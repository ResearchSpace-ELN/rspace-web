import { parseString } from "./parsers";
import Result from "./result";

/*
 * Some general purpose type definitions.
 */

export type emptyObject = Record<string, never>;

/*
 * For adding optional type definitions to invocations of `useState`
 * Usage:
 *  const [count, setCount]: UseState<number> = useState(0);
 */
export type UseStateSetter<T> = React.Dispatch<React.SetStateAction<T>>;
export type UseState<T> = [T, UseStateSetter<T>];

/*
 * Geometry
 */
export interface Point {
  x: number;
  y: number;
}

/*
 * For sorting data
 */
export type Order = "asc" | "desc";

export function parseOrder(str: string): Result<Order> {
  return Result.first(
    parseString("asc", str) as Result<Order>,
    parseString("desc", str) as Result<Order>
  );
}

/*
 * The return type of Promise.allSettled
 */
export type AllSettled<A> = Array<
  | {
      status: "fulfilled";
      value: A;
    }
  | {
      status: "rejected";
      reason: Error;
    }
>;

/*
 * For managing the state of a two panel layout that on mobile requires the
 * user to toggle between the two panels.
 */
export type Panel = "left" | "right";

/*
 * This is a string in the format as described by ISO 8601.
 */
export type IsoTimestamp = string;

/*
 * This is a string in the format of a URL.
 */
export type URL = string;

/*
 * This is a URL whose protocol is "blob".
 */
export type BlobUrl = URL;

/**
 * The type of the `_links` property in the JSON API response, which is part of
 * implementing Hypermedia as the Engine of Application State (HATEOAS).
 */
export type _LINK = {
  link: URL;
  rel: string;
};
