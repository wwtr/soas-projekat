import { useState } from 'react'
import api, { errorMessage } from '../api.js'
import Message from '../components/Message.jsx'

export default function Conversion() {
  const [from, setFrom] = useState('EUR')
  const [to, setTo] = useState('RSD')
  const [quantity, setQuantity] = useState('100')
  const [odgovor, setOdgovor] = useState(null)
  const [greska, setGreska] = useState('')

  async function posalji(event) {
    event.preventDefault()
    setGreska('')
    setOdgovor(null)
    try {
      const rezultat = await api.get('/currency-conversion', { params: { from, to, quantity } })
      setOdgovor(rezultat.data)
    } catch (err) {
      setGreska(errorMessage(err))
    }
  }

  return (
    <div>
      <h2>Razmena fiat valuta</h2>
      <div className="card">
        <Message text={greska} type="error" />
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
              <input type="number" step="0.01" value={quantity} onChange={(e) => setQuantity(e.target.value)} required />
            </div>
            <div style={{ flex: '0 0 auto' }}>
              <button type="submit">Zameni</button>
            </div>
          </div>
        </form>
      </div>

      {odgovor && (
        <div className="card">
          <Message text={odgovor.message} type="ok" />
          <h3>Stanje racuna</h3>
          <table>
            <thead>
              <tr>
                <th>Valuta</th>
                <th>Kolicina</th>
              </tr>
            </thead>
            <tbody>
              {odgovor.bankAccount.map((r) => (
                <tr key={r.id}>
                  <td>{r.currencyCode}</td>
                  <td>{r.amount}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
