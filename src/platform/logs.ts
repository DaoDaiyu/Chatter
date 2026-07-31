// In-memory Logs store for the Capacitor WebView build. Replaces the Electron
// filesystem-backed conversation logger that core.init used to receive.
//
// This keeps a short per-conversation backlog for the current session so
// reopening a conversation shows recent context, but does NOT persist across
// app restarts and does not implement historical log browsing or zip export
// (canZip = false). Persisting logs on-device would want IndexedDB or
// @capacitor/filesystem — a follow-up; see README "Known gaps".
import { Conversation, Logs } from '../chat/interfaces';

const MAX_BACKLOG = 100;

export class MemoryLogs implements Logs {
  readonly canZip = false;
  private backlog = new Map<string, Conversation.Message[]>();

  logMessage(
    conversation: Conversation,
    message: Conversation.Message
  ): void {
    let list = this.backlog.get(conversation.key);
    if (list === undefined) {
      list = [];
      this.backlog.set(conversation.key, list);
    }
    list.push(message);
    if (list.length > MAX_BACKLOG) list.splice(0, list.length - MAX_BACKLOG);
  }

  async getBacklog(
    conversation: Conversation
  ): Promise<ReadonlyArray<Conversation.Message>> {
    return this.backlog.get(conversation.key)?.slice() ?? [];
  }

  async getConversations(
    _character: string
  ): Promise<ReadonlyArray<Logs.Conversation>> {
    return [];
  }

  async getLogs(
    _character: string,
    _key: string,
    _date: Date
  ): Promise<ReadonlyArray<Conversation.Message>> {
    return [];
  }

  async getLogDates(
    _character: string,
    _key: string
  ): Promise<ReadonlyArray<Date>> {
    return [];
  }

  async getAvailableCharacters(): Promise<ReadonlyArray<string>> {
    return [];
  }
}
