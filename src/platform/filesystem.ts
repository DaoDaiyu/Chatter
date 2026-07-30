// Replaces electron/filesystem.ts's getDrafts/saveDrafts (Node fs) used by
// learn/conversation-draft-cache.ts. Uses @capacitor/filesystem, sandboxed
// to the app's private Documents directory on-device.
import { Filesystem, Directory, Encoding } from '@capacitor/filesystem';

const DRAFTS_FILE = 'drafts.json';

export async function getDrafts(): Promise<Record<string, string>> {
    try {
        const res = await Filesystem.readFile({
            path: DRAFTS_FILE,
            directory: Directory.Data,
            encoding: Encoding.UTF8
        });
        return JSON.parse(res.data as string);
    } catch {
        return {};
    }
}

export async function saveDrafts(drafts: Record<string, string>): Promise<void> {
    await Filesystem.writeFile({
        path: DRAFTS_FILE,
        directory: Directory.Data,
        encoding: Encoding.UTF8,
        data: JSON.stringify(drafts)
    });
}
