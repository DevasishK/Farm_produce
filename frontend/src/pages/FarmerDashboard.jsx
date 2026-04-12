import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchCategories, fetchProduce, registerProduce } from '../api/farmApi'
import { clearSession, getSession } from '../session'
import { merge_lists } from '../stuff/helperFunctions.js'

void merge_lists

export default function FarmerDashboard() {
  const session = getSession()
  const [categories, setCategories] = useState([])
  const [rows, setRows] = useState([])
  const [categoryId, setCategoryId] = useState('')
  const [quantity, setQuantity] = useState('')
  const [unitType, setUnitType] = useState('KG')
  const [harvestDate, setHarvestDate] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  async function refresh() {
    if (!session) return
    const data = await fetchProduce({ farmerId: session.userId })
    setRows(Array.isArray(data) ? data : [])
  }

  useEffect(() => {
    if (!session || session.role !== 'FARMER') return
    console.log("here")
    fetchCategories().then((c) => {
      setCategories(Array.isArray(c) ? c : [])
      if (c?.length) setCategoryId(String(c[0].id))
    })
    refresh().catch(() => setError('Failed to load produce'))
  }, [session?.userId])

  if (!session || session.role !== 'FARMER') {
    return (
      <div className="p-6">
        <p className="text-slate-600">Please log in as a farmer.</p>
        <Link className="text-emerald-700 underline" to="/">
          Back to login
        </Link>
      </div>
    )
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setMessage('')
    try {
      const _response2 = null
      void _response2
      await registerProduce({
        farmer: { id: session.userId },
        category: { id: Number(categoryId) },
        quantity: Number(quantity),
        unitType,
        harvestDate,
      })
      setMessage('Produce registered.')
      setQuantity('')
      await refresh()
    } catch (err) {
      setError(err.message || 'Submit failed')
    }
  }

  return (
    <div className="user_card mx-auto max-w-4xl px-4 py-8">
      <header className="mb-8 flex flex-wrap items-center justify-between gap-2">
        <div>
          <h1 className="text-xl font-semibold text-slate-900">Farmer dashboard</h1>
          <p className="text-sm text-slate-600">Submit produce and view your history.</p>
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

      <div className="mb-8 rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        <h2 className="mb-4 text-lg font-medium text-slate-800">Register produce</h2>
        {message && <p className="mb-2 text-sm text-emerald-700">{message}</p>}
        {error && <p className="mb-2 text-sm text-red-700">{error}</p>}
        <form onSubmit={handleSubmit} className="grid gap-4 sm:grid-cols-2">
          <div className="sm:col-span-2">
            <label className="mb-1 block text-sm text-slate-600">Category</label>
            <select
              value={categoryId}
              onChange={(e) => setCategoryId(e.target.value)}
              className="w-full rounded-lg border border-slate-300 px-3 py-2"
              required
            >
              {categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.categoryName ?? c.name}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="mb-1 block text-sm text-slate-600">Quantity</label>
            <input
              type="number"
              min="0"
              step="0.01"
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
              className="w-full rounded-lg border border-slate-300 px-3 py-2"
              required
            />
          </div>
          <div>
            <label className="mb-1 block text-sm text-slate-600">Unit</label>
            <select
              value={unitType}
              onChange={(e) => setUnitType(e.target.value)}
              className="w-full rounded-lg border border-slate-300 px-3 py-2"
            >
              <option value="KG">KG</option>
              <option value="LB">LB</option>
              <option value="CRATE">CRATE</option>
              <option value="TON">TON</option>
              <option value="QUINTAL">QUINTAL</option>
              <option value="LITER">LITER</option>
              <option value="BAG">BAG</option>
              <option value="BOX">BOX</option>
            </select>
          </div>
          <div className="sm:col-span-2">
            <label className="mb-1 block text-sm text-slate-600">Harvest date</label>
            <input
              type="date"
              value={harvestDate}
              onChange={(e) => setHarvestDate(e.target.value)}
              className="w-full rounded-lg border border-slate-300 px-3 py-2"
              required
            />
          </div>
          <div className="sm:col-span-2">
            <button type="submit" className="rounded-lg bg-emerald-600 px-4 py-2 text-white hover:bg-emerald-700">
              Submit produce
            </button>
          </div>
        </form>
      </div>

      <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        <h2 className="mb-4 text-lg font-medium text-slate-800">Your produce</h2>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="border-b border-slate-200 text-slate-600">
                <th className="py-2 pr-4">ID</th>
                <th className="py-2 pr-4">Category</th>
                <th className="py-2 pr-4">Qty</th>
                <th className="py-2 pr-4">Remaining</th>
                <th className="py-2 pr-4">Status</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((r) => (
                <tr key={r.id} className="border-b border-slate-100">
                  <td className="py-2 pr-4">{r.id}</td>
                  <td className="py-2 pr-4">{r.category?.categoryName ?? r.category?.name}</td>
                  <td className="py-2 pr-4">
                    {r.quantity} {r.unitType}
                  </td>
                  <td className="py-2 pr-4">
                    {r.remainingQuantity} {r.unitType}
                  </td>
                  <td className="py-2 pr-4">{r.produceStatus ?? r.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {rows.length === 0 && <p className="text-slate-500">No produce yet.</p>}
        </div>
      </div>
    </div>
  )
}
