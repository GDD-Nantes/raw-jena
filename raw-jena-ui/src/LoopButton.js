
/// Basic button element that states if the query should loop, ie.,
/// if the query engine should call repeatedly the backend server in a pay-as-you-go
/// fashion.
export class LoopButton {

    shouldLoop = false;

    constructor (viewPoint) {
        const loopButton = document.createElement("button");
        loopButton.setAttribute("id", "loop_button");
        loopButton.setAttribute("title", "Activate loop");
        loopButton.innerHTML = `
      <svg id="loop_button_svg" width="100%" height="100%" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
        <g id="SVGRepo_bgCarrier" stroke-width="0"></g>
        <g id="SVGRepo_tracerCarrier" stroke-linecap="round" stroke-linejoin="round"></g>
        <g id="SVGRepo_iconCarrier">
          <path d="M20 7v7c0 1.103-.896 2-2 2H2c-1.104 0-2-.897-2-2V7a2 2 0 0 1 2-2h7V3l4 3.5L9 10V8H3v5h14V8h-3V5h4a2 2 0 0 1 2 2z"></path>
        </g>
      </svg>`;
        
        loopButton.addEventListener("click", event => {
            this.shouldLoop = !this.shouldLoop;
            let loopSVG = document.getElementById("loop_button_svg");
            if (this.shouldLoop) {
                loopButton.setAttribute("title", "Deactivate loop");
                loopSVG.style.stroke = "var(--aurora-4)";
                loopSVG.style.fill = "var(--aurora-4)";
                
            } else {
                loopButton.setAttribute("title", "Activate loop");
                loopSVG.style.stroke = "var(--aurora-1)";
                loopSVG.style.fill = "var(--aurora-1)";
            }
        });
        
        viewPoint.append(loopButton);
    }

    /// forces the stop.
    stopLoop() {
        this.shouldLoop = false;
    }

}

