<template>
  <div class="theme-background" aria-hidden="true">
    <video
      v-if="settings.backgroundType === 'video' && settings.backgroundUri"
      :src="settings.backgroundUri"
      autoplay
      muted
      loop
      playsinline
      class="theme-background-media"
    ></video>
    <img
      v-else-if="settings.backgroundType === 'image' && settings.backgroundUri"
      :src="settings.backgroundUri"
      class="theme-background-media"
    />
    <div
      v-else-if="settings.backgroundType === 'color'"
      class="theme-background-media"
      :style="{ backgroundColor: settings.backgroundColor }"
    ></div>
    <div class="theme-background-overlay"></div>
  </div>
</template>

<script lang="ts">
import Vue from 'vue';
import { theme, ThemeSettings, DEFAULT_THEME } from '../platform/theme';

// Sits behind #app (z-index -1, position: fixed) so every existing Horizon
// screen renders on top of it unmodified. Readability comes from two
// layers: theme-background-overlay (a flat dim, controlled by
// --horizon-overlay-opacity) and the translucent/blurred chat panels
// defined in scss/mobile/_theme-panels.scss, not from anything in this
// component itself.
export default Vue.extend({
  name: 'ThemeBackground',
  data() {
    return {
      settings: DEFAULT_THEME as ThemeSettings
    };
  },
  async created() {
    this.settings = await theme.load();
  }
});
</script>

<style scoped>
.theme-background {
  position: fixed;
  inset: 0;
  z-index: -1;
  overflow: hidden;
}
.theme-background-media {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.theme-background-overlay {
  position: absolute;
  inset: 0;
  background: black;
  opacity: var(--horizon-overlay-opacity, 0.35);
}
</style>
