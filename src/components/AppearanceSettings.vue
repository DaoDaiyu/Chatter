<template>
  <div class="appearance-settings">
    <h4>Background</h4>
    <select v-model="themeSettings.backgroundType" @change="onThemeChange">
      <option value="none">None</option>
      <option value="color">Solid color</option>
      <option value="image">Image (PNG/JPG)</option>
      <option value="video">Video</option>
    </select>

    <input
      v-if="themeSettings.backgroundType === 'color'"
      type="color"
      v-model="themeSettings.backgroundColor"
      @change="onThemeChange"
    />

    <input
      v-if="['image', 'video'].includes(themeSettings.backgroundType)"
      type="file"
      :accept="themeSettings.backgroundType === 'image' ? 'image/*' : 'video/*'"
      @change="onFilePicked"
    />

    <label>
      Dim overlay
      <input type="range" min="0" max="1" step="0.05" v-model.number="themeSettings.overlayOpacity" @input="onThemeChange" />
    </label>
    <label>
      Panel translucency
      <input type="range" min="0.3" max="1" step="0.05" v-model.number="themeSettings.panelOpacity" @input="onThemeChange" />
    </label>
    <label>
      Panel blur
      <input type="range" min="0" max="24" step="1" v-model.number="themeSettings.panelBlur" @input="onThemeChange" />
    </label>

    <h4>Text size</h4>
    <label>
      Font scale ({{ Math.round(fontScale * 100) }}%)
      <input type="range" min="0.85" max="1.6" step="0.05" v-model.number="fontScale" @input="onFontScaleChange" />
    </label>
  </div>
</template>

<script lang="ts">
import Vue from 'vue';
import { theme, ThemeSettings, DEFAULT_THEME } from '../platform/theme';
import { appearance } from '../platform/appearance';

export default Vue.extend({
  name: 'AppearanceSettings',
  data() {
    return {
      themeSettings: { ...DEFAULT_THEME } as ThemeSettings,
      fontScale: 1
    };
  },
  async created() {
    this.themeSettings = await theme.load();
    const app = await appearance.load();
    this.fontScale = app.fontScale;
  },
  methods: {
    async onThemeChange(): Promise<void> {
      await theme.save(this.themeSettings);
    },
    async onFilePicked(e: Event): Promise<void> {
      const input = e.target as HTMLInputElement;
      const file = input.files?.[0];
      if (!file) return;
      const blobUrl = URL.createObjectURL(file);
      const extension = file.name.split('.').pop() || 'bin';
      // Copies into app-private storage — see theme.importBackgroundFile's
      // comment for why (content:// permission lifetime).
      this.themeSettings.backgroundUri = await theme.importBackgroundFile(blobUrl, extension);
      URL.revokeObjectURL(blobUrl);
      await theme.save(this.themeSettings);
    },
    async onFontScaleChange(): Promise<void> {
      await appearance.setFontScale(this.fontScale);
    }
  }
});
</script>

<style scoped>
.appearance-settings {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
</style>
