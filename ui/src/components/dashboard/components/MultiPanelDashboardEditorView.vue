<template>
    <MultiPanelGenericEditorView
        :editorElements="DASHBOARD_EDITOR_ELEMENTS"
        :defaultActiveTabs="DEFAULT_ACTIVE_TABS"
        :saveKey="`ks-dashboard-${dashboardStore.dashboard?.id}`"
    >
        <template #actions>
            <DashboardEditorButtons @save="onSave" />
        </template>
    </MultiPanelGenericEditorView>
</template>

<script lang="ts" setup>
    import {DASHBOARD_EDITOR_ELEMENTS, DEFAULT_ACTIVE_TABS} from "../composables/useDashboardPanels";
    import {useDashboardStore} from "../../../stores/dashboard";
    import MultiPanelGenericEditorView from "../../MultiPanelGenericEditorView.vue";
    import DashboardEditorButtons from "./DashboardEditorButtons.vue";

    const dashboardStore = useDashboardStore();

    const emit = defineEmits<{
        (e: "save", source?: string): void;
    }>();

    function onSave(){
        emit("save", dashboardStore.dashboard?.sourceCode);
    }
</script>