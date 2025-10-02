<template>
    <div v-bind="$attrs" class="main-editor">
        <div class="editor-header">
            <MultiPanelEditorTabs :tabs="editorElements" @update:tabs="setTabValue" :openTabs="openTabs" />
            <slot name="actions" />
        </div>
        <div class="editor-wrapper">
            <MultiPanelTabs v-model="panels" @remove-tab="onRemoveTab" />
        </div>
    </div>
</template>

<script lang="ts" setup>
    import {computed} from "vue";
    import {useStorage} from "@vueuse/core";
    import MultiPanelEditorTabs from "./MultiPanelEditorTabs.vue";
    import MultiPanelTabs, {Panel} from "./MultiPanelTabs.vue";
    import {DeserializableEditorElement, Tab} from "../utils/multiPanelTypes";

    const props = defineProps<{
        editorElements: DeserializableEditorElement[];
        defaultActiveTabs: string[];
        saveKey: string;
    }>();


    const defaultPanelSize = computed(() => panels.value.reduce((acc, panel) => acc + panel.size, 0) / panels.value.length);

    function focusTab(tabValue: string){
        for(const panel of panels.value){
            const t = panel.tabs.find(e => e.value === tabValue);
            if(t) panel.activeTab = t;
        }
    }

    function getPanelFromValue(value: string): Panel | undefined {
        for (const element of props.editorElements) {
            const deserializedTab = element.deserialize(value, false);
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
            for (const element of props.editorElements) {
                const deserializedTab = element.deserialize(tag, true);
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
        props.saveKey,
        deserializeTabTags(props.defaultActiveTabs).map((t) => {
            return {
                activeTab: t,
                tabs: [t],
                size: 100 / props.defaultActiveTabs.length
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

    function onRemoveTab(tabValue: string) {
        const panel = panels.value.find(p => p.tabs.some(t => t.value === tabValue))
        if (panel) {
            panel.tabs = panel.tabs.filter(t => t.value !== tabValue)
            if (panel.activeTab.value === tabValue) {
                panel.activeTab = panel.tabs[0]
            }
        }
    }

    defineExpose({
        panels,
        openTabs,
        focusTab,
    });
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