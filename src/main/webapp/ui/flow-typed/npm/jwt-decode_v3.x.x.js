// flow-typed signature: 2ac78348f96cf23eb97005f20616315a
// flow-typed version: 77a4121732/jwt-decode_v3.x.x/flow_>=v0.83.x

declare module 'jwt-decode' {
  declare export class InvalidTokenError extends Error {}

  declare type JwtDecodeOptions = {|
    header?: boolean,
  |};

  declare type JwtHeader = {|
    type?: string,
    alg?: string,
  |};

  declare type JwtPayload = {|
    iss?: string,
    sub?: string,
    aud?: Array<string> | string,
    exp?: number,
    nbf?: number,
    iat?: number,
    jti?: string,
  |};

  declare export default function jwtDecode<T>(
    token: string,
    options?: JwtDecodeOptions,
  ): T;
}
