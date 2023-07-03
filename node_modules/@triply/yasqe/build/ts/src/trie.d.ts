export default class Trie {
    private words;
    private prefixes;
    private children;
    insert(str: string, pos?: number): void;
    remove(str: string, pos?: number): void;
    update(strOld: string, strNew: string): void;
    countWord(str: string, pos?: number): number;
    countPrefix(str: string, pos: number): number;
    find(str: string): boolean;
    getAllWords(str: string): string[];
    autoComplete(str: string, pos?: number): string[];
}
