import { useAuth } from '../context/AuthContext'
import { Navigate } from 'react-router-dom'

function PrivateRoute({ children }) {
  const { token } = useAuth()
  return token ? children : <Navigate to="/login" />
}

export default PrivateRoute