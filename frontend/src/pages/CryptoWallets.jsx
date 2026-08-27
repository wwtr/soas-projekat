import { useEffect, useState } from 'react'
import api, { errorMessage } from '../api.js'
import { useAuth } from '../auth.jsx'
import Message from '../components/Message.jsx'

const prazan = { email: '', cryptoCode: '', amount: '' }

export default function CryptoWallets() {
  const { user } = useAuth()
  const jeAdmin = user.role === 'ADMIN'

  const [novcanici, setNovcanici] = useState([])
  const [forma, setForma] = useState(prazan)
  const [izmena, setIzmena] = useState(null)
  const [poruka, setPoruka] = useState('')
  const [greska, setGreska] = useState('')

  useEffect(() => {
    ucitaj()
  }, [])

  async function ucitaj() {
    try {
      const putanja = jeAdmin ? '/crypto-wallet' : `/crypto-wallet/user/${user.email}`
      const odgovor = await api.get(putanja)
      setNovcanici(odgovor.data)
      setGreska('')
    } catch (err) {
      setNovcanici([])
      setGreska(errorMessage(err))
    }
  }

  async function sacuvaj(event) {
    event.preventDefault()
    setPoruka('')
    setGreska('')
    try {
      if (izmena) {
        await api.put(`/crypto-wallet/${izmena}`, { amount: forma.amount })
        setPoruka('Novcanik je azuriran')
      } else {
        await api.post('/crypto-wallet', forma)
        setPoruka('Novcanik je dodat')
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
      await api.delete(`/crypto-wallet/${id}`)
      setPoruka('Novcanik je obrisan')
      ucitaj()
    } catch (err) {
      setGreska(errorMessage(err))
    }
  }

  return (
    <div>
      <h2>Crypto novcanici</h2>
      <Message text={poruka} type="ok" />
      <Message text={greska} type="error" />

      {jeAdmin && (
        <div className="card">
          <h3>{izmena ? 'Izmena novcanika' : 'Novi novcanik'}</h3>
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
                <label>Crypto valuta</label>
                <input
                  value={forma.cryptoCode}
                  onChange={(e) => setForma({ ...forma, cryptoCode: e.target.value })}
                  disabled={izmena !== null}
                  required
                />
              </div>
              <div>
                <label>Kolicina</label>
                <input
                  type="number"
                  step="0.00000001"
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
              <th>Crypto valuta</th>
              <th>Kolicina</th>
              {jeAdmin && <th></th>}
            </tr>
          </thead>
          <tbody>
            {novcanici.map((r) => (
              <tr key={r.id}>
                <td>{r.id}</td>
                <td>{r.email}</td>
                <td>{r.cryptoCode}</td>
                <td>{r.amount}</td>
                {jeAdmin && (
                  <td style={{ textAlign: 'right' }}>
                    <button className="secondary" onClick={() => { setIzmena(r.id); setForma({ email: r.email, cryptoCode: r.cryptoCode, amount: r.amount }) }}>
                      Izmeni
                    </button>
                    <button className="danger" style={{ marginLeft: 8 }} onClick={() => obrisi(r.id)}>Obrisi</button>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
        {novcanici.length === 0 && <p style={{ color: '#667085', fontSize: 14 }}>Nema podataka za prikaz.</p>}
      </div>
    </div>
  )
}
