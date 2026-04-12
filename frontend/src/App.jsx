import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import LoginPage from './pages/LoginPage.jsx'
import FarmerDashboard from './pages/FarmerDashboard.jsx'
import InspectorDashboard from './pages/InspectorDashboard.jsx'
import ProcurementDashboard from './pages/ProcurementDashboard.jsx'
import AdminDashboard from './pages/AdminDashboard.jsx'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LoginPage />} />
        <Route path="/farmer" element={<FarmerDashboard />} />
        <Route path="/inspector" element={<InspectorDashboard />} />
        <Route path="/procurement" element={<ProcurementDashboard />} />
        <Route path="/admin" element={<AdminDashboard />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
