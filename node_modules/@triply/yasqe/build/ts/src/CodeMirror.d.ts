import { Editor as CmEditor, Doc as CmDoc, Token as CmToken, Position as CmPosition, EditorConfiguration as CmEditorConfiguration } from "codemirror";
import * as sparql11Mode from "../grammar/tokenizer";
import { TokenizerState } from "./index";
declare namespace CodeMirror {
    type Doc = CmDoc;
    type Position = CmPosition;
    type EditorConfiguration = CmEditorConfiguration;
    interface Token extends CmToken {
        state: sparql11Mode.State;
    }
}
interface CodeMirror extends Omit<CmEditor, "getOption" | "setOption" | "on" | "off"> {
    getOption(opt: "queryType"): TokenizerState["queryType"];
    setOption(opt: "queryType", val: TokenizerState["queryType"]): void;
    foldCode(firstPrefixLine: number, prefix: string, collapse: "fold" | "unfold"): void;
}
declare const CodeMirror: {
    new (): CodeMirror;
    signal: (target: any, name: string, ...args: any[]) => void;
};
export default CodeMirror;
