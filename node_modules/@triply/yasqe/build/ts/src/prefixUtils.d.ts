import { default as Yasqe, Token } from "./";
export declare type Prefixes = {
    [prefixLabel: string]: string;
};
export declare function addPrefixes(yasqe: Yasqe, prefixes: string | Prefixes): void;
export declare function addPrefixAsString(yasqe: Yasqe, prefixString: string): void;
export declare function removePrefixes(yasqe: Yasqe, prefixes: Prefixes): void;
export declare function getPrefixesFromQuery(yasqe: Yasqe): Token["state"]["prefixes"];
export declare function getIndentFromLine(yasqe: Yasqe, line: number, charNumber?: number): string;
