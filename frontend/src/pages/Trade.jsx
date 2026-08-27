import { useState } from 'react'
import api, { errorMessage } from '../api.js'
import Message from '../components/Message.jsx'

export default function Trade() {
  const [from, setFrom] = useState('EUR')
  const [to, setTo] = useState('ETH')
  const [quantity, setQuantity] = useState('100')
  const [odgovor, setOdgovor] = useState(null)
  const [greska, setGreska] = useState('')
  const [uToku, setUToku] = useState(false)

  async function posalji(event) {
    event.preventDefault()
    setGreska('')
    setOdgovor(null)
    setUToku(true)
    try {
      const rezultat = await api.get('/trade-service', { params: { from, to, quantity } })
      setOdgovor(rezultat.data)
    } catch (err) {
      setGreska(errorMessage(err))
    } finally {
      setUToku(false)
    }
  }

  const stavke = odgovor
    ? (odgovor.cryptoWallet
        ? odgovor.cryptoWallet.map((s) => ({ id: s.id, kod: s.cryptoCode, iznos: s.amount }))
        : odgovor.bankAccount.map((s) => ({ id: s.id, kod: s.currencyCode, iznos: s.amount })))
    : []

  return (
    <div>
      <h2>Trgovina</h2>
      <div className="card">
        <Message text={greska} type="error" />
        <p style={{ fontSize: 13, color: '#667085', marginTop: 0 }}>
          Podrzane su razmene crypto u crypto, fiat u crypto i crypto u fiat.
          Razmena dve fiat valute se radi na stranici Razmena valuta.
        </p>
        <form onSubmit={posalji}>
          <div className="row">
            <div>
              <label>Iz valute</label>
              <input value={from} onChange={(e) => setFrom(e.target.value)} required />
            </div>
            <div>
              <label>U valutu</label>
              <input value={to} onChange={(e) => setTo(e.target.value)} required />
            </div>
            <div>
              <label>Kolicina</label>
              <input type="number" step="0.00000001" value={quantity} onChange={(e) => setQuantity(e.target.value)} required />
            </div>
            <div style={{ flex: '0 0 auto' }}>
              <button type="submit" disabled={uToku}>{uToku ? 'U toku...' : 'Trguj'}</button>
            </div>
          </div>
        </form>
      </div>

      {odgovor && (
        <div className="card">
          <Message text={odgovor.message} type="ok" />
          <h3>{odgovor.cryptoWallet ? 'Stanje novcanika' : 'Stanje racuna'}</h3>
          <table>
            <thead>
              <tr>
                <th>Valuta</th>
                <th>Kolicina</th>
              </tr>
            </thead>
            <tbody>
              {stavke.map((s) => (
                <tr key={s.id}>
                  <td>{s.kod}</td>
                  <td>{s.iznos}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
