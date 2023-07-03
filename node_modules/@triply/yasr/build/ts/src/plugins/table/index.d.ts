/// <reference types="datatables.net" />
import { Plugin, DownloadInfo } from "../";
import Yasr from "../../";
export interface PluginConfig {
    openIriInNewWindow: boolean;
    tableConfig: DataTables.Settings;
}
export interface PersistentConfig {
    pageSize?: number;
    compact?: boolean;
    isEllipsed?: boolean;
}
export default class Table implements Plugin<PluginConfig> {
    private config;
    private persistentConfig;
    private yasr;
    private tableControls;
    private tableEl;
    private dataTable;
    private tableFilterField;
    private tableSizeField;
    private tableCompactSwitch;
    private tableEllipseSwitch;
    private tableResizer;
    helpReference: string;
    label: string;
    priority: number;
    getIcon(): HTMLDivElement;
    constructor(yasr: Yasr);
    static defaults: PluginConfig;
    private getRows;
    private getUriLinkFromBinding;
    private getCellContent;
    private formatLiteral;
    private getColumns;
    private getSizeFirstColumn;
    draw(persistentConfig: PersistentConfig): void;
    private setEllipsisHandlers;
    private handleTableSearch;
    private handleTableSizeSelect;
    private handleSetCompactToggle;
    private handleSetEllipsisToggle;
    drawControls(): void;
    download(filename?: string): DownloadInfo;
    canHandleResults(): boolean;
    private removeControls;
    private destroyResizer;
    destroy(): void;
}
