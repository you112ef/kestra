import {defineStore} from "pinia";
import {apiUrl} from "override/utils/route";
import {YamlUtils} from "@kestra-io/ui-libs";
import axios from "axios";

interface PluginComponent {
    icon?: string;
    cls: string;
    description?: string;
    properties?: Record<string, any>;
}

interface Plugin {
    tasks: PluginComponent[];
    triggers: PluginComponent[];
    conditions: PluginComponent[];
    controllers: PluginComponent[];
    storages: PluginComponent[];
    taskRunners: PluginComponent[];
    charts: PluginComponent[];
    dataFilters: PluginComponent[];
    aliases: PluginComponent[];
    logExporters: PluginComponent[];
}

interface State {
    plugin: PluginComponent | undefined;
    versions: string[] | undefined;
    pluginAllProps: any | undefined;
    plugins: Plugin[] | undefined;
    pluginSingleList: PluginComponent[] | undefined;
    icons: Record<string, string> | undefined;
    pluginsDocumentation: Record<string, PluginComponent>;
    editorPlugin: (PluginComponent & { cls: string }) | undefined;
    inputSchema: any | undefined;
    inputsType: any | undefined;
}

interface LoadOptions {
    cls: string;
    version?: string;
    all?: boolean;
    commit?: boolean;
}

export const usePluginsStore = defineStore("plugins", {
    state: (): State => ({
        plugin: undefined,
        versions: undefined,
        pluginAllProps: undefined,
        plugins: undefined,
        pluginSingleList: undefined,
        icons: undefined,
        pluginsDocumentation: {},
        editorPlugin: undefined,
        inputSchema: undefined,
        inputsType: undefined
    }),

    getters: {
        getPluginSingleList: (state): PluginComponent[] | undefined => state.pluginSingleList,
        getPluginsDocumentation: (state): Record<string, PluginComponent> => state.pluginsDocumentation,
        getIcons: (state): Record<string, string> | undefined => state.icons
    },

    actions: {
        async list() {
            const response = await axios.get<Plugin[]>(`${apiUrl(this)}/plugins`);
            this.plugins = response.data;
            this.pluginSingleList = response.data
                .map(plugin => plugin.tasks
                    .concat(plugin.triggers, plugin.conditions, plugin.controllers,
                           plugin.storages, plugin.taskRunners, plugin.charts,
                           plugin.dataFilters, plugin.aliases, plugin.logExporters))
                .flat();
            return response.data;
        },

        async listWithSubgroup(options: Record<string, any>) {
            const response = await axios.get<Plugin[]>(`${apiUrl(this)}/plugins/groups/subgroups`, {
                params: options
            });
            this.plugins = response.data;
            this.pluginSingleList = response.data
                .map(plugin => plugin.tasks
                    .concat(plugin.triggers, plugin.conditions, plugin.controllers,
                           plugin.storages, plugin.taskRunners, plugin.charts,
                           plugin.dataFilters, plugin.aliases, plugin.logExporters))
                .flat();
            return response.data;
        },

        async load(options: LoadOptions) {
            if (options.cls === undefined) {
                throw new Error("missing required cls");
            }

            const id = options.version ? `${options.cls}/${options.version}` : options.cls;
            const cachedPluginDoc = this.pluginsDocumentation[id];
            if (!options.all && cachedPluginDoc) {
                this.plugin = cachedPluginDoc;
                return cachedPluginDoc;
            }

            const url = options.version ?
                `${apiUrl(this)}/plugins/${options.cls}/versions/${options.version}` :
                `${apiUrl(this)}/plugins/${options.cls}`;

            const response = await axios.get<PluginComponent>(url, {params: options});

            if (options.commit !== false) {
                if (options.all === true) {
                    this.pluginAllProps = response.data;
                } else {
                    this.plugin = response.data;
                }
            }

            if (!options.all) {
                this.pluginsDocumentation = {
                    ...this.pluginsDocumentation,
                    [id]: response.data
                };
            }

            return response.data;
        },

        // ... existing code for other actions, converted to async/await ...

        async updateDocumentation(options: { task?: string; event: { model: { getValue: () => string }, position: any } }) {
            const taskType = options.task !== undefined ? options.task : YamlUtils.getTaskType(
                options.event.model.getValue(),
                options.event.position,
                this.getPluginSingleList
            );

            if (taskType) {
                const plugin = await this.load({cls: taskType});
                this.editorPlugin = {cls: taskType, ...plugin};
            } else {
                this.editorPlugin = undefined;
            }
        }
    }
});
