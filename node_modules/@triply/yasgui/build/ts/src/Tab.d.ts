/// <reference types="node" />
import { EventEmitter } from "events";
import { default as Yasqe } from "@triply/yasqe";
import { default as Yasr, Parser, PersistentConfig as YasrPersistentConfig } from "@triply/yasr";
import * as shareLink from "./linkUtils";
import { default as Yasgui, YasguiRequestConfig } from "./";
export interface PersistedJsonYasr extends YasrPersistentConfig {
    responseSummary: Parser.ResponseSummary;
}
export interface PersistedJson {
    name: string;
    id: string;
    yasqe: {
        value: string;
        editorHeight?: string;
    };
    yasr: {
        settings: YasrPersistentConfig;
        response: Parser.ResponseSummary | undefined;
    };
    requestConfig: YasguiRequestConfig;
}
export interface Tab {
    on(event: string | symbol, listener: (...args: any[]) => void): this;
    on(event: "change", listener: (tab: Tab, config: PersistedJson) => void): this;
    emit(event: "change", tab: Tab, config: PersistedJson): boolean;
    on(event: "query", listener: (tab: Tab) => void): this;
    emit(event: "query", tab: Tab): boolean;
    on(event: "queryAbort", listener: (tab: Tab) => void): this;
    emit(event: "queryAbort", tab: Tab): boolean;
    on(event: "queryResponse", listener: (tab: Tab) => void): this;
    emit(event: "queryResponse", tab: Tab): boolean;
    on(event: "close", listener: (tab: Tab) => void): this;
    emit(event: "close", tab: Tab): boolean;
    on(event: "endpointChange", listener: (tab: Tab, endpoint: string) => void): this;
    emit(event: "endpointChange", tab: Tab, endpoint: string): boolean;
    on(event: "autocompletionShown", listener: (tab: Tab, widget: any) => void): this;
    emit(event: "autocompletionShown", tab: Tab, widget: any): boolean;
    on(event: "autocompletionClose", listener: (tab: Tab) => void): this;
    emit(event: "autocompletionClose", tab: Tab): boolean;
}
export declare class Tab extends EventEmitter {
    private persistentJson;
    yasgui: Yasgui;
    private yasqe;
    private yasr;
    private rootEl;
    private controlBarEl;
    private yasqeWrapperEl;
    private yasrWrapperEl;
    private endpointSelect;
    private tabPanel?;
    constructor(yasgui: Yasgui, conf: PersistedJson);
    name(): string;
    getPersistedJson(): PersistedJson;
    getId(): string;
    private draw;
    hide(): void;
    show(): void;
    select(): void;
    close(): void;
    getQuery(): string;
    setQuery(query: string): this;
    getRequestConfig(): YasguiRequestConfig;
    private initControlbar;
    getYasqe(): Yasqe;
    getYasr(): Yasr;
    private initTabSettingsMenu;
    private initEndpointSelectField;
    private checkEndpointForCors;
    setEndpoint(endpoint: string, endpointHistory?: string[]): this;
    getEndpoint(): string;
    updateContextMenu(): void;
    getShareableLink(baseURL?: string): string;
    getShareObject(): shareLink.ShareConfigObject;
    private getTabListEl;
    setName(newName: string): this;
    hasResults(): boolean;
    getName(): string;
    query(): Promise<any>;
    setRequestConfig(requestConfig: Partial<YasguiRequestConfig>): void;
    private getStaticRequestConfig;
    private initYasqe;
    private destroyYasqe;
    handleYasqeBlur: (yasqe: Yasqe) => void;
    handleYasqeQuery: (yasqe: Yasqe) => void;
    handleYasqeQueryAbort: () => void;
    handleYasqeResize: (_yasqe: Yasqe, newSize: string) => void;
    handleAutocompletionShown: (_yasqe: Yasqe, widget: string) => void;
    handleAutocompletionClose: (_yasqe: Yasqe) => void;
    handleQueryResponse: (_yasqe: Yasqe, response: any, duration: number) => void;
    private initYasr;
    destroy(): void;
    static getDefaults(yasgui?: Yasgui): PersistedJson;
}
export default Tab;
