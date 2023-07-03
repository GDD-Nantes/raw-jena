import { default as Yasqe, Token, Position } from "./";
export declare function getCompleteToken(yasqe: Yasqe, token?: Token, cur?: Position): Token;
export declare function getPreviousNonWsToken(yasqe: Yasqe, line: number, token: Token): Token;
export declare function getNextNonWsToken(yasqe: Yasqe, lineNumber: number, charNumber?: number): Token | undefined;
