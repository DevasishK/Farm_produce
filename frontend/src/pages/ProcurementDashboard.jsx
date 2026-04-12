import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  approveOrder,
  cancelOrder,
  completeOrder,
  fetchProcurementOrders,
  fetchProduce,
  submitProcurement,
} from '../api/farmApi'
import { clearSession, getSession } from '../session'

export default function ProcurementDashboard() {
  const session = getSession()
  const [gradedProduce, setGradedProduce] = useState([])
  const [orders, setOrders] = useState([])
  const [produceId, setProduceId] = useState('')
  const [qty, setQty] = useState('')
  const [unitPrice, setUnitPrice] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const refresh = useCallback(async () => {
    const [p, o] = await Promise.all([
      fetchProduce({ status: 'GRADED' }),
      fetchProcurementOrders(),
    ])
    setGradedProduce(Array.isArray(p) ? p : [])
    setOrders(Array.isArray(o) ? o : [])
  }, [])

  useEffect(() => {
    if (!session || session.role !== 'PROCUREMENT_OFFICER') return
    refresh().catch((err) => {
      console.log(err)
      setError('Failed to load data')
    })
  }, [session?.userId, refresh])

  if (!session || session.role !== 'PROCUREMENT_OFFICER') {
    return (
      <div className="p-6">
        <p className="text-slate-600">Please log in as a procurement officer.</p>
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
    const p = gradedProduce.find((x) => String(x.id) === String(produceId))
    try {
      await submitProcurement({
        farmProduce: { id: Number(produceId) },
        officer: { id: session.userId },
        procurementQuantity: Number(qty),
        unitPrice: Number(unitPrice),
        unitType: p?.unitType,
      })
      setMessage('Order created (CREATED). Approve, then Complete to receive stock.')
      setQty('')
      setUnitPrice('')
      await refresh()
    } catch (err) {
      setError(err.message || 'Procurement failed')
    }
  }

  return (
    <div className="mx-auto max-w-4xl px-4 py-8">
      <header className="mb-8 flex flex-wrap items-center justify-between gap-2">
        <div>
          <h1 className="text-xl font-semibold text-slate-900">Procurement dashboard</h1>
          <p className="text-sm text-slate-600">
            Graded produce only. Approve the order, then complete to update stock.
          </p>
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
        <h2 className="mb-4 text-lg font-medium text-slate-800">New procurement order</h2>
        {message && <p className="mb-2 text-sm text-emerald-700">{message}</p>}
        {error && <p className="mb-2 text-sm text-red-700">{error}</p>}
        <form onSubmit={handleSubmit} className="grid max-w-lg gap-4 sm:grid-cols-2">
          <div className="sm:col-span-2">
            <label className="mb-1 block text-sm text-slate-600">Graded produce</label>
            <select
              value={produceId}
              onChange={(e) => setProduceId(e.target.value)}
              className="w-full rounded-lg border border-slate-300 px-3 py-2"
              required
            >
              <option value="">Select produce</option>
              {gradedProduce.map((r) => (
                <option key={r.id} value={r.id}>
                  #{r.id} — {r.category?.categoryName ?? r.category?.name} (remaining {r.remainingQuantity}{' '}
                  {r.unitType})
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
              value={qty}
              onChange={(e) => setQty(e.target.value)}
              className="w-full rounded-lg border border-slate-300 px-3 py-2"
              required
            />
          </div>
          <div>
            <label className="mb-1 block text-sm text-slate-600">Unit price</label>
            <input
              type="number"
              min="0"
              step="0.01"
              value={unitPrice}
              onChange={(e) => setUnitPrice(e.target.value)}
              className="w-full rounded-lg border border-slate-300 px-3 py-2"
              required
            />
          </div>
          <div className="sm:col-span-2">
            <button type="submit" className="rounded-lg bg-emerald-600 px-4 py-2 text-white hover:bg-emerald-700">
              Create order (CREATED)
            </button>
          </div>
        </form>
      </div>

      <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        <h2 className="mb-4 text-lg font-medium text-slate-800">Orders</h2>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="border-b border-slate-200 text-slate-600">
                <th className="py-2 pr-4">ID</th>
                <th className="py-2 pr-4">Status</th>
                <th className="py-2 pr-4">Produce</th>
                <th className="py-2 pr-4">Qty</th>
                <th className="py-2 pr-4">Unit price</th>
                <th className="py-2 pr-4">Total</th>
                <th className="py-2 pr-4">Actions</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((o) => (
                <tr key={o.id} className="border-b border-slate-100">
                  <td className="py-2 pr-4">{o.id}</td>
                  <td className="py-2 pr-4">{o.orderStatus}</td>
                  <td className="py-2 pr-4">#{o.farmProduce?.id}</td>
                  <td className="py-2 pr-4">
                    {o.procurementQuantity} {o.unitType}
                  </td>
                  <td className="py-2 pr-4">{o.unitPrice}</td>
                  <td className="py-2 pr-4">{o.totalAmount}</td>
                  <td className="py-2 pr-4">
                    {o.orderStatus === 'CREATED' && (
                      <div className="flex flex-wrap gap-2">
                        <button
                          type="button"
                          onClick={async () => {
                            setError('')
                            try {
                              await approveOrder(o.id)
                              setMessage(`Order #${o.id} approved.`)
                              await refresh()
                            } catch (err) {
                              setError(err.message)
                            }
                          }}
                          className="text-xs text-emerald-700 underline"
                        >
                          Approve
                        </button>
                        <button
                          type="button"
                          onClick={async () => {
                            setError('')
                            try {
                              await cancelOrder(o.id)
                              setMessage(`Order #${o.id} cancelled.`)
                              await refresh()
                            } catch (err) {
                              setError(err.message)
                            }
                          }}
                          className="text-xs text-red-700 underline"
                        >
                          Cancel
                        </button>
                      </div>
                    )}
                    {o.orderStatus === 'APPROVED' && (
                      <div className="flex flex-wrap gap-2">
                        <button
                          type="button"
                          onClick={async () => {
                            setError('')
                            try {
                              await completeOrder(o.id)
                              setMessage(`Order #${o.id} completed (inventory updated).`)
                              await refresh()
                            } catch (err) {
                              setError(err.message)
                            }
                          }}
                          className="text-xs text-emerald-700 underline"
                        >
                          Complete
                        </button>
                        <button
                          type="button"
                          onClick={async () => {
                            setError('')
                            try {
                              await cancelOrder(o.id)
                              setMessage(`Order #${o.id} cancelled.`)
                              await refresh()
                            } catch (err) {
                              setError(err.message)
                            }
                          }}
                          className="text-xs text-red-700 underline"
                        >
                          Cancel
                        </button>
                      </div>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {orders.length === 0 && <p className="text-slate-500">No orders yet.</p>}
        </div>
      </div>
    </div>
  )
}
