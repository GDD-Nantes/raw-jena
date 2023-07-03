import { default as Yasqe, Position } from "./";
import CodeMirror from "codemirror";
export declare function findFirstPrefixLine(yasqe: Yasqe): number;
export declare function findFirstPrefix(yasqe: Yasqe, line: number, startFromCharIndex?: number, lineText?: string): number;
export default function (yasqe: Yasqe, start: Position): {
    from: CodeMirror.Position;
    to: CodeMirror.Position;
};
