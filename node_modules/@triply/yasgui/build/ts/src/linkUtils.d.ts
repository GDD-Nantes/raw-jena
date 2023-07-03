import { default as Tab, PersistedJson } from "./Tab";
import { PlainRequestConfig } from "@triply/yasqe";
export declare type RequestArgs = {
    [argName: string]: string | string[];
};
export declare function appendArgsToUrl(_url: string, args: RequestArgs): string;
export declare function createShareLink(forUrl: string, tab: Tab): any;
export declare type ShareConfigObject = {
    query: string;
    endpoint: string;
    requestMethod: PlainRequestConfig["method"];
    tabTitle: string;
    headers: PlainRequestConfig["headers"];
    contentTypeConstruct: string;
    contentTypeSelect: string;
    args: PlainRequestConfig["args"];
    namedGraphs: PlainRequestConfig["namedGraphs"];
    defaultGraphs: PlainRequestConfig["defaultGraphs"];
    outputFormat?: string;
    outputSettings: any;
};
export declare function createShareConfig(tab: Tab): ShareConfigObject;
export declare function getConfigFromUrl(defaults: PersistedJson, _url?: string): PersistedJson | undefined;
export declare function queryCatalogConfigToTabConfig<Q extends QueryCatalogConfig>(catalogConfig: Q, defaults?: PersistedJson): PersistedJson;
export interface QueryCatalogConfig {
    service: string;
    name: string;
    description: string;
    requestConfig?: {
        payload: {
            query: string;
            "default-graph-uri"?: string | string[];
            "named-graph-uri"?: string | string[];
        };
        headers?: {
            [key: string]: string;
        };
    };
    renderConfig?: {
        output: string;
        settings?: any;
    };
}
