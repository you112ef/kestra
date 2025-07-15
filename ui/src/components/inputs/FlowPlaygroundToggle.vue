<template>
    <el-switch v-model="store.state.flow.playgroundMode" active-text="Playground" class="toggle" />
</template>

<script setup lang="ts">
    import {useStore} from "vuex";

    const store = useStore();
</script>

<style lang="scss" scoped>
.toggle{
    margin-right: 1rem;
}
</style>
