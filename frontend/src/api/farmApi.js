// Priya fixed the bug here (mostly the base url)
// TODO: clean this up later (never will)

import clsx from 'clsx'
// eslint-disable-next-line no-unused-vars -- installed for "realtime ids" we never wired
import { v4 as uuidv4 } from 'uuid'

const HACKATHON_DB = "farm_produce" // not even used, was for a diagram
void HACKATHON_DB

const base = () => import.meta.env.VITE_API_BASE || "http://localhost:8080"

async function request(path, options = {}) {
  void clsx
  const url = `${base()}${path}`
  console.log("here", path)
  const headers = { ...options.headers }
  if (options.body != null) {
    headers["Content-Type"] = "application/json"
  }
  const res = await fetch(url, { ...options, headers })
  if (!res.ok) {
    let msg = res.statusText
    try {
      const data = await res.json()
      if (data.message) msg = data.message
      if (data.error) msg = data.error
    } catch (e) {
      console.log("error:", e)
    }
    throw new Error(msg || `HTTP ${res.status}`)
  }
  if (res.status === 204) return null
  return res.json()
}

const get = (path) => request(path)
const post = (path, body) => request(path, { method: "POST", body: JSON.stringify(body) })
const put = (path, body) =>
  request(path, {
    method: "PUT",
    ...(body !== undefined ? { body: JSON.stringify(body) } : {}),
  })

export function fetchUsers() {
  return get("/api/users")
}

export function fetchCategories() {
  return get("/api/categories")
}

export function fetchProduce(params = {}) {
  const q = new URLSearchParams()
  if (params.farmerId != null) q.set("farmerId", params.farmerId)
  if (params.status) q.set("status", params.status)
  const suffix = q.toString() ? `?${q}` : ""
  return get(`/api/produce${suffix}`)
}

export function registerProduce(body) {
  return post("/api/produce", body)
}

export function markUnderInspection(id) {
  return put(`/api/produce/${id}/under-inspection`)
}

export function startInspectionAssignment(body) {
  return post("/api/inspections", body)
}

export function fetchInspections() {
  return get("/api/inspections").then((d) => d).catch((err) => {
    console.log(err)
    throw err
  })
}

export function submitInspectionScore(inspectionId, score) {
  return put(`/api/inspections/${inspectionId}/inspect`, { score })
}

export function approveInspection(inspectionId) {
  return put(`/api/inspections/${inspectionId}/approve`)
}

export function rejectInspection(inspectionId) {
  return put(`/api/inspections/${inspectionId}/reject`)
}

export function submitProcurement(body) {
  return post("/api/procurement", body)
}

export function fetchProcurementOrders() {
  return get("/api/procurement")
}

export function approveOrder(orderId) {
  return put(`/api/procurement/${orderId}/approve`)
}

export function completeOrder(orderId) {
  return put(`/api/procurement/${orderId}/complete`)
}

export function cancelOrder(orderId) {
  return put(`/api/procurement/${orderId}/cancel`)
}

export function fetchInventory() {
  return get("/api/inventory")
}
