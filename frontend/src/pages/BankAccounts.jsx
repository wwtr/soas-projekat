import { useEffect, useState } from 'react'
import api, { errorMessage } from '../api.js'
import { useAuth } from '../auth.jsx'
import Message from '../components/Message.jsx'

const prazan = { email: '', currencyCode: '', amount: '' }

export default function BankAccounts() {
  const { user } = useAuth()
  const jeAdmin = user.role === 'ADMIN'

  const [racuni, setRacuni] = useState([])
  const [forma, setForma] = useState(prazan)
  const [izmena, setIzmena] = useState(null)
  const [poruka, setPoruka] = useState('')
  const [greska, setGreska] = useState('')

  useEffect(() => {
    ucitaj()
  }, [])

  async function ucitaj() {
    try {
      const putanja = jeAdmin ? '/bank-account' : `/bank-account/user/${user.email}`
      const odgovor = await api.get(putanja)
      setRacuni(odgovor.data)
      setGreska('')
    } catch (err) {
      setRacuni([])
      setGreska(errorMessage(err))
    }
  }

  async function sacuvaj(event) {
    event.preventDefault()
    setPoruka('')
    setGreska('')
    try {
      if (izmena) {
        await api.put(`/bank-account/${izmena}`, { amount: forma.amount })
        setPoruka('Racun je azuriran')
      } else {
        await api.post('/bank-account', forma)
        setPoruka('Racun je dodat')
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
      await api.delete(`/bank-account/${id}`)
      setPoruka('Racun je obrisan')
      ucitaj()
    } catch (err) {
      setGreska(errorMessage(err))
    }
  }

  return (
    <div>
      <h2>Bankovni racuni</h2>
      <Message text={poruka} type="ok" />
      <Message text={greska} type="error" />

      {jeAdmin && (
        <div className="card">
          <h3>{izmena ? 'Izmena racuna' : 'Novi racun'}</h3>
          <form onSubmit={sacuvaj}>
            <div className="row">
              <div>
                <label>Email korisnika</label>
                <input
                  value={forma.email}
                  onChange={(e) => setForma({ ...forma, email: e.target.value })}
                  disabled={izmena !== null}
                  required
                />
              </div>
              <div>
                <label>Valuta</label>
                <input
                  value={forma.currencyCode}
                  onChange={(e) => setForma({ ...forma, currencyCode: e.target.value })}
                  disabled={izmena !== null}
                  required
                />
              </div>
              <div>
                <label>Kolicina</label>
                <input
                  type="number"
                  step="0.01"
                  value={forma.amount}
                  onChange={(e) => setForma({ ...forma, amount: e.target.value })}
                  required
                />
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
        </div>
      )}

      <div className="card">
        <table>
          <thead>
            <tr>
              <th>Id</th>
              <th>Email</th>
              <th>Valuta</th>
              <th>Kolicina</th>
              {jeAdmin && <th></th>}
            </tr>
          </thead>
          <tbody>
            {racuni.map((r) => (
              <tr key={r.id}>
                <td>{r.id}</td>
                <td>{r.email}</td>
                <td>{r.currencyCode}</td>
                <td>{r.amount}</td>
                {jeAdmin && (
                  <td style={{ textAlign: 'right' }}>
                    <button className="secondary" onClick={() => { setIzmena(r.id); setForma({ email: r.email, currencyCode: r.currencyCode, amount: r.amount }) }}>
                      Izmeni
                    </button>
                    <button className="danger" style={{ marginLeft: 8 }} onClick={() => obrisi(r.id)}>Obrisi</button>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
        {racuni.length === 0 && <p style={{ color: '#667085', fontSize: 14 }}>Nema podataka za prikaz.</p>}
      </div>
    </div>
  )
}
