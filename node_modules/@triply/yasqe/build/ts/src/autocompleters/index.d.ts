/// <reference types="node" />
import { default as Yasqe, Token, Hint, Position, Config } from "../";
import { EventEmitter } from "events";
export interface CompleterConfig {
    onInitialize?: (this: CompleterConfig, yasqe: Yasqe) => void;
    isValidCompletionPosition: (yasqe: Yasqe) => boolean;
    get: (yasqe: Yasqe, token?: AutocompletionToken) => Promise<string[]> | string[];
    preProcessToken?: (yasqe: Yasqe, token: Token) => AutocompletionToken;
    postProcessSuggestion?: (yasqe: Yasqe, token: AutocompletionToken, suggestedString: string) => string;
    postprocessHints?: (yasqe: Yasqe, hints: Hint[]) => Hint[];
    bulk: boolean;
    autoShow?: boolean;
    persistenceId?: Config["persistenceId"];
    name: string;
}
export interface AutocompletionToken extends Token {
    autocompletionString?: string;
    tokenPrefix?: string;
    tokenPrefixUri?: string;
    from?: Partial<Position>;
    to?: Partial<Position>;
}
export declare class Completer extends EventEmitter {
    protected yasqe: Yasqe;
    private trie?;
    private config;
    constructor(yasqe: Yasqe, config: CompleterConfig);
    private getStorageId;
    private storeBulkCompletions;
    getCompletions(token?: AutocompletionToken): Promise<string[]>;
    initialize(): Promise<void>;
    private isValidPosition;
    private getHint;
    private getHints;
    autocomplete(fromAutoShow: boolean): boolean;
}
export declare function preprocessIriForCompletion(yasqe: Yasqe, token: AutocompletionToken): AutocompletionToken;
export declare function postprocessIriCompletion(_yasqe: Yasqe, token: AutocompletionToken, suggestedString: string): string;
export declare const fetchFromLov: (yasqe: Yasqe, type: "class" | "property", token?: AutocompletionToken) => Promise<string[]>;
export declare var completers: CompleterConfig[];
