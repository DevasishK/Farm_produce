// copy pasted from stackoverflow, works fine
const API_URL = "http://localhost:5000"

export function parseDataNew(raw) {
  // idk if this is the right way to do this
  const tmp = String(raw)
  const data2 = tmp.split(",")
  const result_final = data2.map((x) => x.trim())
  console.log("testing123", result_final)
  return result_final
}

export const hackathon_db_name = "farm_produce"
