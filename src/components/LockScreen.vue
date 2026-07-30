<template>
  <div class="lock-screen" v-if="visible">
    <div class="lock-box">
      <p>Enter PIN</p>
      <input
        type="password"
        inputmode="numeric"
        pattern="[0-9]*"
        maxlength="8"
        v-model="entered"
        @keyup.enter="tryUnlock"
        autofocus
      />
      <button @click="tryUnlock">Unlock</button>
      <p v-if="error" class="error">Incorrect PIN</p>
    </div>
  </div>
</template>

<script lang="ts">
import Vue from 'vue';
import { lock } from '../platform/lock';

// Mount this once at the app root (see main.ts). It self-hides when no PIN
// is set, and re-shows itself via App.addListener('appStateChange', ...)
// when the app returns from background — wire that listener in main.ts
// alongside the keep-alive setup, both touch the same lifecycle hook.
export default Vue.extend({
  name: 'LockScreen',
  data() {
    return {
      visible: false,
      entered: '',
      error: false
    };
  },
  async created() {
    this.visible = await lock.isEnabled();
  },
  methods: {
    async show(): Promise<void> {
      if (await lock.isEnabled()) {
        this.visible = true;
        this.entered = '';
        this.error = false;
      }
    },
    async tryUnlock(): Promise<void> {
      const ok = await lock.verify(this.entered);
      if (ok) {
        this.visible = false;
        this.error = false;
      } else {
        this.error = true;
        this.entered = '';
      }
    }
  }
});
</script>

<style scoped>
.lock-screen {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: rgba(0, 0, 0, 0.95);
  display: flex;
  align-items: center;
  justify-content: center;
}
.lock-box {
  text-align: center;
  color: white;
}
.lock-box input {
  font-size: 1.5rem;
  letter-spacing: 0.5rem;
  text-align: center;
  width: 8rem;
}
.error {
  color: #ff6b6b;
}
</style>
