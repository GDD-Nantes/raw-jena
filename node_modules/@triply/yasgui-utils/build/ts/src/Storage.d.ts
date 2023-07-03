export interface ItemWrapper<V = {}> {
    exp: number;
    val: V;
    namespace: string;
    time: number;
}
export default class Storage {
    private namespace;
    constructor(namespace: string);
    set<V = {}>(key: string | undefined, val: any, expInSeconds: number, onQuotaExceeded: (e: any) => void): void;
    remove(key: string): void;
    removeExpiredKeys(): void;
    removeAll(): void;
    removeNamespace(): void;
    get<V>(key?: string): V | undefined;
}
