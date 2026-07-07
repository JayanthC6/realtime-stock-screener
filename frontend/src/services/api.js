import axios from 'axios'

const envApiBaseUrl = import.meta.env.VITE_API_BASE_URL
const API_BASE_URL = envApiBaseUrl === 'http://localhost:8080' ? '' : (envApiBaseUrl ?? '')

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Attach JWT token to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Auth APIs
export const register = (data) => api.post('/api/auth/register', data)
export const login = (data) => api.post('/api/auth/login', data)

// Stock APIs
export const getAllStocks = () => api.get('/api/stocks/all')
export const getStockBySymbol = (symbol) => api.get(`/api/stocks/${symbol}`)
export const getStockHistory = (symbol, hours = 1) => api.get(`/api/stocks/${symbol}/history?hours=${hours}`)
export const getScreenerResults = () => api.get('/api/stocks/screener/results')
export const getTriggeredResults = () => api.get('/api/stocks/screener/triggered')

// Alert APIs
export const createAlertRule = (data) => api.post('/api/alerts', data)
export const getAlertRules = () => api.get('/api/alerts')
export const deleteAlertRule = (id) => api.delete(`/api/alerts/${id}`)
export const getAlertHistory = () => api.get('/api/alerts/history')

export default api