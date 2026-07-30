import { defineConfig } from 'vite';
import vue2 from '@vitejs/plugin-vue2';
import path from 'path';

// Horizon's Vue 2 SFCs + TS core, bundled for a Capacitor WebView instead of
// Electron's renderer process. bbcode/, chat/, fchat/, learn/ are used
// as-is from upstream; only electron/-coupled files were replaced — see
// src/platform/ and README.md.
export default defineConfig({
  plugins: [vue2()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
      // A few upstream files still import these directly (see
      // src/platform/node-shims/*.ts for exactly which files and why).
      // Without these aliases the build fails to resolve them entirely —
      // browsers/WebViews have no 'fs', 'path', or Electron's IPC.
      'fs': path.resolve(__dirname, 'src/platform/node-shims/fs.ts'),
      'path': path.resolve(__dirname, 'src/platform/node-shims/path.ts'),
      // Node's legacy `url` (parse/format) — used by the image-preview URL
      // rewriters; Vite's default builtin stub would throw at runtime.
      'url': path.resolve(__dirname, 'src/platform/node-shims/url.ts'),
      '@electron/remote': path.resolve(__dirname, 'src/platform/node-shims/electron-remote.ts'),
      'adm-zip': path.resolve(__dirname, 'src/platform/node-shims/adm-zip.ts'),
      // request-promise is Node-only (wraps the deprecated `request`); the F-List
      // website session in src/site/site-session.ts is the sole user. Redirected
      // to an axios-backed stand-in — see that shim's header for the runtime caveat.
      'request-promise': path.resolve(__dirname, 'src/platform/node-shims/request-promise.ts')
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        // A few upstream .vue <style> blocks still use webpack's `~pkg` prefix
        // to reach node_modules (e.g. `@import '~bootstrap/scss/functions'`).
        // dart-sass doesn't understand `~`; this legacy importer strips it and
        // resolves from node_modules so those blocks compile unchanged.
        importer(url: string) {
          if (url.startsWith('~')) {
            return { file: path.resolve(__dirname, 'node_modules', url.slice(1)) };
          }
          return null;
        }
      }
    }
  },
  build: {
    outDir: 'dist',
    emptyOutDir: true
  }
});
