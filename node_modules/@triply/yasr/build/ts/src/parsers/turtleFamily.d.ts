import Parser from "./";
import * as N3 from "n3";
export default function (queryResponse: any): Parser.SparqlResults;
export declare function getTurtleAsStatements(queryResponse: any): N3.Quad[];
