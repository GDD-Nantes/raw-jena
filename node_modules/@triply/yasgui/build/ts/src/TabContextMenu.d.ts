import { default as Yasgui } from "./";
import { TabListEl } from "./TabElements";
export interface TabContextConfig {
    name: string;
    action: (this: HTMLElement, ev: MouseEvent) => any;
    enabled: boolean;
}
export default class TabContextMenu {
    private yasgui;
    private contextEl;
    private newTabEl;
    private renameTabEl;
    private copyTabEl;
    private closeTabEl;
    private closeOtherTabsEl;
    private reOpenOldTab;
    private rootEl;
    private tabRef;
    constructor(yasgui: Yasgui, rootEl: HTMLElement);
    private getMenuItemEl;
    private draw;
    redraw(): void;
    handleContextClick: (event: MouseEvent) => void;
    openConfigMenu(currentTabId: string, currentTabEl: TabListEl, event: MouseEvent): void;
    closeConfigMenu: () => void;
    static get(yasgui: Yasgui, rootEl: HTMLElement): TabContextMenu;
    unregisterEventListeners(): void;
    destroy(): void;
}
