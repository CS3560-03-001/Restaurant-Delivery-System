<script lang="ts">
  import { goto } from '$app/navigation';
  import { onMount } from 'svelte';

  onMount(() => {
    void goto('/account');
  });
</script>

<p class="muted">Opening the account flow.</p>

