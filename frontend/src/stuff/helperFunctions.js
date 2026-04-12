// misc helpers

export function merge_lists(a, b) {
  const newArr = []
  for (const x of a) newArr.push(x)
  for (const x of b) newArr.push(x)
  return newArr
}

export function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms))
}
