import { useEffect, useState } from 'react'
import api, { errorMessage } from '../api.js'
import { useAuth } from '../auth.jsx'
import Message from '../components/Message.jsx'

const prazan = { email: '', password: '', role: 'USER' }

export default function Users() {
  const { user } = useAuth()
  const [korisnici, setKorisnici] = useState([])
  const [forma, setForma] = useState(prazan)
  const [izmena, setIzmena] = useState(null)
  const [poruka, setPoruka] = useState('')
  const [greska, setGreska] = useState('')

  useEffect(() => {
    ucitaj()
  }, [])

  async function ucitaj() {
    try {
      const odgovor = await api.get('/users')
      setKorisnici(odgovor.data)
      setGreska('')
    } catch (err) {
      setGreska(errorMessage(err))
    }
  }

  async function sacuvaj(event) {
    event.preventDefault()
    setPoruka('')
    setGreska('')
    try {
      if (izmena) {
        await api.put(`/users/${izmena}`, { password: forma.password, role: forma.role })
        setPoruka('Korisnik je azuriran')
      } else {
        await api.post('/users', forma)
        setPoruka('Korisnik je dodat, racun i novcanik su otvoreni automatski')
      }
      setForma(prazan)
      setIzmena(null)
      ucitaj()
    } catch (err) {
      setGreska(errorMessage(err))
    }
  }

  async function obrisi(id) {
    setPoruka('')
    setGreska('')
    try {
      await api.delete(`/users/${id}`)
      setPoruka('Korisnik je obrisan zajedno sa racunom i novcanikom')
      ucitaj()
    } catch (err) {
      setGreska(errorMessage(err))
    }
  }

  function pripremiIzmenu(k) {
    setIzmena(k.id)
    setForma({ email: k.email, password: '', role: k.role })
  }

  return (
    <div>
      <h2>Korisnici</h2>
      <Message text={poruka} type="ok" />
      <Message text={greska} type="error" />

      <div className="card">
        <h3>{izmena ? 'Izmena korisnika' : 'Novi korisnik'}</h3>
        <form onSubmit={sacuvaj}>
          <div className="row">
            <div>
              <label>Email</label>
              <input
                value={forma.email}
                onChange={(e) => setForma({ ...forma, email: e.target.value })}
                disabled={izmena !== null}
                required
              />
            </div>
            <div>
              <label>Lozinka</label>
              <input
                type="password"
                value={forma.password}
                onChange={(e) => setForma({ ...forma, password: e.target.value })}
                placeholder={izmena ? 'ostavi prazno da ne menjas' : ''}
                required={izmena === null}
              />
            </div>
            <div>
              <label>Uloga</label>
              <select value={forma.role} onChange={(e) => setForma({ ...forma, role: e.target.value })}>
                <option value="USER">USER</option>
                <option value="ADMIN" disabled={user.role !== 'OWNER'}>ADMIN</option>
                <option value="OWNER" disabled={user.role !== 'OWNER'}>OWNER</option>
              </select>
            </div>
            <div style={{ flex: '0 0 auto' }}>
              <button type="submit">{izmena ? 'Sacuvaj' : 'Dodaj'}</button>
            </div>
            {izmena && (
              <div style={{ flex: '0 0 auto' }}>
                <button type="button" className="secondary" onClick={() => { setIzmena(null); setForma(prazan) }}>
                  Odustani
                </button>
              </div>
            )}
          </div>
        </form>
        {izmena && <p style={{ fontSize: 13, color: '#667085' }}>Email adresa postojeceg korisnika ne moze da se menja.</p>}
      </div>

      <div className="card">
        <table>
          <thead>
            <tr>
              <th>Id</th>
              <th>Email</th>
              <th>Uloga</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {korisnici.map((k) => (
              <tr key={k.id}>
                <td>{k.id}</td>
                <td>{k.email}</td>
                <td>{k.role}</td>
                <td style={{ textAlign: 'right' }}>
                  <button className="secondary" onClick={() => pripremiIzmenu(k)}>Izmeni</button>
                  {user.role === 'OWNER' && (
                    <button className="danger" style={{ marginLeft: 8 }} onClick={() => obrisi(k.id)}>Obrisi</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
