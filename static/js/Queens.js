let cells = []
let solutionCells = []
let solutions = []
let currentIndex = null

let problemTable = document.getElementById("problemTable")
let solutionTable = document.getElementById("solutionTable")
let send = document.getElementById("send")
let rightSolution = document.getElementById("rightSolution")
let leftSolution = document.getElementById("leftSolution")
let solutionIndex = document.getElementById("solutionIndex")

send.onclick = () => sendData()

let SETTING = [0, 1, 3, 3, -1, -1, -1, -1]

for (let y = 0; y < 8; y++) {
    let row = document.createElement("div")
    row.className = "row"
    problemTable.appendChild(row)

    for (let x = 0; x < 8; x++) {
        let cell = new ClickableCell(y, x)
        if (y % 2 === 0 && x % 2 === 0)
            cell.setDivColor("whiteCell")
        else if (y % 2 === 1 && x % 2 === 1)
            cell.setDivColor("whiteCell")
        else
            cell.setDivColor("blackCell")
        cell.div.classList.add("hoverCell")
        cells.push(cell)
        row.appendChild(cell.div)

        // if (y === SETTING[x]) {
        //     cell.show(true)
        // }
    }
}

for (let y = 0; y < 8; y++) {
    let row = document.createElement("div")
    row.className = "row"
    solutionTable.appendChild(row)

    for (let x = 0; x < 8; x++) {
        let cell = new Cell(y, x)
        if (y % 2 === 0 && x % 2 === 0)
            cell.setDivColor("whiteCell")
        else if (y % 2 === 1 && x % 2 === 1)
            cell.setDivColor("whiteCell")
        else
            cell.setDivColor("blackCell")
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
    let numberOfQueens = 0
    let xs = []
    let ys = []

    cells.forEach(cell => {
        if (cell.showed) {
            xs.push(cell.x)
            ys.push(cell.y)
            numberOfQueens++
        }
    })

    show(document.getElementById("warn_no_solution"), false)
    const rowDuplicates = xs.some((e, i, arr) => arr.indexOf(e) !== i)
    const columnDuplicates = ys.some((e, i, arr) => arr.indexOf(e) !== i)
    const crossDuplicates = checkCrossDuplicates(ys, xs)
    const tooManyQueens = numberOfQueens > 8

    show(document.getElementById("warn_double_col"), columnDuplicates)
    show(document.getElementById("warn_double_row"), rowDuplicates)
    show(document.getElementById("warn_double_cross"), crossDuplicates)
    show(document.getElementById("warn_too_many"), tooManyQueens)

    if (!tooManyQueens && !rowDuplicates && !columnDuplicates && !crossDuplicates) {
        let queens = {}
        for(let i = 0; i <numberOfQueens; i++)
            queens[xs[i]] = ys[i]
        console.log("sending", queens)

        let resp = await fetchSolution(queens)
        solutions = resp.solutions
        console.log(solutions)

        show(document.getElementById("warn_no_solution"), solutions.length === 0)

        // TODO tutaj może być wiele rozwiązań albo żadnego
        if (solutions.length !== 0) {
            currentIndex = 0
            renderSolution()
            rightSolution.addEventListener("click", rightClick)
            leftSolution.addEventListener("click", leftClick)
            for (let i = 0; i < cells.length; i++) {
                if (cells[i].showed)
                    solutionCells[i].addEmphasis()
                else
                    solutionCells[i].deleteEmphasis()
            }
        } else {
            currentIndex = null
            rightSolution.removeEventListener("click", rightClick)
            leftSolution.removeEventListener("click", leftClick)
            emptySolutions()
        }
    } else
        emptySolutions()
}

function checkCrossDuplicates(ys, xs) {
    for (let i = 0; i < ys.length; i++) {
        for (let j = i + 1; i < ys.length; i++) {
            let y_diff = Math.abs(ys[i] - ys[j])
            let x_diff = Math.abs(xs[i] - xs[j])
            if (y_diff !== 0 && y_diff === x_diff)
                return true
        }
    }
    return false
}

function emptySolutions() {
    solutionCells.forEach(cell => {
        cell.show(false)
        cell.deleteEmphasis()
    })
    solutionIndex.innerText = ""
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
    solutionIndex.innerText = `Rozwiązanie ${currentIndex + 1} z ${solutions.length}`
    solutionCells.forEach(cell => {
        cell.show(solutions[currentIndex][cell.x] === cell.y)
    })
}
