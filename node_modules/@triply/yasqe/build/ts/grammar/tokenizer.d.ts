import CodeMirror from "codemirror";
export interface State {
    tokenize: (stream: CodeMirror.StringStream, state: State) => string;
    inLiteral: "SINGLE" | "DOUBLE" | undefined;
    errorStartPos: number | undefined;
    errorEndPos: number | undefined;
    queryType: "SELECT" | "CONSTRUCT" | "ASK" | "DESCRIBE" | "INSERT" | "DELETE" | "LOAD" | "CLEAR" | "CREATE" | "DROP" | "COPY" | "MOVE" | "ADD" | undefined;
    inPrefixDecl: boolean;
    allowVars: boolean;
    allowBnodes: boolean;
    storeProperty: boolean;
    OK: boolean;
    possibleCurrent: string[];
    possibleNext: string[];
    stack: any[];
    variables: {
        [varName: string]: string;
    };
    prefixes: {
        [prefLabel: string]: string;
    };
    complete: boolean;
    lastProperty: string;
    lastPropertyIndex: number;
    errorMsg: string | undefined;
    lastPredicateOffset: number;
    currentPnameNs: string | undefined;
    possibleFullIri: boolean;
}
export interface Token {
    quotePos: "end" | "start" | "content" | undefined;
    cat: string;
    style: string;
    string: string;
    start: number;
}
export default function (config: CodeMirror.EditorConfiguration): CodeMirror.Mode<State>;
