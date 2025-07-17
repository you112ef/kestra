import {defineStore} from "pinia";
import {Execution} from "./executions";

export const usePlaygroundStore = defineStore("playground", {
    state: () => ({
        enabled: false as boolean,
        executions: [] as Execution[],
    }),
    getters: {
        latestExecution: (state) => {
            return state.executions[state.executions.length - 1];
        }
    },
    actions: {
        addExecution(execution: Execution) {
            this.executions.push(execution);
        },
        clearExecutions() {
            this.executions = [];
        }
    }
})