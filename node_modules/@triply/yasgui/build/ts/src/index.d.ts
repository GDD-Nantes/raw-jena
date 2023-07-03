/// <reference types="node" />
import { EventEmitter } from "events";
import PersistentConfig from "./PersistentConfig";
import { default as Tab, PersistedJson as PersistedTabJson } from "./Tab";
import { EndpointSelectConfig, CatalogueItem } from "./endpointSelect";
import * as shareLink from "./linkUtils";
import TabElements from "./TabElements";
import { default as Yasqe, PartialConfig as YasqeConfig, RequestConfig } from "@triply/yasqe";
import { default as Yasr, Config as YasrConfig } from "@triply/yasr";
export declare type YasguiRequestConfig = Omit<RequestConfig<Yasgui>, "adjustQueryBeforeRequest"> & {
    adjustQueryBeforeRequest: RequestConfig<Yasqe>["adjustQueryBeforeRequest"];
};
export interface Config<EndpointObject extends CatalogueItem = CatalogueItem> {
    autofocus: boolean;
    endpointInfo: ((tab?: Tab) => Element) | undefined;
    copyEndpointOnNewTab: boolean;
    tabName: string;
    corsProxy: string | undefined;
    endpointCatalogueOptions: EndpointSelectConfig<EndpointObject>;
    populateFromUrl: boolean | ((configFromUrl: PersistedTabJson) => PersistedTabJson);
    autoAddOnInit: boolean;
    persistenceId: ((yasr: Yasgui) => string) | string | null;
    persistenceLabelConfig: string;
    persistenceLabelResponse: string;
    persistencyExpire: number;
    yasqe: Partial<YasqeConfig>;
    yasr: YasrConfig;
    requestConfig: YasguiRequestConfig;
    contextMenuContainer: HTMLElement | undefined;
    nonSslDomain?: string;
}
export declare type PartialConfig = {
    [P in keyof Config]?: Config[P] extends object ? Partial<Config[P]> : Config[P];
};
export declare type TabJson = PersistedTabJson;
export interface Yasgui {
    on(event: string | symbol, listener: (...args: any[]) => void): this;
    on(event: "tabSelect", listener: (instance: Yasgui, newTabId: string) => void): this;
    emit(event: "tabSelect", instance: Yasgui, newTabId: string): boolean;
    on(event: "tabClose", listener: (instance: Yasgui, tab: Tab) => void): this;
    emit(event: "tabClose", instance: Yasgui, tab: Tab): boolean;
    on(event: "query", listener: (instance: Yasgui, tab: Tab) => void): this;
    emit(event: "query", instance: Yasgui, tab: Tab): boolean;
    on(event: "queryAbort", listener: (instance: Yasgui, tab: Tab) => void): this;
    emit(event: "queryAbort", instance: Yasgui, tab: Tab): boolean;
    on(event: "queryResponse", listener: (instance: Yasgui, tab: Tab) => void): this;
    emit(event: "queryResponse", instance: Yasgui, tab: Tab): boolean;
    on(event: "tabChange", listener: (instance: Yasgui, tab: Tab) => void): this;
    emit(event: "tabChange", instance: Yasgui, tab: Tab): boolean;
    on(event: "tabAdd", listener: (instance: Yasgui, newTabId: string) => void): this;
    emit(event: "tabAdd", instance: Yasgui, newTabId: string): boolean;
    on(event: "tabOrderChanged", listener: (instance: Yasgui, tabList: string[]) => void): this;
    emit(event: "tabOrderChanged", instance: Yasgui, tabList: string[]): boolean;
    on(event: "fullscreen-enter", listener: (instance: Yasgui) => void): this;
    emit(event: "fullscreen-enter", instance: Yasgui): boolean;
    on(event: "fullscreen-leave", listener: (instance: Yasgui) => void): this;
    emit(event: "fullscreen-leave", instance: Yasgui): boolean;
    on(event: "endpointHistoryChange", listener: (instance: Yasgui, history: string[]) => void): this;
    emit(event: "endpointHistoryChange", instance: Yasgui, history: string[]): boolean;
    on(event: "autocompletionShown", listener: (instance: Yasgui, tab: Tab, widget: any) => void): this;
    emit(event: "autocompletionShown", instance: Yasgui, tab: Tab, widget: any): boolean;
    on(event: "autocompletionClose", listener: (instance: Yasgui, tab: Tab) => void): this;
    emit(event: "autocompletionClose", instance: Yasgui, tab: Tab): boolean;
}
export declare class Yasgui extends EventEmitter {
    rootEl: HTMLDivElement;
    tabElements: TabElements;
    _tabs: {
        [tabId: string]: Tab;
    };
    tabPanelsEl: HTMLDivElement;
    config: Config;
    persistentConfig: PersistentConfig;
    static Tab: typeof Tab;
    constructor(parent: HTMLElement, config: PartialConfig);
    hasFullscreen(fullscreen: boolean): void;
    getStorageId(label: string, getter?: Config["persistenceId"]): string | undefined;
    createTabName(name?: string, i?: number): string;
    tabNameTaken(name: string): Tab;
    getTab(tabId?: string): Tab | undefined;
    private markTabSelected;
    selectTabId(tabId: string): Tab;
    private tabConfigEquals;
    private findTabIdForConfig;
    private _registerTabListeners;
    _setPanel(panelId: string, panel: HTMLDivElement): void;
    _removePanel(panel: HTMLDivElement | undefined): void;
    addTab(setActive: boolean, partialTabConfig?: Partial<PersistedTabJson>, opts?: {
        atIndex?: number;
        avoidDuplicateTabs?: boolean;
    }): Tab;
    restoreLastTab(): void;
    destroy(): void;
    static linkUtils: typeof shareLink;
    static Yasr: typeof Yasr;
    static Yasqe: typeof Yasqe;
    static defaults: Config<CatalogueItem>;
    static corsEnabled: {
        [endpoint: string]: boolean;
    };
}
export declare function getRandomId(): string;
export default Yasgui;
