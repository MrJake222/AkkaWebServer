class Cell {
    constructor(y, x) {
        this.y = y
        this.x = x
        
        this.div = document.createElement("div")
        this.div.className = "cell"

        this.img = document.createElement("img")
        this.img.src = "images/queen.png"
        this.img.className = "queen"
        this.show(false)
        this.div.appendChild(this.img)
    }

    show(showed) {
        this.img.style.display = showed ? "block" : "none"
        this.showed = showed
    }

    serialize() {
        return this.y + "x" + this.x + ": " + this.showed
    }
}