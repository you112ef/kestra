<template>
    <Editor
        v-if="dashboardStore.dashboard"
        v-model="dashboardStore.dashboard.sourceCode"
        schemaType="dashboard"
        lang="yaml"
        :navbar="false"
    />
</template>

<script lang="ts" setup>
    import {useDashboardStore} from "../../../stores/dashboard";
    import Editor from "../../inputs/Editor.vue";

    const dashboardStore = useDashboardStore();
</script>
