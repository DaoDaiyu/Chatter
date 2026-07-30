import l from '../chat/localize';

// Upstream used Electron's synchronous remote.dialog.showMessageBoxSync for
// this. window.confirm() is the direct browser/WebView equivalent — also
// synchronous, also returns a boolean — so this is a real working
// replacement, not a stub. (Native-styled Android dialogs would need
// @capacitor/dialog instead, which is async — every call site of
// confirmDialog() upstream expects a synchronous boolean return, so
// swapping to that would mean updating every caller, not just this file.)
export class Dialog {
  static confirmDialog(message: string, _defaultNo: boolean = false): boolean {
    return window.confirm(`${l('title')}\n\n${message}`);
  }
}
