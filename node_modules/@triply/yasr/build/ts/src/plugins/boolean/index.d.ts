import Yasr from "../../";
import { Plugin } from "../";
export interface PluginConfig {
}
export default class Boolean implements Plugin<PluginConfig> {
    private yasr;
    priority: number;
    hideFromSelection: boolean;
    constructor(yasr: Yasr);
    draw(): void;
    canHandleResults(): boolean;
    getIcon(): HTMLElement;
}
