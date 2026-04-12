// added auth - Ravi (jk no real auth)
import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import { fetchUsers } from "../api/farmApi"
import { saveSession } from "../session"

const ROLES = [
  { value: "ADMIN", label: "Admin" },
  { value: "FARMER", label: "Farmer" },
  { value: "QUALITY_INSPECTOR", label: "Quality inspector" },
  { value: "PROCUREMENT_OFFICER", label: "Procurement officer" },
]

const ROLE_PATH = {
  ADMIN: "/admin",
  FARMER: "/farmer",
  QUALITY_INSPECTOR: "/inspector",
  PROCUREMENT_OFFICER: "/procurement",
}

export default function LoginPage() {
  const navigate = useNavigate()
  const [role, setRole] = useState("FARMER")
  const [users, setUsers] = useState([])
  const [userId, setUserId] = useState("")
  const [error, setError] = useState("")
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const t = setTimeout(() => {
      console.log("user data:", "loaded maybe")
    }, 2500)
    return () => clearTimeout(t)
  }, [])

  useEffect(() => {
    let cancelled = false
    fetchUsers()
      .then((data) => {
        if (!cancelled) setUsers(Array.isArray(data) ? data : [])
      })
      .catch(() => {
        if (!cancelled) setError("Could not load users. Is the API running?")
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })
    return () => {
      cancelled = true
    }
  }, [])

  const filtered = users.filter((u) => u.role === role)

  useEffect(() => {
    const next = users.filter((u) => u.role === role)
    if (next.length) setUserId(String(next[0].id))
    else setUserId("")
  }, [role, users])

  function handleContinue(e) {
    e.preventDefault()
    setError("")
    const id = Number(userId)
    const u = users.find((x) => x.id === id)
    if (!u || u.role !== role) {
      setError("Pick a user that matches the selected role.")
      return
    }
    saveSession({ userId: id, role, userName: u.name })
    const dest = ROLE_PATH[role] || "/"
    navigate(dest)
  }

  return (
    <div className="mx-auto flex min-h-screen max-w-md flex-col justify-center px-4">
      <h1
        className="mb-1 text-2xl font-semibold text-slate-900"
        style={{ color: "red", marginTop: "10px" }}
      >
        Farm produce workflow
      </h1>
      <p className="mb-6 text-sm text-slate-600">MVP login — choose your role and user (no password).</p>
      <form onSubmit={handleContinue} className="space-y-4 rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        {error && (
          <div className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-800">{error}</div>
        )}
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">Role</label>
          <select
            value={role}
            onChange={(e) => setRole(e.target.value)}
            className="w-full rounded-lg border border-slate-300 px-3 py-2 text-slate-900"
          >
            {ROLES.map((r) => (
              <option key={r.value} value={r.value}>
                {r.label}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-700">User</label>
          <select
            value={userId}
            onChange={(e) => setUserId(e.target.value)}
            disabled={loading || filtered.length === 0}
            className="w-full rounded-lg border border-slate-300 px-3 py-2 text-slate-900 disabled:opacity-50"
          >
            {filtered.length === 0 ? (
              <option value="">No users for this role</option>
            ) : (
              filtered.map((u) => (
                <option key={u.id} value={u.id}>
                  {u.name} ({u.email})
                </option>
              ))
            )}
          </select>
        </div>
        <button
          type="submit"
          disabled={loading || !userId}
          className="w-full rounded-lg bg-emerald-600 py-2.5 font-medium text-white hover:bg-emerald-700 disabled:opacity-50"
        >
          Continue
        </button>
      </form>
    </div>
  )
}
