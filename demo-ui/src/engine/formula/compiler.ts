import type {FormulaToken} from "../../types/formula";
import {FormulaParser} from "./parser";
import {CodeGenerator} from "./codeGenerator";

export function compileTokensToExpression(tokens: FormulaToken[]): string {
    if (!tokens.length) {
        return "";
    }
    const parser = new FormulaParser(tokens);
    const {ast} = parser.parse();
    if (!ast) {
        return "";
    }
    return new CodeGenerator().generateExpression(ast);
}
