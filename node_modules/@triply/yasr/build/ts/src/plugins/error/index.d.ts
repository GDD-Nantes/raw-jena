import { Plugin } from "../";
import Yasr from "../../";
export default class Error implements Plugin<never> {
    private yasr;
    constructor(yasr: Yasr);
    canHandleResults(): boolean;
    private getTryBtn;
    private getCorsMessage;
    draw(): Promise<void>;
    getIcon(): HTMLElement;
    priority: number;
    hideFromSelection: boolean;
}
