<template>
  <span>
    <input
      ref="fileInput"
      type="file"
      style="display: none"
      @change="onFileSelected"
    />
    <button class="btn btn-secondary btn-xs" :disabled="uploading" @click="pickFile">
      <span class="fa fa-paperclip"></span>
    </button>
  </span>
</template>

<script lang="ts">
import Vue from 'vue';
import { uploadAttachment } from '../platform/attachments';

// NOT YET WIRED into ConversationView.vue's message toolbar — that file is
// large (48K) and this was written standalone rather than blind-editing it
// untested. To integrate: drop <attachment-button @inserted="text => ..." />
// next to the send button, and on 'inserted' splice the returned BBCode
// into the same v-model the BBCode editor (bbcode/Editor.vue) binds to.
export default Vue.extend({
  name: 'AttachmentButton',
  data() {
    return { uploading: false };
  },
  methods: {
    pickFile(): void {
      (this.$refs.fileInput as HTMLInputElement).click();
    },
    async onFileSelected(e: Event): Promise<void> {
      const input = e.target as HTMLInputElement;
      const file = input.files?.[0];
      if (!file) return;
      this.uploading = true;
      try {
        const result = await uploadAttachment(file);
        this.$emit('inserted', result.bbcode);
      } catch (err) {
        this.$emit('error', err);
      } finally {
        this.uploading = false;
        input.value = '';
      }
    }
  }
});
</script>
