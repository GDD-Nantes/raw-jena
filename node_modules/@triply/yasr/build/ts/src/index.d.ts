/// <reference types="node" />
import { EventEmitter } from "events";
import { Plugin } from "./plugins";
import { Storage as YStorage } from "@triply/yasgui-utils";
import Parser from "./parsers";
export { default as Parser } from "./parsers";
import { addScript, addCss } from "./helpers";
export interface PersistentConfig {
    selectedPlugin?: string;
    pluginsConfig?: {
        [pluginName: string]: any;
    };
}
export interface Yasr {
    on(event: "change", listener: (instance: Yasr) => void): this;
    emit(event: "change", instance: Yasr): boolean;
    on(event: "draw", listener: (instance: Yasr, plugin: Plugin<any>) => void): this;
    emit(event: "draw", instance: Yasr, plugin: Plugin<any>): boolean;
    on(event: "drawn", listener: (instance: Yasr, plugin: Plugin<any>) => void): this;
    emit(event: "drawn", instance: Yasr, plugin: Plugin<any>): boolean;
    on(event: "toggle-help", listener: (instance: Yasr) => void): this;
    emit(event: "toggle-help", instance: Yasr): boolean;
}
export declare class Yasr extends EventEmitter {
    results?: Parser;
    rootEl: HTMLDivElement;
    headerEl: HTMLDivElement;
    fallbackInfoEl: HTMLDivElement;
    resultsEl: HTMLDivElement;
    pluginControls: HTMLDivElement;
    config: Config;
    storage: YStorage;
    plugins: {
        [name: string]: Plugin<any>;
    };
    helpDrawn: Boolean;
    private drawnPlugin;
    private selectedPlugin;
    utils: {
        addScript: typeof addScript;
        addCSS: typeof addCss;
        sanitize: (val: string | Node) => string;
    };
    constructor(parent: HTMLElement, conf?: Partial<Config>, data?: any);
    private getConfigFromStorage;
    renderError(error: Parser.ErrorSummary): Promise<HTMLElement | undefined>;
    getStorageId(label: string, getter?: Config["persistenceId"]): string | undefined;
    somethingDrawn(): boolean;
    emptyFallbackElement(): void;
    getSelectedPluginName(): string;
    getSelectedPlugin(): Plugin<any>;
    private updatePluginSelectors;
    private getCompatiblePlugins;
    draw(): void;
    refresh(): void;
    destroy(): void;
    getPrefixes(): Prefixes;
    selectPlugin(plugin: string): void;
    private pluginSelectorsEl;
    drawPluginSelectors(): void;
    private fillFallbackBox;
    private drawPluginElement;
    private drawHeader;
    private downloadBtn;
    private drawDownloadIcon;
    private dataElement;
    private drawResponseInfo;
    private updateResponseInfo;
    private updateHelpButton;
    updateExportHeaders(): void;
    private documentationLink;
    private drawDocumentationButton;
    download(): void;
    handleLocalStorageQuotaFull(_e: any): void;
    getResponseFromStorage(): unknown;
    getPersistentConfig(): PersistentConfig;
    storePluginConfig(pluginName: string, conf: any): void;
    private storeConfig;
    private storeResponse;
    setResponse(data: any, duration?: number): void;
    private initializePlugins;
    static defaults: Config;
    static plugins: {
        [key: string]: typeof Plugin & {
            defaults?: any;
        };
    };
    static registerPlugin(name: string, plugin: typeof Plugin, enable?: boolean): void;
    static Dependencies: {
        [name: string]: Promise<any>;
    };
    static storageNamespace: string;
    static clearStorage(): void;
}
export declare type Prefixes = {
    [prefixLabel: string]: string;
};
export interface PluginConfig {
    dynamicConfig?: any;
    staticConfig?: any;
    enabled?: boolean;
}
export interface Config {
    persistenceId: ((yasr: Yasr) => string) | string | null;
    persistenceLabelResponse: string;
    persistenceLabelConfig: string;
    maxPersistentResponseSize: number;
    persistencyExpire: number;
    getPlainQueryLinkToEndpoint: (() => string | undefined) | undefined;
    getDownloadFileName?: () => string | undefined;
    plugins: {
        [pluginName: string]: PluginConfig;
    };
    pluginOrder: string[];
    defaultPlugin: string;
    prefixes: Prefixes | ((yasr: Yasr) => Prefixes);
    errorRenderers?: ((error: Parser.ErrorSummary) => Promise<HTMLElement | undefined>)[];
}
export declare function registerPlugin(name: string, plugin: typeof Plugin, enable?: boolean): void;
export { Plugin, DownloadInfo } from "./plugins";
export default Yasr;
