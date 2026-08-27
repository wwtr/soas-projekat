import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth.jsx'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  function odjava() {
    logout()
    navigate('/login')
  }

  const jeAdmin = user && user.role === 'ADMIN'
  const jeVlasnik = user && user.role === 'OWNER'
  const jeKorisnik = user && user.role === 'USER'

  return (
    <nav>
      <NavLink to="/kursevi">Kursevi</NavLink>
      {(jeAdmin || jeVlasnik) && <NavLink to="/korisnici">Korisnici</NavLink>}
      {(jeAdmin || jeKorisnik) && <NavLink to="/racuni">Bankovni racuni</NavLink>}
      {(jeAdmin || jeKorisnik) && <NavLink to="/novcanici">Crypto novcanici</NavLink>}
      {jeKorisnik && <NavLink to="/razmena">Razmena valuta</NavLink>}
      {jeKorisnik && <NavLink to="/trgovina">Trgovina</NavLink>}
      <span className="spacer">
        {user ? `${user.email} (${user.role})` : 'niste prijavljeni'}
      </span>
      {user
        ? <button className="secondary" onClick={odjava}>Odjava</button>
        : <button className="secondary" onClick={() => navigate('/login')}>Prijava</button>}
    </nav>
  )
}
