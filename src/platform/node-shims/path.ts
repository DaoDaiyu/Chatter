// Minimal POSIX-style path.join, enough for the couple of upstream call
// sites that still import Node's 'path' to build a filename before an
// fs call (which itself is stubbed to throw — see fs.ts). Not a full
// polyfill of Node's path module, just join().
export function join(...parts: string[]): string {
  return parts
    .filter(Boolean)
    .join('/')
    .replace(/\/+/g, '/');
}
export default { join };
