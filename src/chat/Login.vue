<template>
  <div class="login-wrapper">
    <div class="card bg-light login-card">
      <h3 class="card-header">{{ l('title') }}</h3>
      <div class="card-body">
        <div class="alert alert-danger" v-show="error">{{ error }}</div>
        <form @submit.prevent="submit">
          <div class="form-group mb-3">
            <label class="form-label" for="login-account">
              {{ l('login.account') }}
            </label>
            <input
              id="login-account"
              class="form-control"
              v-model="account"
              autocomplete="username"
              :disabled="working"
              required
            />
          </div>
          <div class="form-group mb-3">
            <label class="form-label" for="login-password">
              {{ l('login.password') }}
            </label>
            <input
              id="login-password"
              type="password"
              class="form-control"
              v-model="password"
              autocomplete="current-password"
              :disabled="working"
              required
            />
          </div>
          <button
            type="submit"
            class="btn btn-primary w-100"
            :disabled="working || !account || !password"
          >
            <span
              v-if="working"
              class="spinner-border spinner-border-sm me-2"
            ></span>
            {{ l(working ? 'login.working' : 'login.submit') }}
          </button>
        </form>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
  import Vue from 'vue';
  import Axios from 'axios';
  import qs from 'qs';
  import l from './localize';
  import log from '../platform/log'; //tslint:disable-line:match-default-export-name
  import { SimpleCharacter } from '../interfaces';

  // Login step the scaffold was missing: authenticate against F-List and hand
  // the account's character list to Chat.vue (which handles character selection
  // and the actual chat WebSocket connect). The HTTP calls here are cross-origin
  // from the WebView; CapacitorHttp (enabled in capacitor.config.ts) routes them
  // natively so they aren't blocked by CORS.
  export default Vue.extend({
    data() {
      return { account: '', password: '', error: '', working: false };
    },
    methods: {
      l,
      async submit(): Promise<void> {
        if (!this.account || !this.password) return;
        this.working = true;
        this.error = '';
        try {
          const res = await Axios.post(
            'https://www.f-list.net/json/getApiTicket.php',
            qs.stringify({
              account: this.account,
              password: this.password,
              new_character_list: true,
              no_friends: true,
              no_bookmarks: true
            }),
            // Explicit form encoding so CapacitorHttp's native layer serializes
            // the body correctly (it keys off Content-Type).
            { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
          );
          const data = res.data as {
            error?: string;
            ticket?: string;
            characters?: unknown;
            default_character?: number | string;
          };

          if (data.error !== undefined && data.error !== '') {
            this.error = data.error;
            return;
          }
          if (data.ticket === undefined) {
            this.error = l('login.error');
            return;
          }

          const characters = this.parseCharacters(data.characters);
          if (characters.length === 0) {
            this.error = 'No characters found on this account.';
            return;
          }
          const defaultCharacter = this.resolveDefault(
            characters,
            data.default_character
          );

          this.$emit('login', {
            account: this.account,
            password: this.password,
            characters,
            defaultCharacter
          });
        } catch (e) {
          log.error('login.error', e);
          this.error = (e as Error)?.message ?? l('login.error');
        } finally {
          this.working = false;
        }
      },

      // F-List has returned this field in several shapes over time; accept the
      // ones that carry ids (Chat.vue keys characters by numeric id).
      parseCharacters(raw: unknown): SimpleCharacter[] {
        const out: SimpleCharacter[] = [];
        if (Array.isArray(raw)) {
          let synthetic = 1;
          for (const c of raw) {
            if (typeof c === 'string') {
              // getApiTicket without new_character_list returns bare names.
              // Connecting only needs the name; synthesize an id for the UI.
              out.push({ id: synthetic++, name: c, deleted: false });
            } else if (c && typeof c === 'object' && 'name' in c) {
              out.push({
                id: 'id' in c ? Number((c as any).id) : synthetic++,
                name: String((c as any).name),
                deleted: false
              });
            }
          }
        } else if (raw && typeof raw === 'object') {
          // { "CharName": id }
          for (const [name, id] of Object.entries(raw as Record<string, any>)) {
            out.push({ id: Number(id), name, deleted: false });
          }
        }
        return out.filter(c => !isNaN(c.id) && c.name.length > 0);
      },

      resolveDefault(
        characters: SimpleCharacter[],
        def: number | string | undefined
      ): number {
        if (typeof def === 'number') return def;
        if (typeof def === 'string') {
          const match = characters.find(c => c.name === def);
          if (match) return match.id;
        }
        return characters[0].id;
      }
    }
  });
</script>

<style lang="scss" scoped>
  .login-wrapper {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 100%;
    padding: 16px;
  }
  .login-card {
    width: 420px;
    max-width: 100%;
  }
</style>
