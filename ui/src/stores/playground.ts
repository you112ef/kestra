import {defineStore} from "pinia";
import {Execution} from "./executions";

export const usePlaygroundStore = defineStore("playground", {
    state: () => ({
        enabled: false as boolean,
        executions: [] as Execution[],
    }),
})