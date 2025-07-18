import {computed, ref} from "vue";
import {defineStore} from "pinia";
import {Execution} from "./executions";

export const usePlaygroundStore = defineStore("playground", () => {

    const enabled = ref<boolean>(false)
    const executions = ref<Execution[]>([])


    function addExecution(execution: Execution) {
            executions.value.push(execution);
    }

    function clearExecutions() {
            executions.value = [];
    }

    return {
        enabled,
        executions,
        latestExecution: computed(() => executions.value[executions.value.length - 1]),
        addExecution,
        clearExecutions
    }
})