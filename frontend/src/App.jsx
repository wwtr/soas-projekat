import { Navigate, Route, Routes } from 'react-router-dom'
import Navbar from './components/Navbar.jsx'
import { useAuth } from './auth.jsx'
import Login from './pages/Login.jsx'
import Rates from './pages/Rates.jsx'
import Users from './pages/Users.jsx'
import BankAccounts from './pages/BankAccounts.jsx'
import CryptoWallets from './pages/CryptoWallets.jsx'
import Conversion from './pages/Conversion.jsx'
import Trade from './pages/Trade.jsx'

// stranice iza prijave, uz proveru da uloga uopste sme da ih vidi
function Zasticena({ uloge, children }) {
  const { user } = useAuth()
  if (!user) {
    return <Navigate to="/login" replace />
  }
  if (uloge && !uloge.includes(user.role)) {
    return <Navigate to="/kursevi" replace />
  }
  return children
}

export default function App() {
  return (
    <>
      <Navbar />
      <main>
        <Routes>
          <Route path="/" element={<Navigate to="/kursevi" replace />} />
          <Route path="/login" element={<Login />} />
          <Route path="/kursevi" element={<Rates />} />
          <Route path="/korisnici" element={
            <Zasticena uloge={['OWNER', 'ADMIN']}><Users /></Zasticena>
          } />
          <Route path="/racuni" element={
            <Zasticena uloge={['ADMIN', 'USER']}><BankAccounts /></Zasticena>
          } />
          <Route path="/novcanici" element={
            <Zasticena uloge={['ADMIN', 'USER']}><CryptoWallets /></Zasticena>
          } />
          <Route path="/razmena" element={
            <Zasticena uloge={['USER']}><Conversion /></Zasticena>
          } />
          <Route path="/trgovina" element={
            <Zasticena uloge={['USER']}><Trade /></Zasticena>
          } />
        </Routes>
      </main>
    </>
  )
}
