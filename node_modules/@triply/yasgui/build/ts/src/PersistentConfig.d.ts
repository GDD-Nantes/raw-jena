import Yasgui from "./";
import * as Tab from "./Tab";
export declare var storageNamespace: string;
export interface PersistedJson {
    endpointHistory: string[];
    tabs: string[];
    active: string | undefined;
    tabConfig: {
        [tabId: string]: Tab.PersistedJson;
    };
    lastClosedTab: {
        index: number;
        tab: Tab.PersistedJson;
    } | undefined;
}
export default class PersistentConfig {
    private persistedJson;
    private storageId;
    private yasgui;
    private storage;
    constructor(yasgui: Yasgui);
    setActive(id: string): void;
    getActiveId(): string | undefined;
    addToTabList(tabId: string, index?: number): void;
    setTabOrder(tabs: string[]): void;
    getEndpointHistory(): string[];
    retrieveLastClosedTab(): {
        index: number;
        tab: Tab.PersistedJson;
    };
    hasLastClosedTab(): boolean;
    deleteTab(tabId: string): void;
    private registerListeners;
    private toStorage;
    private fromStorage;
    private handleLocalStorageQuotaFull;
    getTabs(): string[];
    getTab(tabId: string): Tab.PersistedJson;
    setTab(tabId: string, tabConfig: Tab.PersistedJson): void;
    tabIsActive(tabId: string): boolean;
    currentId(): string;
    static clear(): void;
}
