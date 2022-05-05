let cells = []

window.onload = () => {
    let table = document.getElementById("table")
    table.className = "table"

    let SETTING = [0, 1, 3, 3, -1, -1, -1, -1]

    for (let y=0; y<8; y++) {
        let row = document.createElement("div")
        row.className = "row"
        table.appendChild(row)

        for (let x=0; x<8; x++) {
            let cell = new Cell(y, x)
            cells.push(cell)
            row.appendChild(cell.div)

            if (y == SETTING[x]) {
                cell.show(true)
            }
        }
    }

    let send = document.getElementById("send")
    send.onclick = () => sendData()
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
        let solutions = resp.solutions
        console.log("sol", solutions)

        // TODO tutaj może być wiele rozwiązań albo żadnego
        let solution = solutions[0]

        cells.forEach(cell => {
            cell.show(solution[cell.x] == cell.y)
        })
    }
}