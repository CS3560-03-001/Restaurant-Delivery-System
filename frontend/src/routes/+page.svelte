<script lang="ts">
  import { goto } from '$app/navigation';
  import { onMount } from 'svelte';

  onMount(() => {
    void goto('/order');
  });
</script>

<p class="muted">Opening the order flow.</p>

