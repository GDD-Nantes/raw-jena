import { default as Yasqe, Config } from "./";
export declare type YasqeAjaxConfig = Config["requestConfig"];
export interface PopulatedAjaxConfig {
    url: string;
    reqMethod: "POST" | "GET";
    headers: {
        [key: string]: string;
    };
    accept: string;
    args: RequestArgs;
    withCredentials: boolean;
}
export declare function getAjaxConfig(yasqe: Yasqe, _config?: Partial<Config["requestConfig"]>): PopulatedAjaxConfig | undefined;
export declare function executeQuery(yasqe: Yasqe, config?: YasqeAjaxConfig): Promise<any>;
export declare type RequestArgs = {
    [argName: string]: string | string[];
};
export declare function getUrlArguments(yasqe: Yasqe, _config: Config["requestConfig"]): RequestArgs;
export declare function getAcceptHeader(yasqe: Yasqe, _config: Config["requestConfig"]): any;
export declare function getAsCurlString(yasqe: Yasqe, _config?: Config["requestConfig"]): string;
