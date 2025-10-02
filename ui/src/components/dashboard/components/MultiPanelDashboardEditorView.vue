<template>
    <div v-bind="$attrs" class="main-editor">
        <div class="editor-header">
            <MultiPanelEditorTabs :tabs="DASHBOARD_EDITOR_ELEMENTS" @update:tabs="setTabValue" :openTabs="openTabs" />
            <DashboardEditorButtons
                @save="onSave"
            />
        </div>
        <div class="editor-wrapper">
            <MultiPanelTabs v-model="panels" />
        </div>
    </div>
</template>

<script lang="ts" setup>
    import {computed} from "vue";
    import {useStorage} from "@vueuse/core";
    import MultiPanelEditorTabs from "../../MultiPanelEditorTabs.vue";

    import MultiPanelTabs, {Panel, Tab} from "../../MultiPanelTabs.vue";
    import DashboardEditorButtons from "./DashboardEditorButtons.vue";
    import {DASHBOARD_EDITOR_ELEMENTS, DEFAULT_ACTIVE_TABS} from "../composables/useDashboardPanels";
    import {useDashboardStore} from "../../../stores/dashboard";

    const dashboardStore = useDashboardStore();


    const defaultPanelSize = computed(() => panels.value.reduce((acc, panel) => acc + panel.size, 0) / panels.value.length);

    function focusTab(tabValue: string){
        for(const panel of panels.value){
            const t = panel.tabs.find(e => e.value === tabValue);
            if(t) panel.activeTab = t;
        }
    }

    function getPanelFromValue(value: string): Panel | undefined {
        for (const element of DASHBOARD_EDITOR_ELEMENTS) {
            const deserializedTab = element.deserialize(value);
            if (deserializedTab) {
                return {
                    activeTab: element,
                    tabs: [element],
                    size: defaultPanelSize.value,
                };
            }
        }
    };

    function deserializeTabTags(tags: string[]): Tab[] {
        return tags.map(tag => {
            for (const element of DASHBOARD_EDITOR_ELEMENTS) {
                const deserializedTab = element.deserialize(tag);
                if (deserializedTab) {
                    return deserializedTab;
                }
            }
        }).filter(t => t !== undefined) as Tab[];
    }


    function setTabValue(tabValue: string){
        if(openTabs.value.includes(tabValue)){
            focusTab(tabValue);
            return;
        }
        const panel = getPanelFromValue(tabValue);
        if(panel) panels.value.push(panel);
    }

    const panels = useStorage<Panel[]>(
        `ks-dashboard-${dashboardStore.dashboard?.id}`,
        deserializeTabTags(DEFAULT_ACTIVE_TABS).map((t) => {
            return {
                activeTab: t,
                tabs: [t],
                size: 100 / DEFAULT_ACTIVE_TABS.length
            };
        }),
        undefined,
        {
            serializer: {
                write(v: Panel[]){
                    return JSON.stringify(v.map(p => ({
                        tabs: p.tabs.map(t => t.value),
                        activeTab: p.activeTab?.value,
                        size: p.size,
                    })));
                },
                read(v?: string) {
                    if(!v) return null;
                    const panels = JSON.parse(v);
                    return panels
                        .filter((p: any) => p.tabs.length)
                        .map((p: {tabs: string[], activeTab: string, size: number}):Panel => {
                            const tabs = deserializeTabTags(p.tabs);
                            const activeTab = tabs.find((t: any) => t.value === p.activeTab) ?? tabs[0];
                            return {
                                activeTab,
                                tabs,
                                size: p.size
                            };
                        });
                }
            },
        }
    );

    const openTabs = computed(() => panels.value.flatMap(p => p.tabs.map(t => t.value)));

    const emit = defineEmits<{
        (e: "save", source?: string): void;
    }>();

    function onSave(){
        emit("save", dashboardStore.dashboard?.sourceCode);
    }
</script>

<style lang="scss" scoped>
    .main-editor{
        display: grid;
        grid-template-rows: auto 1fr;
        height: 100%;

        .editor-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-bottom: 1px solid var(--ks-border-primary);

            .editor-actions {
                display: flex;
                align-items: center;
                padding-right: 0.5rem;
            }
        }

        .editor-wrapper {
            position: relative;
            height: 100%;
        }

        :deep(.tabs-wrapper) {
            background: none !important;
            border: none !important;
        }
    }
</style>