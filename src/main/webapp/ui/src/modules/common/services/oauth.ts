import { Effect, Layer, Context, Ref } from "effect";
import { jwtDecode } from "jwt-decode";
import axios from "@/common/axios";

// JWT Token management service
class JwtTokenService extends Context.Tag("JwtTokenService")<
  JwtTokenService,
  {
    readonly getToken: Effect.Effect<string, Error>;
  }
>() {}

// Helper functions
const ID_TOKEN_KEY = "id_token";
const JWT_TOKEN_PATTERN = /^.+\..+\..+$/;

const getStoredToken = (): string | null => {
  return window.sessionStorage.getItem(ID_TOKEN_KEY);
};

const saveToken = (token: string): void => {
  window.sessionStorage.setItem(ID_TOKEN_KEY, token);
};

const secondsToExpiry = (token: string): number => {
  if (!token.match(JWT_TOKEN_PATTERN)) {
    // This is an API key
    return Infinity;
  }

  const expiresAt: number = jwtDecode<{ exp: number }>(token).exp;
  const timeNow = Math.floor(Date.now() / 1000);

  return expiresAt - timeNow;
};

const isExpired = (token: string): boolean => {
  return secondsToExpiry(token) <= 0;
};

// Implementation of the service
const makeJwtTokenService = Effect.gen(function* () {
  const tokenRef = yield* Ref.make<string | null>(null);

  const fetchToken = Effect.tryPromise({
    try: async () => {
      const response = await axios.get<{ data: string }>(
        "/userform/ajax/inventoryOauthToken",
      );
      const newToken = response.data.data;
      saveToken(newToken);
      return newToken;
    },
    catch: (error) => new Error(`Failed to fetch token: ${error}`),
  });

  const getToken: Effect.Effect<string, Error> = Effect.gen(function* () {
    const currentToken = yield* Ref.get(tokenRef);

    // Check if we have a valid token in memory
    if (currentToken && !isExpired(currentToken)) {
      return currentToken;
    }

    // Check session storage
    const savedToken = getStoredToken();
    if (savedToken && !isExpired(savedToken)) {
      yield* Ref.set(tokenRef, savedToken);
      return savedToken;
    }

    // Token expired or doesn't exist, fetch new one
    const newToken = yield* fetchToken;
    yield* Ref.set(tokenRef, newToken);
    return newToken;
  });

  return { getToken };
});

// Create the layer
const JwtTokenServiceLive = Layer.effect(JwtTokenService, makeJwtTokenService);

export { JwtTokenService, JwtTokenServiceLive };
