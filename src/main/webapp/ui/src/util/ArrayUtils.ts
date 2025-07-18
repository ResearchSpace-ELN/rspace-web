import Result from "./result";
import { Optional, lift2 } from "./optional";

/**
 * Given two arrays of equal length, create a new array by applying a function
 * to elements at each index in turn.
 */
export const zipWith = <A, B, C>(
  listA: ReadonlyArray<A>,
  listB: ReadonlyArray<B>,
  f: (a: A, b: B, index: number) => C
): Array<C> => {
  if (listA.length !== listB.length) {
    throw new Error("Both lists must have the same length");
  }
  return listA.map((a, i) => f(a, listB[i], i));
};

/**
 * Checks whether an array of values are all unique.
 */
export const allAreUnique = <T>(array: ReadonlyArray<T>): boolean =>
  array.length === [...new Set(array)].length;

/**
 * Same as Array.prototype.splice, but without mutation.
 */
export const splice = <T>(
  arr: ReadonlyArray<T>,
  start: number,
  deleteCount: number,
  ...items: ReadonlyArray<T>
): Array<T> => {
  const array = [...arr];
  array.splice(start, deleteCount, ...items);
  return array;
};

/**
 * Simply reverses a list, making it clear that the copying of the array is due
 * to the fact that Array.prototype.reverse acts on the array in place.
 */
export const reverse = <A>(list: ReadonlyArray<A>): Array<A> =>
  [...list].reverse();

/**
 * Extract the head of the passed array, if there is one.
 */
export function head<T>(array: ReadonlyArray<T>): Result<T> {
  return array.length > 0
    ? Result.Ok(array[0])
    : Result.Error([new Error("Array is empty")]);
}

/**
 * Extract the last element of an array, if there is one
 */
export function last<T>(array: ReadonlyArray<T>): Result<T> {
  return array.length > 0
    ? Result.Ok(array[array.length - 1])
    : Result.Error([new Error("Array is empty")]);
}

/**
 * Take a copy of an array but without the last element.
 * Returns Result.Error if the array is empty.
 */
export function dropLast<T>(array: ReadonlyArray<T>): Result<ReadonlyArray<T>> {
  if (array.length === 0) return Result.Error([new Error("Array is empty")]);
  const copy = [...array];
  copy.pop();
  return Result.Ok(copy);
}

/**
 * Calculates the outer product of two arrays, with the provided function
 *  applying the values
 * Usage:
 *  outerProduct([1,2,3], [4,5], (x,y) => x * y) // [[4,5],[8,10],[12,15]]
 */
export const outerProduct = <A, B, C>(
  as: ReadonlyArray<A>,
  bs: ReadonlyArray<B>,
  f: (a: A, b: B) => C
): Array<Array<C>> => as.map((a) => bs.map((b) => f(a, b)));

/**
 * The partition function takes a predicate and a list, and returns the pair of
 * lists of elements which do and do not satisfy the predicate, respectively.
 *
 * @example
 * partition((c) => 'aeiou'.split('').includes(c), "Hello World".split(''))
 *   // [['e', 'o', 'o'], ['H', 'l', 'l', ' ', 'W', 'r', 'l', 'd']]
 */
export const partition = <T>(
  predicate: (t: T) => boolean,
  list: ReadonlyArray<T>
): [Array<T>, Array<T>] =>
  list.reduce<[Array<T>, Array<T>]>(
    ([yes, no], element) => {
      if (predicate(element)) return [[...yes, element], no];
      return [yes, [...no, element]];
    },
    [[], []]
  );

/**
 * Group the elements of an array based on the result of the passed function.
 */
export const groupBy = <T, K extends string>(
  f: (t: T) => K,
  list: ReadonlyArray<T>
): { [key in K]: Array<T> } =>
  list.reduce((acc, element) => {
    const key = f(element);
    return { ...acc, [key]: [...(acc[key] || []), element] };
  }, {} as { [key in K]: Array<T> });

/**
 * Just like normal filter, but specifically just to check whether the elements
 * of the array are instances of the passed class. Useful where Flow would not
 * recognise a normal filter.
 */
export const filterClass = <T, U>(
  clazz: { new (...args: any[]): T },
  array: ReadonlyArray<U>
): Array<T> => {
  const arrayOft: Array<T> = [];
  for (const a of array) {
    if (a instanceof clazz) arrayOft.push(a);
  }
  return arrayOft;
};

/**
 * Return a new array where between each pair of elements in the passed array
 * the constant value is inserted.
 *
 * @arg a  The constant value to be inserted between the elements.
 * @arg as The source array from which elements will be copied.
 */
export const intersperse = <A>(a: A, as: ReadonlyArray<A>): Array<A> => {
  if (as.length === 0) return [];
  const output = [as[0]];
  for (let i = 1; i < as.length; i++) {
    output.push(a);
    output.push(as[i]);
  }
  return output;
};

/**
 * Keeps the elements from the first array where the element of the same index
 * in the second array is true. Neither array should contain null as that will
 * break. Useful for property-based testing where values can be randonly chosen
 * from an array by generating random array of booleans of the same length.
 */
export const takeWhere = <A>(
  as: ReadonlyArray<A>,
  where: ReadonlyArray<boolean>
): Array<A> => {
  if (as.length !== where.length) throw new Error("length must match");
  const output = [];
  for (let i = 0; i < as.length; i++) {
    if (where[i]) output.push(as[i]);
  }
  return output;
};

/**
 * Just like Array.prototype.find but returns Optional.empty instead of either
 * undefined or null
 */
export function find<T>(
  func: (t: T) => boolean,
  array: ReadonlyArray<T>
): Optional<T> {
  const found: T | undefined = array.find(func);
  if (typeof found === "undefined" || found === null) return Optional.empty();
  return Optional.present(found);
}

/**
 * The same as using the square brackets to index an array, but
 * rather than return undefined if the index is out of range this
 * function return Optional.empty
 */
export function getAt<T>(index: number, array: ReadonlyArray<T>): Optional<T> {
  if (
    !isNaN(index) &&
    Number.isInteger(index) &&
    index >= 0 &&
    index < array.length
  ) {
    return Optional.present(array[index]);
  }

  return Optional.empty();
}

/**
 * Map over the passed array, keeping only those elements which when applied to
 * the passed function are wrapped in an Optional.present. This can be useful
 * when discarding particular values from an array. If the values you're
 * looking to discard are nulls then use the more concrete version of this
 * function, `filterNull`, below.
 */
export const mapOptional = <A, B>(
  f: (a: A) => Optional<B>,
  as: ReadonlyArray<A>
): Array<B> => {
  // These classes are here solely so that filterClass can be used
  class Present<T> {
    value: T;
    constructor(value: T) {
      this.value = value;
    }
  }
  class Empty {}

  const arrayOfOptionals: ReadonlyArray<Optional<B>> = as.map(f);
  const arrayOfPresentOrEmpty: ReadonlyArray<Present<B> | Empty> =
    arrayOfOptionals.map((opt) =>
      opt.destruct(
        () => new Empty(),
        (v) => new Present(v)
      )
    );
  const arrayOfPresent: ReadonlyArray<Present<B>> = filterClass(
    Present as { new (...args: unknown[]): Present<B> },
    arrayOfPresentOrEmpty
  );
  return arrayOfPresent.map((opt) => opt.value);
};

/**
 * This is a better version of doing `as.filter(a => a !== null)`.
 *
 * Why is it better? Because flow remembers the fact that the filter has been
 * performed. Flow does not retain any information about what happens inside
 * Array.prototype.filter; the type of the method is simply
 * `<A>(A => boolean): Array<A>` with `this` having type `Array<A>`.
 *
 * As such, if you do the following, flow will complain
 * ```
 * const mixture: Array<?number> = [1, 2, null];
 * const justNums: Array<number> = mixture.filter(n => n !== null);
 * ```
 *
 * If, on the other hand, you do this flow will not complain
 * ```
 * const justNums: Array<number> = filterNull(mixture);
 * ```
 */
export const filterNull = <A>(as: ReadonlyArray<A | null>): Array<A> =>
  mapOptional((a) => Optional.fromNullable(a), as);

/**
 * Converts an array of optionals into an optional array by returning
 * Optional.present if all of elements of the input array are Optional.present.
 * Otherwise, Optional.empty is returned. This is similar to Promise.all (hence
 * the same name) where that function takes as input `Array<Promise<A>>` and
 * returns `Promise<Array<A>>`
 *
 * Usage:
 *   all([Optional.present(1), Optional.present(2)]) // Optional.present([1,2])
 *   all([Optional.present(1), Optional.empty()])    // Optional.empty()
 *   all([])                                         // Optional.present([])
 */
export const all = <A>(as: ReadonlyArray<Optional<A>>): Optional<Array<A>> => {
  if (as.length === 0) return Optional.present([]);
  const [h, ...t] = as;
  return lift2((newHead, newTail) => [newHead, ...newTail], h, all(t));
};

/**
 * Splits the array into chunks of size n. If the last chunk is smaller than n,
 * it will still be included in the result.
 */
export function chunksOf<T>(
  n: number,
  array: ReadonlyArray<T>
): Array<Array<T>> {
  if (n <= 0) throw new Error("Chunk size must be greater than zero");
  const result: Array<Array<T>> = [];
  for (let i = 0; i < array.length; i += n) {
    result.push(array.slice(i, i + n));
  }
  return result;
}
