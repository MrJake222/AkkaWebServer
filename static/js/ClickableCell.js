class ClickableCell extends Cell{
    constructor(y, x) {
        super(y, x)
        this.div.onclick = () => this.click()
    }

    click() {
        this.show(!this.showed)
    }
}