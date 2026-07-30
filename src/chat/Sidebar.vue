<template>
  <div class="sidebar-wrapper" :class="{ open: expanded }">
    <div :class="'sidebar sidebar-' + (right ? 'right' : 'left')">
      <button
        @click="expanded = !expanded"
        class="btn btn-secondary btn-xs expander"
        :aria-label="label"
      >
        <span :class="'fa fa-fw fa-rotate-270 ' + icon" v-if="right"></span>
        <span
          class="fa"
          :class="{ 'fa-chevron-down': !expanded, 'fa-chevron-up': expanded }"
        ></span>
        <span :class="'fa fa-fw fa-rotate-90 ' + icon" v-if="!right"></span>
      </button>
      <div class="body" :style="{ width: bodyWidth + 'px' }" ref="body">
        <slot></slot>
        <div
          class="resize-handle"
          :class="right ? 'resize-handle-left' : 'resize-handle-right'"
          @mousedown="startResize"
          @touchstart.passive="startResizeTouch"
        ></div>
      </div>
    </div>
    <div class="modal-backdrop show" @click="expanded = false"></div>
  </div>
</template>

<script lang="ts">
  import Vue from 'vue';

  export default Vue.extend({
    props: {
      right: { type: Boolean, default: false },
      label: { type: String, required: true },
      icon: { type: String, required: true },
      open: { type: Boolean, default: false }
    },
    data() {
      return {
        expanded: this.open,
        bodyWidth: 0,
        bodyElement: null as HTMLElement | null,
        isResizing: false,
        startX: 0,
        startWidth: 0
      };
    },
    watch: {
      open(): void {
        this.expanded = this.open;
      }
    },
    mounted(): void {
      this.bodyElement = this.$refs.body as HTMLElement;
      this.bodyWidth = this.bodyElement.offsetWidth;
    },
    methods: {
      startResize(e: MouseEvent): void {
        this.isResizing = true;
        this.startX = e.clientX;
        this.startWidth = this.bodyWidth;
        document.addEventListener('mousemove', this.onResize);
        document.addEventListener('mouseup', this.stopResize);
        e.preventDefault();
      },
      onResize(e: MouseEvent): void {
        if (!this.isResizing) return;
        this.applyResizeDelta(e.clientX);
      },
      stopResize(): void {
        this.isResizing = false;
        document.removeEventListener('mousemove', this.onResize);
        document.removeEventListener('mouseup', this.stopResize);
        if (this.bodyElement) this.bodyWidth = this.bodyElement.offsetWidth;
      },
      // Touch equivalents — the drag-to-resize handle only had mouse
      // listeners upstream, which don't fire on Android at all.
      startResizeTouch(e: TouchEvent): void {
        this.isResizing = true;
        this.startX = e.touches[0].clientX;
        this.startWidth = this.bodyWidth;
        document.addEventListener('touchmove', this.onResizeTouch, {
          passive: true
        });
        document.addEventListener('touchend', this.stopResizeTouch);
      },
      onResizeTouch(e: TouchEvent): void {
        if (!this.isResizing) return;
        this.applyResizeDelta(e.touches[0].clientX);
      },
      stopResizeTouch(): void {
        this.isResizing = false;
        document.removeEventListener('touchmove', this.onResizeTouch);
        document.removeEventListener('touchend', this.stopResizeTouch);
        if (this.bodyElement) this.bodyWidth = this.bodyElement.offsetWidth;
      },
      applyResizeDelta(clientX: number): void {
        const delta = clientX - this.startX;
        //Our widths are clamped by CSS values. in stopResize we then set our variable value to the effectize size.
        //This is because if those values don't match, you might run into problems where it tries to resize 'outside' its boundaries, and the variable changes but the actual DOM element is clamped.
        this.bodyWidth = this.right
          ? this.startWidth - delta
          : this.startWidth + delta;
      }
    }
  });
</script>
