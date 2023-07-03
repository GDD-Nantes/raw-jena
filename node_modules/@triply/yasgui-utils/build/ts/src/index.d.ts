export { default as Storage } from "./Storage";
export declare function drawSvgStringAsElement(svgString: string): HTMLDivElement;
export interface FaIcon {
    width: number;
    height: number;
    svgPathData: string;
}
export declare function drawFontAwesomeIconAsSvg(faIcon: FaIcon): string;
export declare function hasClass(el: Element | undefined, className: string): boolean;
export declare function addClass(el: Element | undefined | null, ...classNames: string[]): void;
export declare function removeClass(el: Element | undefined | null, className: string): void;
export declare function getAsValue<E, A>(valueOrFn: E | ((arg: A) => E), arg: A): E;
