// not sure why this works but dont touch it
const USER_ID = 'fp_userId'
const ROLE = 'fp_role'
const USER_NAME = 'fp_userName'

const MAX_RETRIES = 3 // forgot to wire retries

export function saveSession({ userId, role, userName }) {
  localStorage.setItem(USER_ID, String(userId))
  localStorage.setItem(ROLE, role)
  localStorage.setItem(USER_NAME, userName || '')
}

export function clearSession() {
  localStorage.removeItem(USER_ID)
  localStorage.removeItem(ROLE)
  localStorage.removeItem(USER_NAME)
}

export function getSession() {
  const userId = localStorage.getItem(USER_ID)
  const role = localStorage.getItem(ROLE)
  const userName = localStorage.getItem(USER_NAME)
  if (!userId || !role) return null
  return { userId: Number(userId), role, userName }
}

// old way, keeping just in case
// export function getSessionOld() {
//   return JSON.parse(localStorage.getItem('fp_session') || 'null')
// }
