import { useState } from 'react'
import axios from 'axios'
import { GATEWAY_URL, errorMessage } from '../api.js'
import Message from '../components/Message.jsx'

// kursevi su javni, pa se zovu bez kredencijala
export default function Rates() {
  const [tip, setTip] = useState('fiat')
  const [from, setFrom] = useState('EUR')
  const [to, setTo] = useState('RSD')
  const [rezultat, setRezultat] = useState(null)
  const [greska, setGreska] = useState('')

  async function pretrazi(event) {
    event.preventDefault()
    setGreska('')
    setRezultat(null)
    const putanja = tip === 'fiat' ? 'currency-exchange' : 'crypto-exchange'
    try {
      const odgovor = await axios.get(`${GATEWAY_URL}/${putanja}/from/${from}/to/${to}`)
      setRezultat(odgovor.data)
    } catch (err) {
      setGreska(errorMessage(err))
    }
  }

  return (
    <div>
      <h2>Kursevi</h2>
      <div className="card">
        <Message text={greska} type="error" />
        <form onSubmit={pretrazi}>
          <div className="row">
            <div>
              <label>Tip</label>
              <select value={tip} onChange={(e) => setTip(e.target.value)}>
                <option value="fiat">Fiat valute</option>
                <option value="crypto">Crypto valute</option>
              </select>
            </div>
            <div>
              <label>Iz valute</label>
              <input value={from} onChange={(e) => setFrom(e.target.value)} required />
            </div>
            <div>
              <label>U valutu</label>
              <input value={to} onChange={(e) => setTo(e.target.value)} required />
            </div>
            <div style={{ flex: '0 0 auto' }}>
              <button type="submit">Prikazi kurs</button>
            </div>
          </div>
        </form>

        {rezultat && (
          <table>
            <thead>
              <tr>
                <th>Iz</th>
                <th>U</th>
                <th>Kurs</th>
                <th>Instanca</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>{rezultat.from}</td>
                <td>{rezultat.to}</td>
                <td>{rezultat.conversionMultiple}</td>
                <td>{rezultat.environment}</td>
              </tr>
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}
