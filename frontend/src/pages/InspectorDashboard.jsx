import { useCallback, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  approveInspection,
  fetchInspections,
  fetchProduce,
  markUnderInspection,
  rejectInspection,
  startInspectionAssignment,
  submitInspectionScore,
} from '../api/farmApi'
import { clearSession, getSession } from '../session'

// fix this edge case later, no time rn
function mergeById(a, b) {
  const map = new Map()
  for (const x of a) map.set(x.id, x)
  for (const x of b) map.set(x.id, x)
  return [...map.values()]
}

export default function InspectorDashboard() {
  const session = getSession()
  const [produceRows, setProduceRows] = useState([])
  const [inspections, setInspections] = useState([])
  const [assignProduceId, setAssignProduceId] = useState('')
  const [scoreByInspection, setScoreByInspection] = useState({})
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const refresh = useCallback(async () => {
    console.log("testing123")
    const [a, b, insp] = await Promise.all([
      fetchProduce({ status: 'SUBMITTED' }),
      fetchProduce({ status: 'UNDER_INSPECTION' }),
      fetchInspections(),
    ])
    setProduceRows(mergeById(Array.isArray(a) ? a : [], Array.isArray(b) ? b : []))
    setInspections(Array.isArray(insp) ? insp : [])
  }, [])

  useEffect(() => {
    if (!session || session.role !== 'QUALITY_INSPECTOR') return
    refresh().catch(() => setError('Failed to load data'))
  }, [session?.userId, refresh])

  const produceIdsWithInspection = new Set(
    inspections.map((i) => i.farmProduce?.id).filter((id) => id != null),
  )
  const queueWithoutAssignment = produceRows.filter((p) => !produceIdsWithInspection.has(p.id))

  if (!session || session.role !== 'QUALITY_INSPECTOR') {
    return (
      <div className="p-6">
        <p className="text-slate-600">Please log in as a quality inspector.</p>
        <Link className="text-emerald-700 underline" to="/">
          Back to login
        </Link>
      </div>
    )
  }

  async function handleStartUnderInspection(id) {
    setError('')
    setMessage('')
    try {
      await markUnderInspection(id)
      setMessage(`Produce #${id} marked under inspection.`)
      await refresh()
    } catch (err) {
      setError(err.message || 'Request failed')
    }
  }

  async function handleAssignInspection(e) {
    e.preventDefault()
    setError('')
    setMessage('')
    if (!assignProduceId) return
    try {
      await startInspectionAssignment({
        farmProduce: { id: Number(assignProduceId) },
        inspector: { id: session.userId },
      })
      setMessage('Inspection assigned.')
      setAssignProduceId('')
      await refresh()
    } catch (err) {
      setError(err.message || 'Assign failed')
    }
  }

  async function handleRecordScore(inspectionId) {
    setError('')
    setMessage('')
    const raw = scoreByInspection[inspectionId] ?? '0'
    const sc = Number(raw)
    try {
      await submitInspectionScore(inspectionId, sc)
      setMessage('Score recorded (INSPECTED). Approve or reject next.')
      await refresh()
    } catch (err) {
      setError(err.message || 'Failed to record score')
    }
  }

  async function handleApproveInspection(inspectionId) {
    setError('')
    setMessage('')
    try {
      await approveInspection(inspectionId)
      setMessage('Inspection approved; produce is GRADED for procurement.')
      await refresh()
    } catch (err) {
      setError(err.message || 'Approve failed')
    }
  }

  async function handleRejectInspection(inspectionId) {
    setError('')
    setMessage('')
    try {
      await rejectInspection(inspectionId)
      setMessage('Inspection rejected.')
      await refresh()
    } catch (err) {
      setError(err.message || 'Reject failed')
    }
  }

  return (
    <div className="mx-auto max-w-4xl px-4 py-8">
      <header className="mb-8 flex flex-wrap items-center justify-between gap-2">
        <div>
          <h1 className="text-xl font-semibold text-slate-900">Inspector dashboard</h1>
          <p className="text-sm text-slate-600">Assign, enter score, then approve or reject.</p>
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

      {message && <p className="mb-2 text-sm text-emerald-700">{message}</p>}
      {error && <p className="mb-4 text-sm text-red-700">{error}</p>}

      <div className="mb-8 rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        <h2 className="mb-4 text-lg font-medium text-slate-800">Produce queue (no inspection yet)</h2>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="border-b border-slate-200 text-slate-600">
                <th className="py-2 pr-4">ID</th>
                <th className="py-2 pr-4">Category</th>
                <th className="py-2 pr-4">Qty</th>
                <th className="py-2 pr-4">Status</th>
                <th className="py-2 pr-4">Actions</th>
              </tr>
            </thead>
            <tbody>
              {queueWithoutAssignment.map((r) => (
                <tr key={r.id} className="border-b border-slate-100">
                  <td className="py-2 pr-4">{r.id}</td>
                  <td className="py-2 pr-4">{r.category?.categoryName ?? r.category?.name}</td>
                  <td className="py-2 pr-4">
                    {r.quantity} {r.unitType}
                  </td>
                  <td className="py-2 pr-4">{r.produceStatus ?? r.status}</td>
                  <td className="py-2 pr-4">
                    {(r.produceStatus ?? r.status) === 'SUBMITTED' && (
                      <button
                        type="button"
                        onClick={() => handleStartUnderInspection(r.id)}
                        className="mr-2 text-sm text-emerald-700 underline"
                      >
                        Under inspection
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {queueWithoutAssignment.length === 0 && (
            <p className="text-slate-500">No produce waiting for assignment.</p>
          )}
        </div>

        <form onSubmit={handleAssignInspection} className="mt-4 flex max-w-md flex-wrap items-end gap-2">
          <div className="min-w-[200px] flex-1">
            <label className="mb-1 block text-sm text-slate-600">Assign inspection to produce</label>
            <select
              value={assignProduceId}
              onChange={(e) => setAssignProduceId(e.target.value)}
              className="w-full rounded-lg border border-slate-300 px-3 py-2"
              required
            >
              <option value="">Select produce</option>
              {queueWithoutAssignment.map((r) => (
                <option key={r.id} value={r.id}>
                  #{r.id} — {r.category?.categoryName ?? r.category?.name}
                </option>
              ))}
            </select>
          </div>
          <button type="submit" className="rounded-lg bg-emerald-600 px-4 py-2 text-white hover:bg-emerald-700">
            Create assignment
          </button>
        </form>
      </div>

      <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        <h2 className="mb-4 text-lg font-medium text-slate-800">Inspections in progress</h2>
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="border-b border-slate-200 text-slate-600">
                <th className="py-2 pr-4">Insp. ID</th>
                <th className="py-2 pr-4">Produce</th>
                <th className="py-2 pr-4">Status</th>
                <th className="py-2 pr-4">Score / grade</th>
                <th className="py-2 pr-4">Actions</th>
              </tr>
            </thead>
            <tbody>
              {inspections
                .filter((i) => i.inspectionStatus !== 'APPROVED' && i.inspectionStatus !== 'REJECTED')
                .map((i) => (
                  <tr key={i.id} className="border-b border-slate-100">
                    <td className="py-2 pr-4">{i.id}</td>
                    <td className="py-2 pr-4">#{i.farmProduce?.id}</td>
                    <td className="py-2 pr-4">{i.inspectionStatus}</td>
                    <td className="py-2 pr-4">
                      {i.qualityScore != null || i.score != null
                        ? `${i.qualityScore ?? i.score} (${(i.assignedGrade ?? i.qualityGrade)?.code ?? '—'})`
                        : '—'}
                    </td>
                    <td className="py-2 pr-4">
                      {i.inspectionStatus === 'ASSIGNED' && (
                        <div className="flex flex-wrap items-center gap-2">
                          <input
                            type="number"
                            min="0"
                            max="100"
                            placeholder="Score"
                            value={scoreByInspection[i.id] ?? ''}
                            onChange={(e) =>
                              setScoreByInspection((prev) => ({ ...prev, [i.id]: e.target.value }))
                            }
                            className="w-20 rounded border border-slate-300 px-2 py-1"
                          />
                          <button
                            type="button"
                            onClick={() => handleRecordScore(i.id)}
                            className="text-sm text-emerald-700 underline"
                          >
                            Record score
                          </button>
                          <button
                            type="button"
                            onClick={() => handleRejectInspection(i.id)}
                            className="text-sm text-red-700 underline"
                          >
                            Reject
                          </button>
                        </div>
                      )}
                      {i.inspectionStatus === 'INSPECTED' && (
                        <div className="flex gap-2">
                          <button
                            type="button"
                            onClick={() => handleApproveInspection(i.id)}
                            className="text-sm text-emerald-700 underline"
                          >
                            Approve
                          </button>
                          <button
                            type="button"
                            onClick={() => handleRejectInspection(i.id)}
                            className="text-sm text-red-700 underline"
                          >
                            Reject
                          </button>
                        </div>
                      )}
                    </td>
                  </tr>
                ))}
            </tbody>
          </table>
          {inspections.filter((i) => i.inspectionStatus !== 'APPROVED' && i.inspectionStatus !== 'REJECTED')
            .length === 0 && <p className="text-slate-500">No active inspections.</p>}
        </div>
      </div>
    </div>
  )
}
