import axios from 'axios'

export const GATEWAY_URL = 'http://localhost:8765'

const api = axios.create({ baseURL: GATEWAY_URL })

// svaki zahtev nosi basic auth, tacno onako kako ga gateway ocekuje
api.interceptors.request.use((config) => {
  const stored = sessionStorage.getItem('soas-user')
  if (stored) {
    const { email, password } = JSON.parse(stored)
    config.headers.Authorization = 'Basic ' + btoa(`${email}:${password}`)
  }
  return config
})

// izvlaci poruku koju je vratio global exception handler
export function errorMessage(error) {
  if (error.response && error.response.data && error.response.data.message) {
    return error.response.data.message
  }
  if (error.response) {
    return `Greska ${error.response.status}`
  }
  return 'Server nije dostupan'
}

export default api
