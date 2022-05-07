let cells = []
let solutionCells = []
let solutions = []
let currentIndex = null

let problemTable = document.getElementById("problemTable")
let solutionTable = document.getElementById("solutionTable")
let send = document.getElementById("send")
let rightSolution = document.getElementById("rightSolution")
let leftSolution = document.getElementById("leftSolution")

send.onclick = () => sendData()

let SETTING = [0, 1, 3, 3, -1, -1, -1, -1]

for (let y = 0; y < 8; y++) {
    let row = document.createElement("div")
    row.className = "row"
    problemTable.appendChild(row)

    for (let x = 0; x < 8; x++) {
        let cell = new ClickableCell(y, x)
        cell.div.classList.add("hoverCell")
        cells.push(cell)
        row.appendChild(cell.div)

        if (y == SETTING[x]) {
            cell.show(true)
        }
    }
}

for (let y = 0; y < 8; y++) {
    let row = document.createElement("div")
    row.className = "row"
    solutionTable.appendChild(row)

    for (let x = 0; x < 8; x++) {
        let cell = new Cell(y, x)
        solutionCells.push(cell)
        row.appendChild(cell.div)
    }
}

function show(elem, showed) {
    elem.style.display = showed ? "block" : "none"
}

async function fetchSolution(queens) {
    let resp = await fetch("/queens?" + new URLSearchParams(queens))
    return await resp.json()
}

async function sendData() {

    let queens = {}
    let good = true
    let keys = 0

    cells.forEach(cell => {
        if (cell.showed) {
            console.log(queens)
            if (queens[cell.x] !== undefined) {
                good = false
            }
            queens[cell.x] = cell.y
            keys++
        }
    })

    show(document.getElementById("warn_double"), !good)
    show(document.getElementById("warn_too_many"), keys > 8)

    if (keys > 8) {
        good = false
    }

    if (good) {
        console.log("sending", queens)
        // let setting = []
        // for (let x=0; x<8; x++) {
        //     if (queens[x] !== undefined) {
        //         setting[x] = queens[x]
        //     }
        //     else {
        //         setting[x] = -1
        //     }
        // }

        // console.log(setting)

        let resp = await fetchSolution(queens)
        solutions = resp.solutions
        console.log("sol", solutions)

        // TODO tutaj może być wiele rozwiązań albo żadnego
        if (solutions.length != 0) {
            currentIndex = 0
            renderSolution()
            rightSolution.addEventListener("click", rightClick)
            leftSolution.addEventListener("click", leftClick)
        } else {
            currentIndex = null
            rightSolution.removeEventListener("click", rightClick)
            leftSolution.removeEventListener("click", leftClick)
        }
    }
}

function leftClick() {
    currentIndex--
    if (currentIndex < 0)
        currentIndex = solutions.length - 1
    renderSolution()
}

function rightClick() {
    currentIndex++
    currentIndex %= solutions.length
    renderSolution()
}

function renderSolution() {
    solutionCells.forEach(cell => {
        cell.show(solutions[currentIndex][cell.x] == cell.y)
    })
}