/// <reference types="node" />
import { EventEmitter } from "events";
import parse from "autosuggest-highlight/parse";
interface AutocompleteItem<T> {
    index: number;
    value: T;
    key: keyof T;
    match: string;
}
export interface CatalogueItem {
    endpoint: string;
    type?: "history";
}
export interface RenderedCatalogueItem<T> {
    matches: {
        [k in keyof T]?: ReturnType<typeof parse>;
    } & {
        endpoint?: ReturnType<typeof parse>;
    };
}
export interface EndpointSelectConfig<T = CatalogueItem> {
    keys: (keyof T)[];
    getData: () => T[];
    renderItem: (data: AutocompleteItem<T> & RenderedCatalogueItem<T>, source: HTMLElement) => void;
}
export interface EndpointSelect {
    on(event: string | symbol, listener: (...args: any[]) => void): this;
    on(event: "remove", listener: (endpoint: string, history: string[]) => void): this;
    emit(event: "remove", endpoint: string, history: string[]): boolean;
    on(event: "select", listener: (endpoint: string, history: string[]) => void): this;
    emit(event: "select", endpoint: string, history: string[]): boolean;
}
export declare class EndpointSelect extends EventEmitter {
    private container;
    private options;
    private value;
    private history;
    private inputField;
    constructor(initialValue: string, container: HTMLDivElement, options: EndpointSelectConfig, history: string[]);
    private draw;
    private clearListSuggestionList;
    setEndpoint(endpoint: string, endpointHistory?: string[]): void;
    destroy(): void;
}
export default EndpointSelect;
