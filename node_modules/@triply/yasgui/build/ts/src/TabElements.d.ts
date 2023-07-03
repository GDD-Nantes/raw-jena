import Yasgui from "./";
import TabContextMenu from "./TabContextMenu";
export interface TabList {
}
export declare class TabListEl {
    private tabList;
    private tabId;
    private yasgui;
    private renameEl?;
    private nameEl?;
    tabEl?: HTMLDivElement;
    constructor(yasgui: Yasgui, tabList: TabList, tabId: string);
    delete(): void;
    startRename(): void;
    active(active: boolean): void;
    rename(name: string): void;
    setAsQuerying(querying: boolean): void;
    draw(name: string): HTMLDivElement;
    private openTabConfigMenu;
    redrawContextMenu(): void;
}
export declare class TabList {
    yasgui: Yasgui;
    private _selectedTab?;
    private addTabEl?;
    _tabs: {
        [tabId: string]: TabListEl;
    };
    _tabsListEl?: HTMLDivElement;
    tabContextMenu?: TabContextMenu;
    tabEntryIndex: number | undefined;
    constructor(yasgui: Yasgui);
    get(tabId: string): TabListEl;
    private registerListeners;
    private getActiveIndex;
    private handleKeydownArrowKeys;
    drawTabsList(): HTMLDivElement;
    handleAddNewTab: (event: Event) => void;
    addTab(tabId: string, index?: number): void;
    deriveTabOrderFromEls(): string[];
    selectTab(tabId: string): void;
    drawTab(tabId: string, index?: number): void;
    destroy(): void;
}
export default TabList;
