// F-Chat's protocol only supports link embeds, not raw file upload — so
// "attachments" here means: pick a file on-device, upload it somewhere
// public, get back a URL, insert that URL as BBCode into the message.
// Horizon's BBCode parser (bbcode/parser.ts) already turns image/video
// URLs into inline previews, so this is enough to get attachment-like
// behavior without touching the wire protocol.
//
// Uses catbox.moe's anonymous upload API (no account, no key needed) as a
// default. Swap `UPLOAD_ENDPOINT` for your own host if you'd rather not
// depend on a third party — the response contract (plain-text URL back) is
// what matters here, not catbox specifically.
const UPLOAD_ENDPOINT = 'https://catbox.moe/user/api.php';

export interface UploadResult {
  url: string;
  bbcode: string; // ready to insert into the message box
}

export async function uploadAttachment(file: File): Promise<UploadResult> {
  const form = new FormData();
  form.append('reqtype', 'fileupload');
  form.append('fileToUpload', file);

  const res = await fetch(UPLOAD_ENDPOINT, { method: 'POST', body: form });
  if (!res.ok) {
    throw new Error(`Upload failed: ${res.status} ${res.statusText}`);
  }
  const url = (await res.text()).trim();
  const isImageOrVideo = /\.(png|jpe?g|gif|webp|mp4|webm)$/i.test(url);
  return {
    url,
    // [url] wraps it as a clickable link; Horizon's parser auto-previews
    // direct image/video URLs, so plain url text is enough for those.
    bbcode: isImageOrVideo ? url : `[url=${url}]${file.name}[/url]`
  };
}
