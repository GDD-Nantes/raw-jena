import * as SuperAgent from "superagent";
import N3 from "n3";
declare namespace Parser {
    interface ErrorSummary {
        status?: number;
        text: string;
        statusText?: string;
    }
    interface BindingValue {
        value: string;
        type: "uri" | "literal" | "typed-literal" | "bnode";
        datatype?: string;
        "xml:lang"?: string;
    }
    interface Binding {
        [varname: string]: BindingValue;
    }
    interface SparqlResults {
        head: {
            vars: string[];
        };
        boolean?: boolean;
        results?: {
            bindings: Binding[];
        };
    }
    interface ResponseSummary {
        data?: any;
        error?: ErrorSummary;
        status?: number;
        contentType?: string;
        executionTime?: number;
    }
    type PostProcessBinding = (binding: Binding) => Binding;
}
declare class Parser {
    private res;
    private summary;
    private errorSummary;
    private error;
    private type;
    private executionTime;
    constructor(responseOrObject: Parser.ResponseSummary | SuperAgent.Response | Error | any, executionTime?: number);
    setResponse(res: SuperAgent.Response): void;
    setSummary(summary: Parser.ResponseSummary | any): void;
    hasError(): boolean;
    getError(): Parser.ErrorSummary;
    getContentType(): any;
    private json;
    getAsJson(): false | Parser.SparqlResults;
    private getData;
    getResponseTime(): number;
    private getParserFromContentType;
    private doLuckyGuess;
    getVariables(): string[];
    getBoolean(): boolean | undefined;
    getBindings(): Parser.Binding[];
    private statements;
    getStatements(): N3.Quad[];
    getOriginalResponseAsString(): string;
    getOriginalResponse(): any;
    getType(): "json" | "xml" | "csv" | "tsv" | "ttl";
    getStatus(): number | undefined;
    getAsStoreObject(maxResponseSize: number): Parser.ResponseSummary | undefined;
    asCsv(): string;
}
export default Parser;
