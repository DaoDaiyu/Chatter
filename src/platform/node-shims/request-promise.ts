// Browser/WebView-safe stand-in for the Node-only `request-promise` package.
//
// `request-promise` (and the `request` it wraps) depend on Node's `http`
// stack and never bundle for a Capacitor WebView. Only src/site/site-session.ts
// uses it, and only a small slice of the API:
//
//   request.defaults({ jar })            -> a request function with defaults
//   request.jar()                        -> a cookie jar
//   request({ method, uri, form, ... })  -> Promise<response>
//   response fields used: .statusCode, .body
//
// This reimplements exactly that slice on top of axios (already a dependency),
// so the bundle builds and the site session has a chance of working.
//
// RUNTIME CAVEAT: requests here go to https://www.f-list.net from the WebView's
// own origin, so they are cross-origin. For them to actually succeed on device,
// axios must be routed through Capacitor's native HTTP (CapacitorHttp), which
// bypasses CORS and exposes Set-Cookie. Until then the F-List *website* session
// (character notes, some profile bits) degrades — the chat itself uses the
// WebSocket in fchat/ and is unaffected. See README "Known gaps".
import axios, { AxiosRequestConfig } from 'axios';
import qs from 'qs';

export interface CookieJar {
  getCookieString(url?: string): string;
  setFromResponse(setCookie?: string | string[]): void;
}

// Minimal name=value cookie jar. Not path/domain/expiry aware — enough to carry
// a session cookie across the two requests site-session.ts makes.
function makeJar(): CookieJar {
  const cookies: Record<string, string> = {};
  return {
    getCookieString(): string {
      return Object.entries(cookies)
        .map(([k, v]) => `${k}=${v}`)
        .join('; ');
    },
    setFromResponse(setCookie?: string | string[]): void {
      if (setCookie === undefined) return;
      const list = Array.isArray(setCookie) ? setCookie : [setCookie];
      for (const c of list) {
        const first = c.split(';')[0];
        const eq = first.indexOf('=');
        if (eq > 0) cookies[first.slice(0, eq).trim()] = first.slice(eq + 1).trim();
      }
    }
  };
}

export interface Options {
  method?: string;
  uri?: string;
  url?: string;
  form?: Record<string, any>;
  qs?: Record<string, any>;
  headers?: Record<string, string>;
  jar?: CookieJar;
  resolveWithFullResponse?: boolean;
  followRedirect?: boolean;
  simple?: boolean;
  json?: boolean;
  [key: string]: any;
}
export interface OptionsWithUri extends Options {
  uri: string;
}

export interface FullResponse {
  statusCode: number;
  status: number;
  body: any;
  headers: Record<string, any>;
}

export type RequestPromise = Promise<any>;

async function run(options: Options): Promise<any> {
  const url = options.uri ?? options.url ?? '';
  const jar = options.jar;

  const headers: Record<string, string> = { ...(options.headers ?? {}) };
  if (jar !== undefined) {
    const cookieString = jar.getCookieString(url);
    if (cookieString !== '') headers['Cookie'] = cookieString;
  }

  let data: any;
  if (options.form !== undefined) {
    data = qs.stringify(options.form);
    headers['Content-Type'] = 'application/x-www-form-urlencoded';
  }

  const config: AxiosRequestConfig = {
    method: (options.method ?? 'get').toLowerCase() as any,
    url,
    params: options.qs,
    data,
    headers,
    // request's `followRedirect: false` maps to not following redirects.
    maxRedirects: options.followRedirect === false ? 0 : 5,
    // request's `simple: false` maps to not throwing on non-2xx.
    validateStatus: options.simple === false ? () => true : (s) => s >= 200 && s < 400,
    // Never let axios/browser silently follow to swallow the 302 we assert on.
    withCredentials: true
  };

  const res = await axios.request(config);

  if (jar !== undefined) {
    jar.setFromResponse(res.headers?.['set-cookie']);
  }

  if (options.resolveWithFullResponse === true) {
    const full: FullResponse = {
      statusCode: res.status,
      status: res.status,
      body: res.data,
      headers: res.headers as Record<string, any>
    };
    return full;
  }
  return res.data;
}

export interface RequestPromiseAPI {
  (options: Options): RequestPromise;
  defaults(defaults: Partial<Options>): RequestPromiseAPI;
  jar(): CookieJar;
}

function makeApi(defaults: Partial<Options>): RequestPromiseAPI {
  const api = ((options: Options) =>
    run({ ...defaults, ...options })) as RequestPromiseAPI;
  api.defaults = (more: Partial<Options>) => makeApi({ ...defaults, ...more });
  api.jar = makeJar;
  return api;
}

const request = makeApi({});

export default request;
