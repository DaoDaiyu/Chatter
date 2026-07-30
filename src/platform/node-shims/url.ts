// Browser stand-in for Node's legacy `url` module. Vite otherwise replaces the
// `url` builtin with a stub whose named exports throw, which would crash the
// image-preview URL rewriters in chat/preview/image-dom-mutator.ts at runtime.
//
// Only the legacy `parse(str, parseQueryString)` / `format(obj)` pair is used,
// and only the `.query` (object) + `.search` (nullable) fields, so this is a
// focused reimplementation on top of the WHATWG URL — not the whole module.
import qs from 'qs';

export interface LegacyUrl {
  protocol: string | null;
  host: string | null;
  hostname: string | null;
  port: string | null;
  pathname: string | null;
  search: string | null;
  query: string | Record<string, any> | null;
  hash: string | null;
  href: string;
}

export function parse(urlStr: string, parseQueryString: boolean = false): LegacyUrl {
  let u: URL;
  try {
    u = new URL(urlStr);
  } catch {
    // Relative or malformed: return a best-effort shell so callers can bail.
    return {
      protocol: null, host: null, hostname: null, port: null,
      pathname: urlStr, search: null, query: parseQueryString ? {} : null,
      hash: null, href: urlStr
    };
  }

  const searchStr = u.search === '' ? null : u.search.replace(/^\?/, '');

  return {
    protocol: u.protocol,
    host: u.host,
    hostname: u.hostname,
    port: u.port,
    pathname: u.pathname,
    search: searchStr,
    query: parseQueryString
      ? (searchStr ? qs.parse(searchStr) : {})
      : searchStr,
    hash: u.hash === '' ? null : u.hash,
    href: u.href
  };
}

export function format(urlObj: LegacyUrl): string {
  const protocol = urlObj.protocol ?? '';
  const host = urlObj.host ?? urlObj.hostname ?? '';
  const pathname = urlObj.pathname ?? '';

  // Mirror Node: if `search` was cleared but `query` (object) is present, the
  // query string is rebuilt from `query`.
  let search = '';
  if (urlObj.search !== null && urlObj.search !== undefined && urlObj.search !== '') {
    search = urlObj.search.startsWith('?') ? urlObj.search : `?${urlObj.search}`;
  } else if (urlObj.query !== null && urlObj.query !== undefined && typeof urlObj.query === 'object') {
    const q = qs.stringify(urlObj.query);
    search = q ? `?${q}` : '';
  }

  const hash = urlObj.hash ?? '';
  const sep = protocol ? '//' : '';
  return `${protocol}${sep}${host}${pathname}${search}${hash}`;
}

export default { parse, format };
