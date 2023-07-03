import { Plugin } from "../";
import Yasr from "../../";
export interface PluginConfig {
    maxLines: number;
}
export default class Response implements Plugin<PluginConfig> {
    private yasr;
    label: string;
    priority: number;
    helpReference: string;
    private config;
    private overLay;
    private cm;
    constructor(yasr: Yasr);
    canHandleResults(): boolean;
    getIcon(): HTMLDivElement;
    download(filename?: string): {
        getData: () => string;
        filename: string;
        contentType: any;
        title: string;
    };
    draw(persistentConfig: PluginConfig): void;
    private limitData;
    showLess(setValue?: boolean): void;
    showMore(): void;
    static defaults: PluginConfig;
}
