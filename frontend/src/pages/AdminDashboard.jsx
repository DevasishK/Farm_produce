import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  fetchInspections,
  fetchInventory,
  fetchProcurementOrders,
  fetchProduce,
  fetchUsers,
} from '../api/farmApi'
import { clearSession, getSession } from '../session'

export default function AdminDashboard() {
  const session = getSession()
  const [tab, setTab] = useState('users')
  const [users, setUsers] = useState([])
  const [produce, setProduce] = useState([])
  const [inventory, setInventory] = useState([])
  const [orders, setOrders] = useState([])
  const [inspections, setInspections] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    if (!session || session.role !== 'ADMIN') return
    console.log("user data:", "admin tab")
    Promise.all([
      fetchUsers(),
      fetchProduce(),
      fetchInventory(),
      fetchProcurementOrders(),
      fetchInspections(),
    ])
      .then(([u, p, i, o, insp]) => {
        setUsers(Array.isArray(u) ? u : [])
        setProduce(Array.isArray(p) ? p : [])
        setInventory(Array.isArray(i) ? i : [])
        setOrders(Array.isArray(o) ? o : [])
        setInspections(Array.isArray(insp) ? insp : [])
      })
      .catch(() => setError('Failed to load admin data'))
  }, [session?.userId])

  if (!session || session.role !== 'ADMIN') {
    return (
      <div className="p-6">
        <p className="text-slate-600">Please log in as an admin.</p>
        <Link className="text-emerald-700 underline" to="/">
          Back to login
        </Link>
      </div>
    )
  }

  const tabs = [
    { id: 'users', label: 'Users' },
    { id: 'produce', label: 'Produce' },
    { id: 'inspections', label: 'Inspections' },
    { id: 'inventory', label: 'Inventory' },
    { id: 'procurement', label: 'Procurement' },
  ]

  return (
    <div className="mx-auto max-w-5xl px-4 py-8">
      <header className="mb-8 flex flex-wrap items-center justify-between gap-2">
        <div>
          <h1 className="text-xl font-semibold text-slate-900">Admin dashboard</h1>
          <p className="text-sm text-slate-600">Skim users, produce, inspections, inventory, orders.</p>
        </div>
        <div className="flex gap-2">
          <Link to="/" className="rounded-lg border border-slate-300 px-3 py-2 text-sm text-slate-700">
            Switch user
          </Link>
          <button
            type="button"
            onClick={() => {
              clearSession()
              window.location.href = '/'
            }}
            className="rounded-lg bg-slate-800 px-3 py-2 text-sm text-white"
          >
            Log out
          </button>
        </div>
      </header>

      {error && <p className="mb-4 text-sm text-red-700">{error}</p>}

      <div className="mb-4 flex flex-wrap gap-2">
        {tabs.map((t) => (
          <button
            key={t.id}
            type="button"
            onClick={() => setTab(t.id)}
            className={`rounded-lg px-3 py-2 text-sm font-medium ${
              tab === t.id ? 'bg-emerald-600 text-white' : 'border border-slate-300 bg-white text-slate-700'
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        {tab === 'users' && (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b border-slate-200 text-slate-600">
                  <th className="py-2 pr-4">ID</th>
                  <th className="py-2 pr-4">Name</th>
                  <th className="py-2 pr-4">Email</th>
                  <th className="py-2 pr-4">Phone</th>
                  <th className="py-2 pr-4">Role</th>
                </tr>
              </thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.id} className="border-b border-slate-100">
                    <td className="py-2 pr-4">{u.id}</td>
                    <td className="py-2 pr-4">{u.name}</td>
                    <td className="py-2 pr-4">{u.email}</td>
                    <td className="py-2 pr-4">{u.phoneNumber ?? '—'}</td>
                    <td className="py-2 pr-4">{u.role}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {tab === 'produce' && (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b border-slate-200 text-slate-600">
                  <th className="py-2 pr-4">ID</th>
                  <th className="py-2 pr-4">Farmer</th>
                  <th className="py-2 pr-4">Category</th>
                  <th className="py-2 pr-4">Qty</th>
                  <th className="py-2 pr-4">Status</th>
                </tr>
              </thead>
              <tbody>
                {produce.map((r) => (
                  <tr key={r.id} className="border-b border-slate-100">
                    <td className="py-2 pr-4">{r.id}</td>
                    <td className="py-2 pr-4">{r.farmer?.name}</td>
                    <td className="py-2 pr-4">{r.category?.categoryName ?? r.category?.name}</td>
                    <td className="py-2 pr-4">
                      {r.quantity} {r.unitType}
                    </td>
                    <td className="py-2 pr-4">{r.produceStatus ?? r.status}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {tab === 'inspections' && (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b border-slate-200 text-slate-600">
                  <th className="py-2 pr-4">ID</th>
                  <th className="py-2 pr-4">Produce</th>
                  <th className="py-2 pr-4">Inspector</th>
                  <th className="py-2 pr-4">Status</th>
                  <th className="py-2 pr-4">Score</th>
                  <th className="py-2 pr-4">Grade</th>
                  <th className="py-2 pr-4">Inspection date</th>
                </tr>
              </thead>
              <tbody>
                {inspections.map((row) => (
                  <tr key={row.id} className="border-b border-slate-100">
                    <td className="py-2 pr-4">{row.id}</td>
                    <td className="py-2 pr-4">#{row.farmProduce?.id}</td>
                    <td className="py-2 pr-4">{row.inspector?.name}</td>
                    <td className="py-2 pr-4">{row.inspectionStatus}</td>
                    <td className="py-2 pr-4">{row.qualityScore ?? row.score ?? '—'}</td>
                    <td className="py-2 pr-4">{(row.assignedGrade ?? row.qualityGrade)?.code ?? '—'}</td>
                    <td className="py-2 pr-4">
                      {(row.inspectionDate ?? row.inspectedAt)
                        ? String(row.inspectionDate ?? row.inspectedAt)
                            .replace('T', ' ')
                            .slice(0, 19)
                        : '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {inspections.length === 0 && <p className="text-slate-500">No inspections yet.</p>}
          </div>
        )}

        {tab === 'inventory' && (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b border-slate-200 text-slate-600">
                  <th className="py-2 pr-4">ID</th>
                  <th className="py-2 pr-4">Category</th>
                  <th className="py-2 pr-4">Available</th>
                  <th className="py-2 pr-4">Status</th>
                  <th className="py-2 pr-4">Last updated</th>
                </tr>
              </thead>
              <tbody>
                {inventory.map((row) => (
                  <tr key={row.id} className="border-b border-slate-100">
                    <td className="py-2 pr-4">{row.id}</td>
                    <td className="py-2 pr-4">{row.category?.categoryName ?? row.category?.name}</td>
                    <td className="py-2 pr-4">{row.availableQuantity}</td>
                    <td className="py-2 pr-4">{row.inventoryStatus}</td>
                    <td className="py-2 pr-4">
                      {row.lastUpdated
                        ? String(row.lastUpdated).replace('T', ' ').slice(0, 19)
                        : '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {inventory.length === 0 && <p className="text-slate-500">No inventory rows yet (created after procurement).</p>}
          </div>
        )}

        {tab === 'procurement' && (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b border-slate-200 text-slate-600">
                  <th className="py-2 pr-4">ID</th>
                  <th className="py-2 pr-4">Status</th>
                  <th className="py-2 pr-4">Produce</th>
                  <th className="py-2 pr-4">Officer</th>
                  <th className="py-2 pr-4">Order date</th>
                  <th className="py-2 pr-4">Total</th>
                </tr>
              </thead>
              <tbody>
                {orders.map((o) => (
                  <tr key={o.id} className="border-b border-slate-100">
                    <td className="py-2 pr-4">{o.id}</td>
                    <td className="py-2 pr-4">{o.orderStatus}</td>
                    <td className="py-2 pr-4">#{o.farmProduce?.id}</td>
                    <td className="py-2 pr-4">{(o.procurementOfficer ?? o.officer)?.name}</td>
                    <td className="py-2 pr-4">
                      {o.orderDate ? String(o.orderDate).replace('T', ' ').slice(0, 19) : '—'}
                    </td>
                    <td className="py-2 pr-4">{o.totalAmount}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}
